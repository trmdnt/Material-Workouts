package eu.trmdnt.workouts.service

import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.startForegroundService
import eu.trmdnt.workouts.settings.SettingsManager
import eu.trmdnt.workouts.settings.timerDefaultValuePreferenceKey
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

@Singleton
class TimerServiceManager(
    val applicationContext: Context,
    private val settingsManager: SettingsManager
) {
    private val _timer = MutableStateFlow<MyTimer?>(null)
    val timer: StateFlow<MyTimer?> = _timer

    private var removalJob: Job? = null
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        val TAG: String = TimerServiceManager::class.java.simpleName
    }

    fun startTimer(timer: MyTimer) {
        _timer.value = timer

        val intent = Intent(applicationContext, TimerService::class.java)
        startForegroundService(applicationContext, intent)
        scheduleTimerRemoval()
    }

    private fun scheduleTimerRemoval() {
        removalJob?.cancel()
        timer.value?.let {
            removalJob = managerScope.launch {
                delay((it.getTimeLeft() + 5000).milliseconds)
                timer.value?.let { timer ->
                    if (timer.isOver()) {
                        _timer.value = null
                    } else {
                        scheduleTimerRemoval()
                    }
                }
            }
        }
    }

    suspend fun startTimer(workoutId: Long) {
        val time = settingsManager.getIntPreference(timerDefaultValuePreferenceKey).first()
        val timer =
            MyTimer(workoutId = workoutId, endsAt = System.currentTimeMillis() + time * 1000)
        startTimer(timer)
    }

    fun addTime(seconds: Int) {
        startTimer(timer.value!!.addTime(seconds))
    }

    fun stopTimer() {
        _timer.value = null
    }
}