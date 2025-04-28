package com.akhilraghav.hourlymins.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Entity representing a diary entry for an hour
 */
@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Date,
    val content: String,
    val isProductiveHour: Boolean,
    val productivityRating: Int, // 1-5 rating
    val tags: String? = null // Comma-separated tags
)
