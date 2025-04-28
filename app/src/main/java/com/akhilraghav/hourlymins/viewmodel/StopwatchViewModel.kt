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
    
    // Current elapsed time
    private val _timeElapsed = MutableStateFlow(0L)
    val timeElapsed: StateFlow<Long> = _timeElapsed.asStateFlow()
    
    // Running state
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    // Lap times
    private val _laps = MutableStateFlow<List<Long>>(emptyList())
    val laps: StateFlow<List<Long>> = _laps.asStateFlow()
    
    // Timer for updating the stopwatch
    private var timer: Timer? = null
    private var startTime: Long = 0
    private var pausedTime: Long = 0
    
    fun start() {
        if (!_isRunning.value) {
            if (_timeElapsed.value == 0L) {
                startTime = SystemClock.elapsedRealtime()
            } else {
                // Resume from paused state
                startTime = SystemClock.elapsedRealtime() - _timeElapsed.value
            }
            
            _isRunning.value = true
            
            timer = Timer()
            timer?.scheduleAtFixedRate(object : TimerTask() {
                override fun run() {
                    val currentTime = SystemClock.elapsedRealtime()
                    _timeElapsed.value = currentTime - startTime
                }
            }, 0, 10) // Update every 10ms for smooth display
        }
    }
    
    fun pause() {
        timer?.cancel()
        timer = null
        _isRunning.value = false
    }
    
    fun stop() {
        timer?.cancel()
        timer = null
        _isRunning.value = false
        _timeElapsed.value = 0L
        _laps.value = emptyList()
    }
    
    fun lap() {
        if (_isRunning.value) {
            val currentLaps = _laps.value.toMutableList()
            currentLaps.add(_timeElapsed.value)
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
}
