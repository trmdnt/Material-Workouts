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
    val timerServiceManager: TimerServiceManager
    private val _uiState = MutableStateFlow<UiState>(UiState())
    val uiState: StateFlow<UiState> = _uiState
    var timer: MyTimer? = null


    fun refreshTimer() {
        timer?.let {
            if (it.isOver()) {

            }
            _uiState.value = _uiState.value.copy(
                showTimer = true,
                timerText = it.getText() + if (it.isOver()) " (time over)" else ""
            )
            Handler(Looper.getMainLooper()).postDelayed({
                refreshTimer()
            }, 1000)
        }
        if (timer == null) {
            _uiState.value = _uiState.value.copy(
                showTimer = false,
                timerText = ""
            )
        }
    }

    init {
        timerServiceManager = TimerServiceManager(getApplication<Application>().applicationContext)
        timerServiceManager.startTimer(MyTimer(endsAt = System.currentTimeMillis() + 1000 * (120 + 30)))

        viewModelScope.launch() {
            timerServiceManager.timer.collect {
                if (timer == null && it != null) {
                    Handler(Looper.getMainLooper()).postDelayed({
                        refreshTimer()
                    }, 1000)
                }
                timer = it
            }
        }
    }

    data class UiState(
        val showTimer: Boolean = false,
        val timerText: String = ""
    )


    override fun onCleared() {
        super.onCleared()
        timerServiceManager.unBindService()
    }
}