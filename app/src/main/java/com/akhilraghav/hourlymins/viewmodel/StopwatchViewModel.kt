package com.akhilraghav.hourlymins.viewmodel

import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Timer
import java.util.TimerTask

class StopwatchViewModel : ViewModel() {
    
    // Stopwatch states
    enum class StopwatchState { IDLE, RUNNING, PAUSED }
    
    // Current state
    private val _stopwatchState = MutableStateFlow(StopwatchState.IDLE)
    val stopwatchState: StateFlow<StopwatchState> = _stopwatchState.asStateFlow()
    
    // Current elapsed time
    private val _elapsedTime = MutableStateFlow(0L)
    val elapsedTime: StateFlow<Long> = _elapsedTime.asStateFlow()
    
    // Lap times
    private val _laps = MutableStateFlow<List<Long>>(emptyList())
    val laps: StateFlow<List<Long>> = _laps.asStateFlow()
    
    // Timer for updating the stopwatch
    private var timer: Timer? = null
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    
    fun startStopwatch() {
        if (_stopwatchState.value == StopwatchState.IDLE) {
            startTime = SystemClock.elapsedRealtime()
            pausedTime = 0
        } else if (_stopwatchState.value == StopwatchState.PAUSED) {
            startTime = SystemClock.elapsedRealtime() - pausedTime
        }
        
        _stopwatchState.value = StopwatchState.RUNNING
        
        timer = Timer()
        timer?.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val currentTime = SystemClock.elapsedRealtime()
                _elapsedTime.value = currentTime - startTime
            }
        }, 0, 10) // Update every 10ms for smooth display
    }
    
    fun pauseStopwatch() {
        timer?.cancel()
        timer = null
        pausedTime = _elapsedTime.value
        _stopwatchState.value = StopwatchState.PAUSED
    }
    
    fun resetStopwatch() {
        timer?.cancel()
        timer = null
        _stopwatchState.value = StopwatchState.IDLE
        _elapsedTime.value = 0
        _laps.value = emptyList()
    }
    
    fun addLap() {
        if (_stopwatchState.value == StopwatchState.RUNNING) {
            val currentLaps = _laps.value.toMutableList()
            currentLaps.add(_elapsedTime.value)
            _laps.value = currentLaps
        }
    }
    
    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
    
    class StopwatchViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(StopwatchViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return StopwatchViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
    
    /**
     * Formats time in milliseconds to HH:MM:SS.ms format
     */
    companion object {
        fun formatTime(timeInMillis: Long): String {
            val hours = (timeInMillis / (1000 * 60 * 60)) % 24
            val minutes = (timeInMillis / (1000 * 60)) % 60
            val seconds = (timeInMillis / 1000) % 60
            val millis = (timeInMillis % 1000) / 10
            
            return if (hours > 0) {
                String.format("%02d:%02d:%02d.%02d", hours, minutes, seconds, millis)
            } else {
                String.format("%02d:%02d.%02d", minutes, seconds, millis)
            }
        }
    }
}
