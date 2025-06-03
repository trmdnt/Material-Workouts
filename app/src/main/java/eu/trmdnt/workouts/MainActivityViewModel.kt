package eu.trmdnt.workouts

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.trmdnt.workouts.service.MyTimer
import eu.trmdnt.workouts.service.TimerServiceManager
import eu.trmdnt.workouts.settings.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val settingsManager: SettingsManager, @ApplicationContext private val appContext: Context
) : ViewModel() {
    // TODO the viewmodel should probably not be in charge of managing the timerServiceManager for the whole app

    val timerServiceManager: TimerServiceManager = TimerServiceManager(appContext)
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
                showTimer = true, timerText = it.getText() + if (it.isOver()) " (time over)" else ""
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
            settingsManager.getBooleanPreference(alwaysShowTimerUiPreferenceKey).collect {
                alwaysShowTimerUi = it
                _uiState.value = _uiState.value.copy(
                    showTimerPickerButton = it
                )
            }
        }

        viewModelScope.launch {
            settingsManager.getIntPreference(timerDefaultValuePreferenceKey).collect {
                timerDefaultValue = it
                _uiState.value = _uiState.value.copy(
                    timerDefaultValue = it
                )
            }
        }

        viewModelScope.launch {
            settingsManager.getBooleanPreference(startTimerOnSetPreferenceKey).collect {
                startTimerOnSet = it
            }
        }

        viewModelScope.launch {
            settingsManager.getBooleanPreference(useDynamicColorPreferenceKey).collect {
                _uiState.value = _uiState.value.copy(
                    useDynamicColors = it
                )
            }
        }

        viewModelScope.launch {
            settingsManager.getThemePreference().collect { theme ->
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
                endsAt = System.currentTimeMillis() + (time ?: timerDefaultValue) * 1000 + 10, workoutId = workoutId
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


    override fun onCleared() {
        super.onCleared()
        timerServiceManager.unBindService()
    }

    fun onTimerCancelPressed() {
        timerServiceManager.stopTimer()
    }

    fun onTimerAddTimePressed() {
        timerServiceManager.addTime(10)
    }
}