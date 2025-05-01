package com.example.gymutil.service

import kotlinx.serialization.Serializable


@Serializable
data class MyTimer(
    val startedAt: Long = System.currentTimeMillis(),
    val endsAt: Long,
) {
    private fun getTimeLeft(): Long {
        val now = System.currentTimeMillis()
        val timeLeft = endsAt - now
        if (timeLeft < 0L) {
            return 0
        }
        return timeLeft
    }

    private fun getTotalTime(): Long {
        return (endsAt - startedAt)
    }

    private fun formatTimeSpan(time: Long): String {
        val totalSeconds = time / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }

    fun isOver(): Boolean {
        return getTimeLeft() == 0.toLong()
    }

    fun getTimePassed(): String {
        return formatTimeSpan(getTotalTime() - getTimeLeft())
    }

    fun getTotalTimeString(): String {
        return formatTimeSpan(getTotalTime())
    }

    fun getText(): String {
        return getTimePassed() + "/" + getTotalTimeString()
    }
}