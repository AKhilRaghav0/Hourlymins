package com.akhilraghav.hourlymins.notifications

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.akhilraghav.hourlymins.MainActivity
import com.akhilraghav.hourlymins.R

/**
 * Helper class for creating and showing notifications
 */
class NotificationHelper(private val context: Context) {
    
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    
    /**
     * Shows an hourly check-in notification
     */
    fun showHourlyCheckInNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTIFICATION_TYPE, NOTIFICATION_TYPE_HOURLY)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            HOURLY_NOTIFICATION_ID, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, HOURLY_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Hourly Check-in")
            .setContentText("What did you accomplish in the last hour?")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_check,
                "Log Activity",
                pendingIntent
            )
        
        notificationManager.notify(HOURLY_NOTIFICATION_ID, builder.build())
    }
    
    /**
     * Shows a pomodoro timer notification
     */
    fun showPomodoroNotification(title: String, message: String) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTIFICATION_TYPE, NOTIFICATION_TYPE_POMODORO)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            POMODORO_NOTIFICATION_ID, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, POMODORO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
        
        notificationManager.notify(POMODORO_NOTIFICATION_ID, builder.build())
    }
    
    /**
     * Shows a todo reminder notification
     */
    fun showTodoReminderNotification(taskTitle: String, taskId: Long) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra(EXTRA_NOTIFICATION_TYPE, NOTIFICATION_TYPE_TODO)
            putExtra(EXTRA_TASK_ID, taskId)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            TODO_NOTIFICATION_ID + taskId.toInt(), 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        val builder = NotificationCompat.Builder(context, TODO_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Task Reminder")
            .setContentText(taskTitle)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .addAction(
                R.drawable.ic_check,
                "Mark Complete",
                pendingIntent
            )
        
        notificationManager.notify(TODO_NOTIFICATION_ID + taskId.toInt(), builder.build())
    }
    
    companion object {
        // Channel IDs
        const val HOURLY_CHANNEL_ID = "hourly_checkins"
        const val POMODORO_CHANNEL_ID = "pomodoro_timer"
        const val TODO_CHANNEL_ID = "todo_reminders"
        
        // Notification IDs
        const val HOURLY_NOTIFICATION_ID = 1001
        const val POMODORO_NOTIFICATION_ID = 2001
        const val TODO_NOTIFICATION_ID = 3001
        
        // Notification types
        const val NOTIFICATION_TYPE_HOURLY = "hourly"
        const val NOTIFICATION_TYPE_POMODORO = "pomodoro"
        const val NOTIFICATION_TYPE_TODO = "todo"
        
        // Intent extras
        const val EXTRA_NOTIFICATION_TYPE = "notification_type"
        const val EXTRA_TASK_ID = "task_id"
    }
}
