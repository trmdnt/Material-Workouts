package eu.trmdnt.workouts

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import eu.trmdnt.workouts.service.MyTimer
import eu.trmdnt.workouts.service.TimerServiceManager
import eu.trmdnt.workouts.ui.settings.alwaysShowTimerUiPreference
import eu.trmdnt.workouts.ui.settings.startTimerOnSetPreference
import eu.trmdnt.workouts.ui.settings.timerDefaultValuePreference
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val preferencesDataStore: DataStore<Preferences>, @ApplicationContext private val appContext: Context
) : ViewModel() {
    val timerServiceManager: TimerServiceManager = TimerServiceManager(appContext)
    private val _uiState = MutableStateFlow<UiState>(UiState())
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

        //TODO does not work
        viewModelScope.launch {
            preferencesDataStore.data.map { preferences ->
                preferences[alwaysShowTimerUiPreference] != false
            }.collect {
                alwaysShowTimerUi = it
                _uiState.value = _uiState.value.copy(
                    showTimerPickerButton = it
                )
            }
        }

        viewModelScope.launch {
            preferencesDataStore.data.map { preferences ->
                preferences[timerDefaultValuePreference] ?: 90
            }.collect {
                timerDefaultValue = it
                _uiState.value = _uiState.value.copy(
                    timerDefaultValue = it
                )
            }
        }

        viewModelScope.launch {
            preferencesDataStore.data.map { preferences ->
                preferences[startTimerOnSetPreference] != false
            }.collect {
                startTimerOnSet = it
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
                endsAt = System.currentTimeMillis() + (time ?: timerDefaultValue) * 1000, workoutId = workoutId
            )
        )
    }

    data class UiState(
        val showTimer: Boolean = false,
        val timerText: String = "",
        val showTimerPickerButton: Boolean = true,
        val timerDefaultValue: Int = 0
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