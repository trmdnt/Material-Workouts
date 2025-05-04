package com.example.gymutil.service

import android.Manifest
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.gymutil.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.concurrent.fixedRateTimer


private const val TIMER_NOTIF_ID = 1
private const val TIMER_PROGRESS_CHANNEL = "timer_notification_channel"
private const val TIMER_FINISHED_CHANNEL = "timer_finished_notification_channel"
private const val INTENT_STOP_SERVICE = "stop_service"

class TimerService : Service() {
    private val binder = LocalBinder()
    private val _timer = MutableStateFlow<MyTimer?>(null)
    val timer: StateFlow<MyTimer?> = _timer
    private var notificationTimer: Timer? = null
    private var shouldStop: Boolean = false

    companion object {
        val TAG: String = TimerService::class.java.simpleName
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action == INTENT_STOP_SERVICE) {
                stopService()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("TimerService: new service started")

        val timerString = intent?.getStringExtra("timer")
        if (timerString == null) {
            Log.e(TAG, "onStartCommand: timer is null")
            throw IllegalArgumentException("onStartCommand: timer is null")
        }
        Log.d(TAG, "service should not stop")
        shouldStop = false
        val filter = IntentFilter()
        filter.addAction(INTENT_STOP_SERVICE)
        ContextCompat.registerReceiver(this, broadcastReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        _timer.value = Json.decodeFromString<MyTimer>(timerString)
        Log.d(TAG, "onStartCommand: created timer object: ${_timer.value?.getText()}")
        startAsForegroundService()


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun stopService() {
        notificationTimer?.cancel()
        unregisterReceiver(broadcastReceiver)
        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d(TAG, "stopForegroundService: removed itself from foreground")
        stopSelf()
        stopService(Intent(this, TimerService::class.java))
    }

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    private fun getNotification(): Notification {
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_PROGRESS_CHANNEL, "timer", NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }
        val stopIntent = Intent(applicationContext, broadcastReceiver::class.java)
        stopIntent.action = INTENT_STOP_SERVICE
        val stopPendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(this, TIMER_PROGRESS_CHANNEL)
        return notificationBuilder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentTitle("set timer").setSubText(applicationContext.applicationInfo.name)
            .setContentText(timer.value!!.getText()).setSmallIcon(R.drawable.rounded_timer_24).setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW).setCategory(NotificationCompat.CATEGORY_PROGRESS)
//            .setSilent(true)
//            .setProgress(100, timer.value!!.getPercentageDone(), false)
            .addAction(R.drawable.rounded_timer_off_24, "Stop", stopPendingIntent)
            .build()
    }

    private fun startAsForegroundService() {
        // promote service to foreground service
        startUpdating()

        ServiceCompat.startForeground(
            this, TIMER_NOTIF_ID, getNotification(), if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )
    }

    private fun startUpdating() {
        if (notificationTimer == null) {
            notificationTimer = fixedRateTimer(period = 1000L, initialDelay = 1000L) {
                if (timer.value?.isOver() == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            TIMER_FINISHED_CHANNEL, "timer", NotificationManager.IMPORTANCE_HIGH
                        )
                        val notificationManager =
                            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.createNotificationChannel(channel)
                    }

                    val notificationBuilder: NotificationCompat.Builder =
                        NotificationCompat.Builder(this@TimerService, TIMER_FINISHED_CHANNEL)
                            .setSmallIcon(R.drawable.rounded_timer_off_24).setContentTitle("set timer")
                            .setContentText("time is over " + timer.value!!.getText())
                            .setPriority(NotificationCompat.PRIORITY_HIGH).setSilent(false)
                    if (ActivityCompat.checkSelfPermission(
                            this@TimerService, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                    } else {
                        NotificationManagerCompat.from(this@TimerService)
                            .notify(2, notificationBuilder.build())
                    }
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    Log.d(TAG, "service should stop")
                    shouldStop = true
                    CoroutineScope(Dispatchers.Main).launch {
                        delay(5000)
                        if (shouldStop) {
                            Log.d(TAG, "service stopping")
                            stopService()
                        }
                    }
                    this.cancel()
                    notificationTimer = null
                } else {
                    updateNotification()
                }
            }
        }
    }

    private fun updateNotification() {
        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(TIMER_NOTIF_ID, getNotification())
    }

    fun addTime(seconds: Int) {
        startUpdating()
        shouldStop = false
        _timer.value = timer.value?.addTime(seconds)
    }

    fun restart(timer: MyTimer) {
        startUpdating()
        shouldStop = false
        _timer.value = timer
    }
}