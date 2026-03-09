package com.example.activelife.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import com.example.activelife.data.ActivityLog
import com.example.activelife.data.ActivityRepository
import com.example.activelife.data.NotificationEntity
import com.example.activelife.sensors.DevicePostureSensor
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class ActivityViewModel(
    private val repository: ActivityRepository,
    private val postureSensor: DevicePostureSensor
) : ViewModel() {

    // =================================================================================
    // LIVE POSTURE SENSOR (This is what was missing!)
    // =================================================================================

    // These two lines MUST be right here at the top level of the class
    private val _livePosture = MutableStateFlow("STILL")
    val livePosture: StateFlow<String> = _livePosture.asStateFlow()

    init {
        viewModelScope.launch {
            postureSensor.liveState.collect { state ->
                _livePosture.value = state
            }
        }

        viewModelScope.launch {
            repository.currentSteps.collect { currentStepCount ->
                if (currentStepCount > 0) {
                    repository.saveDailySteps(currentStepCount)
                }
            }
        }
    }



    // =================================================================================
    // OTHER SENSOR DATA & CALCULATIONS
    // =================================================================================

    val currentStatus: StateFlow<String> = repository.getLastLogFlow()
        .map { log -> log?.activityType ?: "STILL" }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "STILL")

    val steps: StateFlow<Int> = repository.currentSteps
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

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

    val notifications: StateFlow<List<com.example.activelife.data.NotificationEntity>> = repository.getAllNotificationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val todayLogs: StateFlow<List<ActivityLog>> = repository.getRecentLogsFlow()
        .map {
            val midnight = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            repository.getLogsSince(midnight)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val minuteTicker = kotlinx.coroutines.flow.flow {
        while (true) {
            emit(System.currentTimeMillis())
            kotlinx.coroutines.delay(60000) // tick every 1 minute
        }
    }

    // 2. The accurate sitting time calculator
    val sittingTimeFormatted: StateFlow<String> = kotlinx.coroutines.flow.combine(todayLogs, minuteTicker) { logs, currentTime ->
        var totalSittingMillis = 0L
        val sortedLogs = logs.sortedBy { it.timestamp }

        for (i in sortedLogs.indices) {
            val currentLog = sortedLogs[i]
            if (currentLog.activityType == "STILL") {
                // If it's STILL, calculate time until the next status change (or until RIGHT NOW if they are currently sitting)
                val nextTimestamp = if (i + 1 < sortedLogs.size) {
                    sortedLogs[i+1].timestamp
                } else {
                    currentTime
                }
                totalSittingMillis += (nextTimestamp - currentLog.timestamp)
            }
        }

        val hours = (totalSittingMillis / (1000 * 60 * 60)).toInt()
        val minutes = ((totalSittingMillis / (1000 * 60)) % 60).toInt()

        if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "0m")

    // =================================================================================
    // WEEKLY CHART DATA
    // =================================================================================

    val weeklyData: StateFlow<List<Pair<String, Int>>> = repository.getWeeklySteps()
        .map { historyList ->
            val result = mutableListOf<Pair<String, Int>>()
            val today = LocalDate.now()

            for (i in 6 downTo 0) {
                val dateToCheck = today.minusDays(i.toLong())
                val dateString = dateToCheck.toString()

                val steps = historyList.find { it.date == dateString }?.stepCount ?: 0
                val dayLabel = dateToCheck.format(DateTimeFormatter.ofPattern("EEE"))

                result.add(Pair(dayLabel, steps))
            }
            result
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // =================================================================================
    // ACTIONS
    // =================================================================================

    fun startMonitoring() {
        repository.startTracking()
    }
}

// =================================================================================
// VIEW MODEL FACTORY
// =================================================================================

class ViewModelFactory(
    private val repository: ActivityRepository,
    private val postureSensor: DevicePostureSensor
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ActivityViewModel(repository, postureSensor) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}