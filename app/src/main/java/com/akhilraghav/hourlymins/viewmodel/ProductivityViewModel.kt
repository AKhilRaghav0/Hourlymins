package com.akhilraghav.hourlymins.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akhilraghav.hourlymins.data.entities.ProductivityRecord
import com.akhilraghav.hourlymins.data.repository.DiaryRepository
import com.akhilraghav.hourlymins.data.repository.ProductivityRepository
import com.akhilraghav.hourlymins.data.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class ProductivityViewModel(
    private val productivityRepository: ProductivityRepository,
    private val diaryRepository: DiaryRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {
    
    // All productivity records
    val allRecords = productivityRepository.allRecords
    
    // Selected date for viewing productivity
    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()
    
    // Productivity record for the selected date
    private val _recordForSelectedDate = MutableStateFlow<ProductivityRecord?>(null)
    val recordForSelectedDate: StateFlow<ProductivityRecord?> = _recordForSelectedDate.asStateFlow()
    
    // Weekly productivity stats
    private val _weeklyStats = MutableStateFlow<List<ProductivityRecord>>(emptyList())
    val weeklyStats: StateFlow<List<ProductivityRecord>> = _weeklyStats.asStateFlow()
    
    // Monthly productivity stats
    private val _monthlyStats = MutableStateFlow<List<ProductivityRecord>>(emptyList())
    val monthlyStats: StateFlow<List<ProductivityRecord>> = _monthlyStats.asStateFlow()
    
    init {
        loadRecordForSelectedDate()
        loadWeeklyStats()
        loadMonthlyStats()
    }
    
    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
        loadRecordForSelectedDate()
    }
    
    private fun loadRecordForSelectedDate() {
        viewModelScope.launch {
            _recordForSelectedDate.value = productivityRepository.getRecordForDate(_selectedDate.value)
        }
    }
    
    private fun loadWeeklyStats() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.DAY_OF_YEAR, -7)
            val weekAgo = calendar.time
            
            productivityRepository.getRecordsBetweenDates(weekAgo, Date()).collect {
                _weeklyStats.value = it
            }
        }
    }
    
    private fun loadMonthlyStats() {
        viewModelScope.launch {
            val calendar = Calendar.getInstance()
            calendar.time = Date()
            calendar.add(Calendar.MONTH, -1)
            val monthAgo = calendar.time
            
            productivityRepository.getRecordsBetweenDates(monthAgo, Date()).collect {
                _monthlyStats.value = it
            }
        }
    }
    
    suspend fun createDailyProductivityRecord(date: Date = Date(), notes: String? = null, overallRating: Int) {
        // Get productive and wasted hours
        val productiveHours = diaryRepository.getProductiveHoursForDate(date)
        val wastedHours = diaryRepository.getWastedHoursForDate(date)
        
        // Get task counts
        val completedTasks = taskRepository.getCompletedTasksForDate(date)
        val totalTasks = taskRepository.getTotalTaskCount()
        
        val record = ProductivityRecord(
            date = date,
            productiveHours = productiveHours,
            wastedHours = wastedHours,
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            overallRating = overallRating,
            notes = notes
        )
        
        productivityRepository.insert(record)
        loadRecordForSelectedDate()
    }
    
    fun updateProductivityRecord(record: ProductivityRecord) {
        viewModelScope.launch {
            productivityRepository.update(record)
            loadRecordForSelectedDate()
        }
    }
    
    fun deleteProductivityRecord(record: ProductivityRecord) {
        viewModelScope.launch {
            productivityRepository.delete(record)
            loadRecordForSelectedDate()
        }
    }
    
    fun getRecordsBetweenDates(startDate: Date, endDate: Date): Flow<List<ProductivityRecord>> {
        return productivityRepository.getRecordsBetweenDates(startDate, endDate)
    }
    
    suspend fun getAverageProductiveHours(startDate: Date, endDate: Date): Float {
        return productivityRepository.getAverageProductiveHours(startDate, endDate)
    }
    
    suspend fun getAverageProductivityRating(startDate: Date, endDate: Date): Float {
        return productivityRepository.getAverageProductivityRating(startDate, endDate)
    }
    
    class ProductivityViewModelFactory(
        private val productivityRepository: ProductivityRepository,
        private val diaryRepository: DiaryRepository,
        private val taskRepository: TaskRepository
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductivityViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductivityViewModel(productivityRepository, diaryRepository, taskRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
