package com.akhilraghav.hourlymins.data.repository

import com.akhilraghav.hourlymins.data.dao.ProductivityDao
import com.akhilraghav.hourlymins.data.entities.ProductivityRecord
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository for handling productivity record operations
 */
class ProductivityRepository(private val productivityDao: ProductivityDao) {
    
    val allRecords: Flow<List<ProductivityRecord>> = productivityDao.getAllRecords()
    
    suspend fun insert(productivityRecord: ProductivityRecord): Long {
        return productivityDao.insert(productivityRecord)
    }
    
    suspend fun update(productivityRecord: ProductivityRecord) {
        productivityDao.update(productivityRecord)
    }
    
    suspend fun delete(productivityRecord: ProductivityRecord) {
        productivityDao.delete(productivityRecord)
    }
    
    suspend fun getRecordById(id: Long): ProductivityRecord? {
        return productivityDao.getRecordById(id)
    }
    
    suspend fun getRecordForDate(date: Date): ProductivityRecord? {
        return productivityDao.getRecordForDate(date)
    }
    
    fun getRecordsBetweenDates(startDate: Date, endDate: Date): Flow<List<ProductivityRecord>> {
        return productivityDao.getRecordsBetweenDates(startDate, endDate)
    }
    
    suspend fun getAverageProductiveHours(startDate: Date, endDate: Date): Float {
        return productivityDao.getAverageProductiveHours(startDate, endDate)
    }
    
    suspend fun getAverageProductivityRating(startDate: Date, endDate: Date): Float {
        return productivityDao.getAverageProductivityRating(startDate, endDate)
    }
}
