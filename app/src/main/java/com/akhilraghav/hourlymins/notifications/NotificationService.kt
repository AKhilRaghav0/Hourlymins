package com.akhilraghav.hourlymins.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.akhilraghav.hourlymins.MainActivity
import com.akhilraghav.hourlymins.R

/**
 * Service for handling all app notifications
 */
class NotificationService(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    /**
     * Show a notification when a Pomodoro work session is complete
     */
    fun showPomodoroWorkCompleteNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            POMODORO_WORK_NOTIFICATION_ID, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NotificationHelper.POMODORO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Work Session Complete!")
            .setContentText("Time to take a break. Good job!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(POMODORO_WORK_NOTIFICATION_ID, notification)
    }
    
    /**
     * Show a notification when a Pomodoro break is complete
     */
    fun showPomodoroBreakCompleteNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            POMODORO_BREAK_NOTIFICATION_ID, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NotificationHelper.POMODORO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Break Complete!")
            .setContentText("Time to get back to work!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(POMODORO_BREAK_NOTIFICATION_ID, notification)
    }
    
    /**
     * Show a notification for hourly check-ins
     */
    fun showHourlyCheckInNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            HOURLY_CHECKIN_NOTIFICATION_ID, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NotificationHelper.HOURLY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hourly Check-in")
            .setContentText("How productive have you been this hour?")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(HOURLY_CHECKIN_NOTIFICATION_ID, notification)
    }
    
    /**
     * Show a notification for task reminders
     */
    fun showTaskReminderNotification(taskName: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            TASK_REMINDER_NOTIFICATION_ID, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, NotificationHelper.TODO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Task Reminder")
            .setContentText("Don't forget: $taskName")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        notificationManager.notify(TASK_REMINDER_NOTIFICATION_ID, notification)
    }
    
    companion object {
        const val POMODORO_WORK_NOTIFICATION_ID = 1001
        const val POMODORO_BREAK_NOTIFICATION_ID = 1002
        const val HOURLY_CHECKIN_NOTIFICATION_ID = 1003
        const val TASK_REMINDER_NOTIFICATION_ID = 1004
    }
}
