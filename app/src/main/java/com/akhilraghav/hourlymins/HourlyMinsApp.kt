package com.akhilraghav.hourlymins

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.work.Configuration
import androidx.work.WorkManager
import com.akhilraghav.hourlymins.data.AppDatabase
import com.akhilraghav.hourlymins.notifications.NotificationHelper

class HourlyMinsApp : Application(), Configuration.Provider {
    
    val database by lazy { AppDatabase.getDatabase(this) }
    
    override fun onCreate() {
        super.onCreate()
        
        // Create notification channels
        createNotificationChannels()
        
        // Initialize WorkManager
        WorkManager.getInstance(this)
    }
    
    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create hourly check-in channel
            val hourlyChannel = NotificationChannel(
                NotificationHelper.HOURLY_CHANNEL_ID,
                "Hourly Check-ins",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for hourly check-ins"
                enableVibration(true)
            }
            
            // Create pomodoro channel
            val pomodoroChannel = NotificationChannel(
                NotificationHelper.POMODORO_CHANNEL_ID,
                "Pomodoro Timer",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for pomodoro timer"
                enableVibration(true)
            }
            
            // Create todo reminder channel
            val todoChannel = NotificationChannel(
                NotificationHelper.TODO_CHANNEL_ID,
                "Todo Reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for todo reminders"
            }
            
            // Register the channels
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannels(listOf(hourlyChannel, pomodoroChannel, todoChannel))
        }
    }
    
    override fun getWorkManagerConfiguration(): Configuration {
        return Configuration.Builder()
            .setMinimumLoggingLevel(android.util.Log.INFO)
            .build()
    }
}
