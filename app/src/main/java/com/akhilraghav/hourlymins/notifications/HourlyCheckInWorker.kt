package com.akhilraghav.hourlymins.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.akhilraghav.hourlymins.data.AppDatabase
import com.akhilraghav.hourlymins.data.entities.DiaryEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.Date

/**
 * Worker class for handling hourly check-ins
 */
class HourlyCheckInWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            // Check if we're in active hours (e.g., 8 AM to 10 PM)
            val calendar = Calendar.getInstance()
            val hourOfDay = calendar.get(Calendar.HOUR_OF_DAY)
            
            // Only show notifications during active hours
            if (hourOfDay in 8..22) {
                val notificationHelper = NotificationHelper(context)
                notificationHelper.showHourlyCheckInNotification()
                
                // Check if previous hour has an entry, if not mark as wasted
                checkAndCreateMissedEntry()
            }
            
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    /**
     * Checks if the previous hour has an entry, if not creates a "missed" entry
     */
    private suspend fun checkAndCreateMissedEntry() {
        val database = AppDatabase.getDatabase(context)
        val diaryEntryDao = database.diaryEntryDao()
        
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.HOUR_OF_DAY, -1) // Previous hour
        
        // Set minutes, seconds, milliseconds to 0 to get the start of the previous hour
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val previousHourStart = calendar.time
        
        // Set minutes, seconds to 59, milliseconds to 999 to get the end of the previous hour
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        val previousHourEnd = calendar.time
        
        // Check if we have an entry for the previous hour
        val entries = diaryEntryDao.getEntriesBetweenDates(previousHourStart, previousHourEnd).firstOrNull()
        
        if (entries.isNullOrEmpty()) {
            // No entry for previous hour, create a "missed" entry
            val missedEntry = DiaryEntry(
                timestamp = previousHourStart,
                content = "Missed check-in",
                isProductiveHour = false,
                productivityRating = 1 // Lowest rating
            )
            diaryEntryDao.insert(missedEntry)
        }
    }
}
