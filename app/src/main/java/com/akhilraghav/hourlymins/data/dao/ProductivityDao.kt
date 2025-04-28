package com.akhilraghav.hourlymins.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akhilraghav.hourlymins.data.entities.ProductivityRecord
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface ProductivityDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(productivityRecord: ProductivityRecord): Long
    
    @Update
    suspend fun update(productivityRecord: ProductivityRecord)
    
    @Delete
    suspend fun delete(productivityRecord: ProductivityRecord)
    
    @Query("SELECT * FROM productivity_records ORDER BY date DESC")
    fun getAllRecords(): Flow<List<ProductivityRecord>>
    
    @Query("SELECT * FROM productivity_records WHERE id = :id")
    suspend fun getRecordById(id: Long): ProductivityRecord?
    
    @Query("SELECT * FROM productivity_records WHERE date(date/1000, 'unixepoch', 'localtime') = date(:date/1000, 'unixepoch', 'localtime')")
    suspend fun getRecordForDate(date: Date): ProductivityRecord?
    
    @Query("SELECT * FROM productivity_records WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getRecordsBetweenDates(startDate: Date, endDate: Date): Flow<List<ProductivityRecord>>
    
    @Query("SELECT AVG(productiveHours) FROM productivity_records WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageProductiveHours(startDate: Date, endDate: Date): Float
    
    @Query("SELECT AVG(overallRating) FROM productivity_records WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getAverageProductivityRating(startDate: Date, endDate: Date): Float
}
