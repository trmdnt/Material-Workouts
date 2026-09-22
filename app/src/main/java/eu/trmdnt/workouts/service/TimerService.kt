package eu.trmdnt.workouts.service

import android.Manifest
import android.annotation.SuppressLint
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
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Builder
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import dagger.hilt.android.AndroidEntryPoint
import eu.trmdnt.workouts.MainActivity
import eu.trmdnt.workouts.R
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds


private const val TIMER_NOTIF_ID = 1
private const val TIMER_PROGRESS_CHANNEL = "timer_notification_channel"
private const val TIMER_FINISHED_CHANNEL = "timer_finished_notification_channel"
private const val INTENT_STOP_SERVICE = "stop_service"
private const val INTENT_ADD_TIME = "add_time"

@AndroidEntryPoint
class TimerService : Service() {
    @Inject
    lateinit var timerServiceManager: TimerServiceManager

    private val serviceScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    private val binder = Binder()

    private var isRunning = false

    companion object {
        val TAG: String = TimerService::class.java.simpleName
    }

    private fun handleTimerUpdate() {
        timerJob?.cancel()

        timerJob = serviceScope.launch {
            while (isActive) {
                refreshState()
                delay(1000.milliseconds)
            }
        }
    }

    private fun stop() {
        stopForeground(STOP_FOREGROUND_DETACH)
        stopSelf()
    }

    private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            when (action) {
                INTENT_STOP_SERVICE -> {
                    timerServiceManager.stopTimer()
                }

                INTENT_ADD_TIME -> {
                    timerServiceManager.addTime(10)
                }
            }
        }
    }

    private fun refreshState() {
        timerServiceManager.timer.value?.let { timer ->
            if (timer.isOver()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val channel = NotificationChannel(
                        TIMER_FINISHED_CHANNEL,
                        "timer finished",
                        NotificationManager.IMPORTANCE_HIGH
                    )
                    val notificationManager =
                        applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
                    notificationManager.createNotificationChannel(channel)
                }

                val notificationBuilder: Builder = Builder(
                    this@TimerService, TIMER_FINISHED_CHANNEL
                ).setSmallIcon(R.drawable.rounded_timer_off_24).setContentTitle(getString(R.string.set_timer))
                    .setContentText(getString(R.string.time_is_over, timer.getText()))
                    .setPriority(NotificationCompat.PRIORITY_HIGH).setSilent(false)
                    .setContentIntent(getOpenAppPendingIntent())
                if (ActivityCompat.checkSelfPermission(
                        this@TimerService, Manifest.permission.POST_NOTIFICATIONS
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    NotificationManagerCompat.from(this@TimerService)
                        .notify(TIMER_NOTIF_ID, notificationBuilder.build())
                }
                stop()
            } else {
                updateNotification()
            }
        }

    }


    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (!isRunning) {
            isRunning = true
            serviceScope.launch {
                timerServiceManager.timer.collect { timer ->
                    if (timer == null) {
                        //remove the notification if the timer was stopped
                        stopForeground(STOP_FOREGROUND_REMOVE)
                        stop()
                    } else {
                        handleTimerUpdate()
                    }
                }
            }


            val filter = IntentFilter()
            filter.addAction(INTENT_STOP_SERVICE)
            filter.addAction(INTENT_ADD_TIME)


            ContextCompat.registerReceiver(
                this,
                broadcastReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )

            startAsForegroundService()
        }

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun startAsForegroundService() {
        ServiceCompat.startForeground(
            this,
            TIMER_NOTIF_ID,
            getNotification(),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        )
    }

    private fun updateNotification() {
        val notificationManager: NotificationManager =
            getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(TIMER_NOTIF_ID, getNotification())
    }

    private fun getNotification(): Notification {
        val notificationManager =
            applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                TIMER_PROGRESS_CHANNEL, "timer progress", NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        val stopPendingIntent = getActionPendingIntent(INTENT_STOP_SERVICE)
        val addTimePendingIntent = getActionPendingIntent(INTENT_ADD_TIME)

        val openAppPendingIntent = getOpenAppPendingIntent()

        val notificationBuilder = Builder(this, TIMER_PROGRESS_CHANNEL)
        return notificationBuilder.setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentTitle(getString(R.string.set_timer)).setContentText(timerServiceManager.timer.value!!.getText())
            .setSmallIcon(R.drawable.rounded_timer_24).setShowWhen(false)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_PROGRESS).setOngoing(true)
            .setContentIntent(openAppPendingIntent)
            .addAction(R.drawable.outline_timer_10_select_24, getString(R.string.add_10s), addTimePendingIntent)
            .addAction(R.drawable.rounded_timer_off_24, getString(R.string.stop), stopPendingIntent).build()
    }

    private fun getOpenAppPendingIntent(): PendingIntent {
        var openAppIntent: Intent
        val timer = timerServiceManager.timer.value
        if (timer != null && timer.workoutId != null) {
            openAppIntent = Intent(
                Intent.ACTION_VIEW,
                "workouts://app/activities/workout/${timer.workoutId}?edit=true".toUri(),
                applicationContext,
                MainActivity::class.java
            )
        } else {
            openAppIntent = Intent(this, MainActivity::class.java).apply {
                this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
        }
        val openAppPendingIntent =
            PendingIntent.getActivity(this, 0, openAppIntent, PendingIntent.FLAG_IMMUTABLE)
        return openAppPendingIntent
    }

    private fun getActionPendingIntent(action: String): PendingIntent? {
        val intent = Intent(INTENT_STOP_SERVICE)
        intent.setPackage(applicationContext.packageName)
        intent.action = action
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        return pendingIntent
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(broadcastReceiver)
        } catch (_: Exception) {
        }
        serviceScope.cancel()
    }
}