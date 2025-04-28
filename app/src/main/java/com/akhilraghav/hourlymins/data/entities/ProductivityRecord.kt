package com.akhilraghav.hourlymins.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a daily productivity record
 */
@Entity(tableName = "productivity_records")
data class ProductivityRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Date,
    val productiveHours: Int,
    val wastedHours: Int,
    val totalTasks: Int,
    val completedTasks: Int,
    val overallRating: Int, // 1-5 rating
    val notes: String? = null
)
