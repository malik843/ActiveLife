package com.example.activelife.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import com.example.activelife.data.ActivityLog
import com.example.activelife.data.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ActivityViewModel(private val repository: ActivityRepository) : ViewModel() {

    // =================================================================================
    // PART 1: LIVE SENSOR DATA (Kept from your previous version)
    // =================================================================================

    // 1. Current Status (Sitting/Walking) - Derived from Database Logs
    val currentStatus: StateFlow<String> = repository.getLastLogFlow()
        .map { log -> log?.activityType ?: "STILL" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "STILL")

    // 2. Live Steps - Derived from Sensor
    val steps: StateFlow<Int> = repository.currentSteps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // 3. Calculated Metrics (Math logic preserved)
    val calories: StateFlow<Int> = steps.map { (it * 0.04).toInt() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val distance: StateFlow<Double> = steps.map { (it * 0.762) / 1000 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val speed: StateFlow<Double> = currentStatus.map { status ->
        when (status) {
            "WALKING" -> 4.0
            "RUNNING" -> 8.0
            else -> 0.0
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val todayLogs: StateFlow<List<ActivityLog>> = repository.getRecentLogsFlow()
        .map {
            // Query for logs since midnight
            val midnight = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            repository.getLogsSince(midnight)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())


    // =================================================================================
    // PART 2: NEW CHART DATA (The only new addition)
    // =================================================================================

    // Logic: Get DB data -> Format it for the Bar Chart
    val weeklyData: StateFlow<List<Pair<String, Int>>> = repository.getWeeklySteps()
        .map { historyList ->
            val result = mutableListOf<Pair<String, Int>>()
            val today = LocalDate.now()

            // Generate the last 7 days labels (e.g. "Mon", "Tue")
            for (i in 6 downTo 0) {
                val dateToCheck = today.minusDays(i.toLong())
                val dateString = dateToCheck.toString() // "2026-02-17"

                // If DB has data for this date, use it. Otherwise, 0.
                val steps = historyList.find { it.date == dateString }?.stepCount ?: 0
                val dayLabel = dateToCheck.format(DateTimeFormatter.ofPattern("EEE"))

                result.add(Pair(dayLabel, steps))
            }
            result
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =================================================================================
    // PART 3: ACTIONS
    // =================================================================================

    fun startMonitoring() {
        repository.startTracking()
    }
}

// Factory remains exactly the same
class ViewModelFactory(private val repository: ActivityRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivityViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}