package com.akhilraghav.hourlymins.data.repository

import com.akhilraghav.hourlymins.data.dao.DiaryEntryDao
import com.akhilraghav.hourlymins.data.entities.DiaryEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository for handling diary entry operations
 */
class DiaryRepository(private val diaryEntryDao: DiaryEntryDao) {
    
    val allEntries: Flow<List<DiaryEntry>> = diaryEntryDao.getAllEntries()
    
    suspend fun insert(diaryEntry: DiaryEntry): Long {
        return diaryEntryDao.insert(diaryEntry)
    }
    
    suspend fun update(diaryEntry: DiaryEntry) {
        diaryEntryDao.update(diaryEntry)
    }
    
    suspend fun delete(diaryEntry: DiaryEntry) {
        diaryEntryDao.delete(diaryEntry)
    }
    
    suspend fun getEntryById(id: Long): DiaryEntry? {
        return diaryEntryDao.getEntryById(id)
    }
    
    fun getEntriesForDate(date: Date): Flow<List<DiaryEntry>> {
        return diaryEntryDao.getEntriesForDate(date)
    }
    
    fun getEntriesBetweenDates(startDate: Date, endDate: Date): Flow<List<DiaryEntry>> {
        return diaryEntryDao.getEntriesBetweenDates(startDate, endDate)
    }
    
    suspend fun getProductiveHoursForDate(date: Date): Int {
        return diaryEntryDao.getProductiveHoursForDate(date)
    }
    
    suspend fun getWastedHoursForDate(date: Date): Int {
        return diaryEntryDao.getWastedHoursForDate(date)
    }
}
