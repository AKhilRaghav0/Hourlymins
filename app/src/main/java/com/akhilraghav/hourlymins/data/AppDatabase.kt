package com.akhilraghav.hourlymins.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.akhilraghav.hourlymins.data.converters.DateConverter
import com.akhilraghav.hourlymins.data.dao.DiaryEntryDao
import com.akhilraghav.hourlymins.data.dao.ProductivityDao
import com.akhilraghav.hourlymins.data.dao.TaskDao
import com.akhilraghav.hourlymins.data.entities.DiaryEntry
import com.akhilraghav.hourlymins.data.entities.ProductivityRecord
import com.akhilraghav.hourlymins.data.entities.Task

@Database(
    entities = [DiaryEntry::class, Task::class, ProductivityRecord::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun taskDao(): TaskDao
    abstract fun productivityDao(): ProductivityDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hourlymins_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
