package com.example.activelife.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_steps")
data class DailySteps(
    @PrimaryKey val date: String, // Format: "2026-02-03"
    val stepCount: Int
)