package com.akhilraghav.hourlymins.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.akhilraghav.hourlymins.data.entities.Task
import com.akhilraghav.hourlymins.data.repository.TaskRepository
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Date

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {
    
    // All tasks
    val allTasks = repository.allTasks
    
    // Active tasks
    val activeTasks = repository.activeTasks
    
    // Completed tasks
    val completedTasks = repository.completedTasks
    
    // Active task count
    val activeTaskCount = repository.activeTaskCount
    
    fun createTask(title: String, description: String? = null, dueDate: Date? = null, priority: Int = 0) {
        viewModelScope.launch {
            val task = Task(
                title = title,
                description = description,
                dueDate = dueDate,
                priority = priority,
                isCompleted = false,
                createdAt = Date()
            )
            repository.insert(task)
        }
    }
    
    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.update(task)
        }
    }
    
    fun deleteTask(task: Task) {
        viewModelScope.launch {
            repository.delete(task)
        }
    }
    
    fun markTaskAsCompleted(taskId: Long) {
        viewModelScope.launch {
            repository.markTaskAsCompleted(taskId)
        }
    }
    
    fun getTaskById(id: Long, callback: (Task?) -> Unit) {
        viewModelScope.launch {
            val task = repository.getTaskById(id)
            callback(task)
        }
    }
    
    suspend fun getCompletedTasksForDate(date: Date): Int {
        return repository.getCompletedTasksForDate(date)
    }
    
    suspend fun getTotalTaskCount(): Int {
        return repository.getTotalTaskCount()
    }
    
    class TaskViewModelFactory(private val repository: TaskRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(TaskViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return TaskViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
