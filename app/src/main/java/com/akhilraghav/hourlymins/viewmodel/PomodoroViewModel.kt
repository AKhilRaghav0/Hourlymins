package com.akhilraghav.hourlymins.viewmodel

import android.content.Context
import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.akhilraghav.hourlymins.notifications.NotificationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PomodoroViewModel : ViewModel() {
    
    // Timer states
    enum class TimerState { IDLE, WORK, BREAK, PAUSED }
    
    // Default durations (in milliseconds)
    companion object {
        const val DEFAULT_WORK_DURATION = 25 * 60 * 1000L  // 25 minutes
        const val DEFAULT_SHORT_BREAK_DURATION = 5 * 60 * 1000L  // 5 minutes
        const val DEFAULT_LONG_BREAK_DURATION = 15 * 60 * 1000L  // 15 minutes
        const val TIMER_INTERVAL = 1000L  // 1 second
    }
    
    // Timer configuration
    private var workDuration = DEFAULT_WORK_DURATION
    private var shortBreakDuration = DEFAULT_SHORT_BREAK_DURATION
    private var longBreakDuration = DEFAULT_LONG_BREAK_DURATION
    private var longBreakAfterPomodoros = 4
    
    // Current state
    private val _timerState = MutableStateFlow(TimerState.IDLE)
    val timerState: StateFlow<TimerState> = _timerState.asStateFlow()
    
    // Current time remaining
    private val _timeRemaining = MutableStateFlow(workDuration)
    val timeRemaining: StateFlow<Long> = _timeRemaining.asStateFlow()
    
    // Current pomodoro count
    private val _pomodoroCount = MutableStateFlow(0)
    val pomodoroCount: StateFlow<Int> = _pomodoroCount.asStateFlow()
    
    // Timer
    private var countDownTimer: CountDownTimer? = null
    
    // Notification helper
    private var notificationHelper: NotificationHelper? = null
    
    fun initialize(context: Context) {
        notificationHelper = NotificationHelper(context)
    }
    
    fun startWorkTimer() {
        _timerState.value = TimerState.WORK
        _timeRemaining.value = workDuration
        startTimer()
    }
    
    fun startBreakTimer() {
        val isLongBreak = _pomodoroCount.value % longBreakAfterPomodoros == 0 && _pomodoroCount.value > 0
        val breakDuration = if (isLongBreak) longBreakDuration else shortBreakDuration
        
        _timerState.value = TimerState.BREAK
        _timeRemaining.value = breakDuration
        startTimer()
        
        // Show notification
        notificationHelper?.showPomodoroNotification(
            title = if (isLongBreak) "Long Break Started" else "Short Break Started",
            message = if (isLongBreak) "Take a ${longBreakDuration / (60 * 1000)} minute break" else "Take a ${shortBreakDuration / (60 * 1000)} minute break"
        )
    }
    
    fun pauseTimer() {
        countDownTimer?.cancel()
        _timerState.value = TimerState.PAUSED
    }
    
    fun resumeTimer() {
        if (_timerState.value == TimerState.PAUSED) {
            if (_timerState.value == TimerState.WORK) {
                _timerState.value = TimerState.WORK
            } else {
                _timerState.value = TimerState.BREAK
            }
            startTimer()
        }
    }
    
    fun stopTimer() {
        countDownTimer?.cancel()
        _timerState.value = TimerState.IDLE
        _timeRemaining.value = workDuration
    }
    
    fun setWorkDuration(minutes: Int) {
        workDuration = minutes * 60 * 1000L
        if (_timerState.value == TimerState.IDLE) {
            _timeRemaining.value = workDuration
        }
    }
    
    fun setShortBreakDuration(minutes: Int) {
        shortBreakDuration = minutes * 60 * 1000L
    }
    
    fun setLongBreakDuration(minutes: Int) {
        longBreakDuration = minutes * 60 * 1000L
    }
    
    fun setLongBreakAfterPomodoros(count: Int) {
        longBreakAfterPomodoros = count
    }
    
    private fun startTimer() {
        countDownTimer?.cancel()
        
        countDownTimer = object : CountDownTimer(_timeRemaining.value, TIMER_INTERVAL) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = millisUntilFinished
            }
            
            override fun onFinish() {
                if (_timerState.value == TimerState.WORK) {
                    // Completed a work session
                    _pomodoroCount.value++
                    
                    // Show notification
                    notificationHelper?.showPomodoroNotification(
                        title = "Pomodoro Completed",
                        message = "Great job! You've completed ${_pomodoroCount.value} pomodoros today."
                    )
                    
                    // Start break
                    startBreakTimer()
                } else {
                    // Break finished
                    // Show notification
                    notificationHelper?.showPomodoroNotification(
                        title = "Break Finished",
                        message = "Time to get back to work!"
                    )
                    
                    // Reset to idle state
                    _timerState.value = TimerState.IDLE
                    _timeRemaining.value = workDuration
                }
            }
        }.start()
    }
    
    override fun onCleared() {
        super.onCleared()
        countDownTimer?.cancel()
    }
    
    class PomodoroViewModelFactory : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(PomodoroViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return PomodoroViewModel() as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
