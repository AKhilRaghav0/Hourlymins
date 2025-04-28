package com.akhilraghav.hourlymins.data.repository

import com.akhilraghav.hourlymins.data.dao.TaskDao
import com.akhilraghav.hourlymins.data.entities.Task
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository for handling task operations
 */
class TaskRepository(private val taskDao: TaskDao) {
    
    val allTasks: Flow<List<Task>> = taskDao.getAllTasks()
    val activeTasks: Flow<List<Task>> = taskDao.getActiveTasks()
    val completedTasks: Flow<List<Task>> = taskDao.getCompletedTasks()
    val activeTaskCount: Flow<Int> = taskDao.getActiveTaskCount()
    
    suspend fun insert(task: Task): Long {
        return taskDao.insert(task)
    }
    
    suspend fun update(task: Task) {
        taskDao.update(task)
    }
    
    suspend fun delete(task: Task) {
        taskDao.delete(task)
    }
    
    suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskById(id)
    }
    
    suspend fun markTaskAsCompleted(taskId: Long, completionDate: Date = Date()) {
        taskDao.markTaskAsCompleted(taskId, completionDate)
    }
    
    suspend fun getCompletedTasksForDate(date: Date): Int {
        return taskDao.getCompletedTasksForDate(date)
    }
    
    suspend fun getTotalTaskCount(): Int {
        return taskDao.getTotalTaskCount()
    }
}
