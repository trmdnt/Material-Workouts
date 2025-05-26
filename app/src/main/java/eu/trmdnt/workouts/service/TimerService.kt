package eu.trmdnt.workouts.service

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
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.net.toUri
import eu.trmdnt.workouts.MainActivity
import eu.trmdnt.workouts.R
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import java.util.*
import kotlin.concurrent.fixedRateTimer


private const val TIMER_NOTIF_ID = 1
private const val TIMER_PROGRESS_CHANNEL = "timer_notification_channel"
private const val TIMER_FINISHED_CHANNEL = "timer_finished_notification_channel"
private const val INTENT_STOP_SERVICE = "stop_service"
private const val INTENT_ADD_TIME = "add_time"

private enum class SERVICE_STATE {
    RUNNING, STOPPED, SHOULD_STOP, NOT_STARTED
}

//TODO the code for the service seems very bad
class TimerService : Service() {
    private val binder = LocalBinder()
    private val _timer = MutableStateFlow<MyTimer?>(null)
    val timer: StateFlow<MyTimer?> = _timer
    private var state: SERVICE_STATE = SERVICE_STATE.NOT_STARTED
    private var stopServiceJob: Job? = null

    companion object {
        val TAG: String = TimerService::class.java.simpleName + " (" + this.hashCode() + ")"
    }

    private val refreshTimer: Timer;

    init {
        refreshTimer = fixedRateTimer(period = 1000L, initialDelay = 1000L) {
            refreshState()
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            Log.d(TAG, "onReceive: $action")
            when (action) {
                INTENT_STOP_SERVICE -> {
                    stopService()
                }

                INTENT_ADD_TIME -> {
                    addTime(10)
                }
            }
        }
    }

    private fun refreshState() {
        Log.d(TAG, "refreshState: state: $state")
        when (state) {
            SERVICE_STATE.RUNNING -> {
                stopServiceJob?.cancel()
                if (timer.value?.isOver() == true) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val channel = NotificationChannel(
                            TIMER_FINISHED_CHANNEL, "timer finished", NotificationManager.IMPORTANCE_HIGH
                        )
                        val notificationManager =
                            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                        notificationManager.createNotificationChannel(channel)
                    }

                    val notificationBuilder: Builder = Builder(
                        this@TimerService,
                        TIMER_FINISHED_CHANNEL
                    ).setSmallIcon(R.drawable.rounded_timer_off_24).setContentTitle("Set timer")
                        .setContentText("time is over " + timer.value!!.getText())
                        .setPriority(NotificationCompat.PRIORITY_HIGH).setSilent(false)
                        .setContentIntent(getOpenAppPendingIntent())
                    if (ActivityCompat.checkSelfPermission(
                            this@TimerService, Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                    } else {
                        NotificationManagerCompat.from(this@TimerService)
                            .notify(TIMER_NOTIF_ID, notificationBuilder.build())
                    }
                    stopForeground(STOP_FOREGROUND_DETACH)
                    state = SERVICE_STATE.SHOULD_STOP
                    stopServiceJob = CoroutineScope(Dispatchers.Main).launch {
                        delay(5000)
                        if (state == SERVICE_STATE.SHOULD_STOP) {
                            Log.d(TAG, "refreshState: calling stopService()")
                            stopService()
                        }
                    }
                } else {
                    updateNotification()
                }
            }

            SERVICE_STATE.STOPPED -> {
                refreshTimer.cancel()
                Log.d(TAG, "refreshState: cancelled")
            }

            SERVICE_STATE.SHOULD_STOP -> {

            }

            SERVICE_STATE.NOT_STARTED -> {

            }
        }
    }


    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val timerString = intent?.getStringExtra("timer")
        val action = intent?.action
        Log.d(TAG, "onStartCommand: ")
        Log.d(TAG, "onStartCommand: intent: $intent")
        Log.d(TAG, "onStartCommand: timer: $timerString")
        Log.d(TAG, "onStartCommand: action: $action")
        if (timerString == null) {
            Log.e(TAG, "onStartCommand: timer is null")
            Log.e(TAG, "intent: ${intent.toString()}")
            stopService()
            return START_REDELIVER_INTENT
        } else {
            Log.d(TAG, "service should not stop")
            state = SERVICE_STATE.RUNNING
            val filter = IntentFilter()
            filter.addAction(INTENT_STOP_SERVICE)
            filter.addAction(INTENT_ADD_TIME)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                registerReceiver(broadcastReceiver, filter, RECEIVER_NOT_EXPORTED)
            } else {
                registerReceiver(broadcastReceiver, filter)
            }


            _timer.value = Json.decodeFromString<MyTimer>(timerString)
            Log.d(TAG, "onStartCommand: created timer object: ${_timer.value?.getText()}")
            startAsForegroundService()

        }
        return super.onStartCommand(intent, flags, startId)
    }

    fun stopService() {
        //throw RuntimeException()
        Log.d(TAG, "stopService: called")
        /*
        this is in case the service was removed from foreground and detached from the notification (after the
         time ran out) but was started in foreground again (because the timer was changed) and is then stopped while
         running. Since the service is now detached from the notification, the last timer progress update
         notification stays so it needs to be removed manually
         */
        if (state == SERVICE_STATE.RUNNING) {
            val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.cancel(TIMER_NOTIF_ID)
        }

        state = SERVICE_STATE.STOPPED
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (e: Error) {

        }

        stopForeground(STOP_FOREGROUND_REMOVE)
        Log.d(TAG, "stopService: removed itself from foreground")
        stopSelf()
        stopService(Intent(this, TimerService::class.java))
    }

    inner class LocalBinder : Binder() {
        fun getService(): TimerService = this@TimerService
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(TAG, "onBind: bind to caller")
        return binder
    }

    private fun startAsForegroundService() {
        // promote service to foreground service
        state = SERVICE_STATE.RUNNING
        Log.d(TAG, "startAsForegroundService: try to promote to foreground")

        ServiceCompat.startForeground(
            this, TIMER_NOTIF_ID, getNotification(), if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            }
        )
    }

    private fun updateNotification() {
        val notificationManager: NotificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(TIMER_NOTIF_ID, getNotification())
        Log.d(TAG, "updateNotification: refreshed notification")
    }

    private fun getNotification(): Notification {
        val notificationManager = applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_PROGRESS_CHANNEL, "timer progress", NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val stopPendingIntent = getActionPendingIntent(INTENT_STOP_SERVICE)
        val addTimePendingIntent = getActionPendingIntent(INTENT_ADD_TIME)

        val openAppPendingIntent = getOpenAppPendingIntent()

        val notificationBuilder: Builder = Builder(this, TIMER_PROGRESS_CHANNEL)
        return notificationBuilder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentTitle("Set timer")
            .setContentText(timer.value!!.getText()).setSmallIcon(R.drawable.rounded_timer_24).setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW).setCategory(NotificationCompat.CATEGORY_PROGRESS)
            .setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.outline_timer_10_select_24, "Add 10s", addTimePendingIntent)
            .addAction(R.drawable.rounded_timer_off_24, "Stop", stopPendingIntent).build()
    }

    private fun getOpenAppPendingIntent(): PendingIntent {
        var openAppIntent: Intent
        if (timer.value!!.workoutId != null) {
            openAppIntent = Intent(
                Intent.ACTION_VIEW,
                //TODO deep link does not open the workout in edit mode
                "workouts://app/activities/workout/${timer.value!!.workoutId}?editMode=true".toUri(),
                applicationContext,
                MainActivity::class.java
            )
        } else {
            openAppIntent = Intent(this, MainActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        val openAppPendingIntent = PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)
        return openAppPendingIntent
    }

    private fun getActionPendingIntent(action: String): PendingIntent? {
        val intent = Intent(INTENT_STOP_SERVICE)
        intent.setPackage(applicationContext.packageName)
        intent.action = action
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent;
    }

    fun addTime(seconds: Int) {
        startAsForegroundService()
        _timer.value = timer.value?.addTime(seconds)
        Log.d(TAG, "addTime: replaced timer")
        refreshState()
    }

    fun restart(timer: MyTimer) {
        startAsForegroundService()
        _timer.value = timer
        Log.d(TAG, "restart: replaced timer")
    }
}