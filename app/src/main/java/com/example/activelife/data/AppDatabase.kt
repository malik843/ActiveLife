package com.example.activelife.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [ActivityLog::class, DailySteps::class, NotificationEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
}