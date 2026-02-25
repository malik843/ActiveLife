package com.example.activelife.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "activity_logs")
data class ActivityLog(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val activityType: String,
    val timestamp: Long = System.currentTimeMillis()
)