package com.example.gymutil.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.example.gymutil.R
import kotlinx.serialization.json.Json
import kotlin.concurrent.fixedRateTimer

private const val NOTIF_ID = 1

class TimerService : Service() {
    private val binder = LocalBinder()
    public lateinit var timer: MyTimer
    private val notificationTimer = fixedRateTimer(period = 1000L, initialDelay = 1000L) {
        updateNotification()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        println("TimerService: new service started")
        Toast.makeText(this, "start command", Toast.LENGTH_SHORT).show()
        val timerString = intent?.getStringExtra("timer") ?: throw IllegalArgumentException("did not find timer")
        timer = Json.decodeFromString<MyTimer>(timerString)

        startAsForegroundService()


        notificationTimer
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return binder
    }

    fun stopForegroundService() {
        Toast.makeText(this, "stop command", Toast.LENGTH_SHORT).show()
        stopSelf()
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
            .setContentTitle("Timer Service")
            .setContentText(timer.getText())
            .setSubText("test")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()
    }

    private fun startAsForegroundService() {
        // promote service to foreground service
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
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIF_ID, getNotification())
    }

}