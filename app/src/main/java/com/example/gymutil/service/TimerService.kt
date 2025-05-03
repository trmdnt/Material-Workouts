package com.example.gymutil.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.gymutil.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.concurrent.fixedRateTimer

private const val NOTIF_ID = 1

class TimerService : Service() {
    private val binder = LocalBinder()
    private val _timer = MutableStateFlow<MyTimer?>(null)
    val timer: StateFlow<MyTimer?> = _timer
    private var notificationTimer: Timer? = null

    companion object {
        val TAG: String = TimerService::class.java.simpleName
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("TimerService: new service started")
//        Toast.makeText(
//            this,
//            "service was started from empty intent, check battery optimization settings",
//            Toast.LENGTH_SHORT
//        ).show()

        val timerString = intent?.getStringExtra("timer")
        if (timerString == null) {
            Log.e(TAG, "onStartCommand: timer is null")
            throw IllegalArgumentException("onStartCommand: timer is null")
        } else {
            _timer.value = Json.decodeFromString<MyTimer>(timerString)
            startAsForegroundService()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun stopForegroundService() {
        Toast.makeText(this, "stop command", Toast.LENGTH_SHORT).show()
        notificationTimer?.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
        stopService(Intent(this, TimerService::class.java))
    }

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    private fun getNotification(): Notification {
        val notificationBuilder: NotificationCompat.Builder
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

            val channel = NotificationChannel(
                "general_notification_channel",
                "timer",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)

            notificationBuilder =
                NotificationCompat.Builder(this, "general_notification_channel")
        } else {
            notificationBuilder = NotificationCompat.Builder(this)
        }
        return notificationBuilder
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentTitle("set timer")
            .setSubText(applicationContext.applicationInfo.name)
            .setContentText(timer.value!!.getText())
            .setSmallIcon(R.drawable.rounded_timer_24)
            .setShowWhen(false)
            .build()
    }

    private fun startAsForegroundService() {
        // promote service to foreground service
        notificationTimer = fixedRateTimer(period = 1000L, initialDelay = 1000L) {
            updateNotification()
        }

        ServiceCompat.startForeground(
            this,
            NOTIF_ID,
            getNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )
    }

    private fun updateNotification() {
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID, getNotification())
    }

    fun addTime(seconds: Int) {
        _timer.value = timer.value?.addTime(seconds)
    }

    fun restart(timer: MyTimer) {
        _timer.value = timer
    }
}