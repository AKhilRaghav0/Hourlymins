package com.akhilraghav.hourlymins.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akhilraghav.hourlymins.data.entities.DiaryEntry
import com.akhilraghav.hourlymins.data.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date

class DiaryViewModel(private val repository: DiaryRepository) : ViewModel() {
    
    // All diary entries
    val allEntries = repository.allEntries
    
    // Selected date for viewing entries
    private val _selectedDate = MutableStateFlow(Date())
    val selectedDate: StateFlow<Date> = _selectedDate.asStateFlow()
    
    // Entries for the selected date
    private val _entriesForSelectedDate = MutableStateFlow<List<DiaryEntry>>(emptyList())
    val entriesForSelectedDate: StateFlow<List<DiaryEntry>> = _entriesForSelectedDate.asStateFlow()
    
    // Current entry being edited
    private val _currentEntry = MutableStateFlow<DiaryEntry?>(null)
    val currentEntry: StateFlow<DiaryEntry?> = _currentEntry.asStateFlow()
    
    init {
        loadEntriesForSelectedDate()
    }
    
    fun setSelectedDate(date: Date) {
        _selectedDate.value = date
        loadEntriesForSelectedDate()
    }
    
    private fun loadEntriesForSelectedDate() {
        viewModelScope.launch {
            repository.getEntriesForDate(_selectedDate.value).collect {
                _entriesForSelectedDate.value = it
            }
        }
    }
    
    fun createNewEntry(content: String, isProductiveHour: Boolean, productivityRating: Int, tags: String? = null) {
        viewModelScope.launch {
            val entry = DiaryEntry(
                timestamp = Date(),
                content = content,
                isProductiveHour = isProductiveHour,
                productivityRating = productivityRating,
                tags = tags
            )
            repository.insert(entry)
        }
    }
    
    fun updateEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.update(entry)
        }
    }
    
    fun deleteEntry(entry: DiaryEntry) {
        viewModelScope.launch {
            repository.delete(entry)
        }
    }
    
    fun getEntryById(id: Long) {
        viewModelScope.launch {
            _currentEntry.value = repository.getEntryById(id)
        }
    }
    
    fun getEntriesForDateRange(startDate: Date, endDate: Date): Flow<List<DiaryEntry>> {
        return repository.getEntriesBetweenDates(startDate, endDate)
    }
    
    fun clearCurrentEntry() {
        _currentEntry.value = null
    }
    
    suspend fun getProductiveHoursForDate(date: Date): Int {
        return repository.getProductiveHoursForDate(date)
    }
    
    suspend fun getWastedHoursForDate(date: Date): Int {
        return repository.getWastedHoursForDate(date)
    }
    
    class DiaryViewModelFactory(private val repository: DiaryRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DiaryViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DiaryViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
