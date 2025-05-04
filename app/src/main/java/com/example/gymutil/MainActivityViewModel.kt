package com.example.gymutil

import android.app.Application
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.gymutil.service.MyTimer
import com.example.gymutil.service.TimerServiceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {
    val timerServiceManager: TimerServiceManager = TimerServiceManager(getApplication<Application>().applicationContext)
    private val _uiState = MutableStateFlow<UiState>(UiState())
    val uiState: StateFlow<UiState> = _uiState
    var timer: MyTimer? = null


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
                timerText = it.getText() + if (it.isOver()) " (time over)" else ""
            )
        }
        if (timer == null) {
            _uiState.value = _uiState.value.copy(
                showTimer = false,
                timerText = ""
            )
        }
    }

    init {
        viewModelScope.launch() {
            timerServiceManager.timer.collect {
                if (timer == null && it != null) {
                    timer = it
                    refreshTimer()
                }
                timer = it
                refreshTimerOnce()
            }
        }
    }

    fun addTimer(workoutId: Long?) {
        timerServiceManager.startTimer(
            MyTimer(
                endsAt = System.currentTimeMillis() + 1000 * (120),
                workoutId = workoutId
            )
        )
    }

    data class UiState(
        val showTimer: Boolean = false,
        val timerText: String = ""
    )


    override fun onCleared() {
        super.onCleared()
        timerServiceManager.unBindService()
    }

    fun onTimerCancelPressed() {
        timerServiceManager.stopTimer()
    }

    fun onTimerAddTimePressed() {
        println("ADDTIME: pressed on add time")
        timerServiceManager.addTime(10)
    }
}