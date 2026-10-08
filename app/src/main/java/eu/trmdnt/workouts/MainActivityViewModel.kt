package eu.trmdnt.workouts

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import eu.trmdnt.workouts.service.MyTimer
import eu.trmdnt.workouts.service.TimerServiceManager
import eu.trmdnt.workouts.settings.Prefs
import eu.trmdnt.workouts.settings.SettingsManager
import eu.trmdnt.workouts.settings.Theme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val settingsManager: SettingsManager,
    private val appContext: Application,
    private val timerServiceManager: TimerServiceManager
) : ViewModel() {
    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow<UiState>(UiState())
    val uiState: StateFlow<UiState> = _uiState
    var timer: MyTimer? = null

    private var startTimerOnSet = true
    private var timerDefaultValue = 99
    private var alwaysShowTimerUi: Boolean = true


    private fun refreshTimer() {
        refreshTimerOnce()
        if (timer != null) {
            Handler(Looper.getMainLooper()).postDelayed({
                refreshTimer()
            }, 1000)
        }
    }

    private fun refreshTimerOnce() {
        timer?.let {
            _uiState.value = _uiState.value.copy(
                showTimer = true,
                timerText = it.getText() + if (it.isOver()) " (" + appContext.getString(R.string.time_over) + ")" else ""
            )
        }
        if (timer == null) {
            _uiState.value = _uiState.value.copy(
                showTimer = false, timerText = ""
            )
        }
    }

    init {
        viewModelScope.launch {
            timerServiceManager.timer.collect {
                if (timer == null && it != null) {
                    timer = it
                    refreshTimer()
                }
                timer = it
                refreshTimerOnce()
            }
        }

        viewModelScope.launch {
            settingsManager.getPreference(Prefs.alwaysShowTimerUi).collect {
                alwaysShowTimerUi = it
                _uiState.value = _uiState.value.copy(
                    showTimerPickerButton = it
                )
            }
        }

        viewModelScope.launch {
            settingsManager.getPreference(Prefs.timerDefaultValue).collect {
                timerDefaultValue = it
                _uiState.value = _uiState.value.copy(
                    timerDefaultValue = it
                )
            }
        }

        viewModelScope.launch {
            settingsManager.getPreference(Prefs.startTimerOnSet).collect {
                startTimerOnSet = it
            }
        }

        viewModelScope.launch {
            settingsManager.getPreference(Prefs.useDynamicColor).collect {
                _uiState.value = _uiState.value.copy(
                    useDynamicColors = it
                )
            }
        }

        viewModelScope.launch {
            settingsManager.getPreference(Prefs.useTheme).collect { theme ->
                _uiState.value = _uiState.value.copy(theme = theme, showSplashScreen = false)
            }
        }
    }

    fun addTimer(workoutId: Long? = null, time: Int? = null) {
        if (workoutId != null) {
            if (!startTimerOnSet) {
                return
            }
        }
        timerServiceManager.startTimer(
            MyTimer(
                //need to add 10ms because time is rounded down which leads to the time in mm:ss missing one sec
                endsAt = System.currentTimeMillis() + (time ?: timerDefaultValue) * 1000 + 10,
                workoutId = workoutId
            )
        )
    }

    data class UiState(
        val showTimer: Boolean = false,
        val timerText: String = "",
        val showTimerPickerButton: Boolean = true,
        val timerDefaultValue: Int = 0,
        val theme: Theme = Theme.System,
        val useDynamicColors: Boolean = true,
        val showSplashScreen: Boolean = true
    )

    fun onTimerCancelPressed() {
        timerServiceManager.stopTimer()
    }

    fun onTimerAddTimePressed() {
        timerServiceManager.addTime(10)
    }
}