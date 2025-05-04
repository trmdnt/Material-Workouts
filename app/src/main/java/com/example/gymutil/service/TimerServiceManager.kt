package com.example.gymutil.service

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.core.content.ContextCompat.startForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class TimerServiceManager(
    val applicationContext: Context
) {
    private var timerService: TimerService? = null
    private var isConnecting = false
    private val _timer = MutableStateFlow<MyTimer?>(null)
    val timer: StateFlow<MyTimer?> = _timer

    val timerServiceCollector: FlowCollector<MyTimer?> = FlowCollector {
        println("ADDTIME: manager: received new value")
        _timer.value = it
    }

    var timerCollectorJob: Job? = null

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as TimerService.LocalBinder
            isConnecting = false
            timerService = binder.getService()

            timerCollectorJob = CoroutineScope(Dispatchers.Main).launch {
                binder.getService().timer.collect(timerServiceCollector)
            }

            println("TimerServiceManager connected")
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            timerService = null
            timerCollectorJob?.cancel()
            timerCollectorJob = null
            _timer.value = null
            println("TimerServiceManager disconnected")
        }
    }

    init {
        tryToBindToServiceIfRunning()
    }

    fun startTimer(timer: MyTimer) {
        timerService?.let {
            it.restart(timer)
            return
        }

        if (!isConnecting) {
            val intent = Intent(applicationContext, TimerService::class.java)
            intent.putExtra("timer", Json.encodeToString(timer))
            startForegroundService(applicationContext, intent)
            tryToBindToServiceIfRunning()
        }
    }

    fun addTime(seconds: Int) {
        timerService?.addTime(seconds)
    }

    fun stopTimer() {
        timerService?.stopService()
    }

    fun unBindService() {
        applicationContext.unbindService(connection)
    }

//    fun getTimer(): StateFlow<MyTimer?> {
//        return timer
//    }

    private fun tryToBindToServiceIfRunning(): Boolean {
        if (isConnecting) {
            return true
        }
        if (isServiceRunning(TimerService::class.java)) {
            println("TimerServiceManager: Service is already running, trying to bind.")
            isConnecting = true
            return applicationContext.bindService(
                Intent(applicationContext, TimerService::class.java), connection, 0
            )
        } else {
            println("TimerServiceManager: Service is not running.")
            return false
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = applicationContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }
}

interface TimerServiceObserver {
    fun onTimerModified()
}