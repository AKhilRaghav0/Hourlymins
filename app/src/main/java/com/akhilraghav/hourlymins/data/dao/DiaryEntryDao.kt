package com.akhilraghav.hourlymins.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akhilraghav.hourlymins.data.entities.DiaryEntry
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface DiaryEntryDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(diaryEntry: DiaryEntry): Long
    
    @Update
    suspend fun update(diaryEntry: DiaryEntry)
    
    @Delete
    suspend fun delete(diaryEntry: DiaryEntry)
    
    @Query("SELECT * FROM diary_entries ORDER BY timestamp DESC")
    fun getAllEntries(): Flow<List<DiaryEntry>>
    
    @Query("SELECT * FROM diary_entries WHERE id = :id")
    suspend fun getEntryById(id: Long): DiaryEntry?
    
    @Query("SELECT * FROM diary_entries WHERE timestamp BETWEEN :startDate AND :endDate ORDER BY timestamp DESC")
    fun getEntriesBetweenDates(startDate: Date, endDate: Date): Flow<List<DiaryEntry>>
    
    @Query("SELECT * FROM diary_entries WHERE date(timestamp/1000, 'unixepoch', 'localtime') = date(:date/1000, 'unixepoch', 'localtime') ORDER BY timestamp DESC")
    fun getEntriesForDate(date: Date): Flow<List<DiaryEntry>>
    
    @Query("SELECT COUNT(*) FROM diary_entries WHERE isProductiveHour = 1 AND date(timestamp/1000, 'unixepoch', 'localtime') = date(:date/1000, 'unixepoch', 'localtime')")
    suspend fun getProductiveHoursForDate(date: Date): Int
    
    @Query("SELECT COUNT(*) FROM diary_entries WHERE isProductiveHour = 0 AND date(timestamp/1000, 'unixepoch', 'localtime') = date(:date/1000, 'unixepoch', 'localtime')")
    suspend fun getWastedHoursForDate(date: Date): Int
}
