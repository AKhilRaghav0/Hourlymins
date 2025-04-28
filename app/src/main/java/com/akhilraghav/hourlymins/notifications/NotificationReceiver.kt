package com.akhilraghav.hourlymins.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Broadcast receiver for handling notification events and scheduling
 */
class NotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Reschedule notifications after device reboot
            scheduleHourlyNotifications(context)
        }
    }
    
    companion object {
        private const val HOURLY_WORK_NAME = "hourly_check_in_work"
        
        /**
         * Schedules hourly notifications using WorkManager
         */
        fun scheduleHourlyNotifications(context: Context) {
            val hourlyWorkRequest = PeriodicWorkRequestBuilder<HourlyCheckInWorker>(
                1, TimeUnit.HOURS
            ).build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                HOURLY_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                hourlyWorkRequest
            )
        }
        
        /**
         * Cancels scheduled hourly notifications
         */
        fun cancelHourlyNotifications(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(HOURLY_WORK_NAME)
        }
    }
}
