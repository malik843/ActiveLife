package com.example.activelife.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ActivityLog::class, DailySteps::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
}