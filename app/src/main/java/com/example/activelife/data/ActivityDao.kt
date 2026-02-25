package com.example.activelife.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: ActivityLog): Long

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastLog(): ActivityLog?

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 1")
    fun getLastLogFlow(): Flow<ActivityLog?>

    @Query("SELECT * FROM activity_logs WHERE timestamp >= :startTime")
    suspend fun getLogsSince(startTime: Long): List<ActivityLog>

    @Query("SELECT * FROM activity_logs ORDER BY timestamp DESC LIMIT 20")
    fun getRecentLogsFlow(): Flow<List<ActivityLog>>



    @Insert(onConflict = androidx.room.OnConflictStrategy.REPLACE)
    suspend fun insertDailySteps(dailySteps: DailySteps)

    @Query("SELECT * FROM daily_steps WHERE date = :date")
    suspend fun getStepsForDate(date: String): DailySteps?

    @Query("SELECT SUM(stepCount) FROM daily_steps")
    suspend fun getTotalSteps(): Long

    @Query("SELECT * FROM daily_steps ORDER BY date DESC LIMIT 7")
    fun getLast7Days(): Flow<List<DailySteps>>
}