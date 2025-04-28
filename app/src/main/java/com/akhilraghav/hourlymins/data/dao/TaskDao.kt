package com.akhilraghav.hourlymins.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.akhilraghav.hourlymins.data.entities.Task
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface TaskDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: Task): Long
    
    @Update
    suspend fun update(task: Task)
    
    @Delete
    suspend fun delete(task: Task)
    
    @Query("SELECT * FROM tasks ORDER BY isCompleted ASC, priority DESC, dueDate ASC")
    fun getAllTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY priority DESC, dueDate ASC")
    fun getActiveTasks(): Flow<List<Task>>
    
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getCompletedTasks(): Flow<List<Task>>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 0")
    fun getActiveTaskCount(): Flow<Int>
    
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1 AND date(completedAt/1000, 'unixepoch', 'localtime') = date(:date/1000, 'unixepoch', 'localtime')")
    suspend fun getCompletedTasksForDate(date: Date): Int
    
    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun getTotalTaskCount(): Int
    
    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :completionDate WHERE id = :taskId")
    suspend fun markTaskAsCompleted(taskId: Long, completionDate: Date = Date())
}
