package com.example.activelife.data

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.work.WorkManager
import com.example.activelife.sensors.ActivityTransitionReceiver
import com.example.activelife.sensors.StepSensor
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.flow.Flow
import androidx.annotation.RequiresPermission
import android.Manifest
import java.time.LocalDate
import java.time.ZoneId

class ActivityRepository(
    private val activityDao: ActivityDao,
    private val stepSensor: StepSensor,
    private val context: Context
) {

    private val client = ActivityRecognition.getClient(context)
    private val workManager = WorkManager.getInstance(context)

    // FIX 1: The Sensor returns a Flow, not a StateFlow.
    // We pass the raw Flow to the ViewModel.
    val currentSteps: Flow<Int> = stepSensor.steps

    private val pendingIntent: PendingIntent by lazy {
        val intent = Intent(context, ActivityTransitionReceiver::class.java)
        PendingIntent.getBroadcast(
            context,
            100,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    // In data/ActivityRepository.kt

    fun startTracking() {
        val transitions = mutableListOf<ActivityTransition>()

        // We want to know when the user STARTS doing these things
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.WALKING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.STILL)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )
        transitions.add(
            ActivityTransition.Builder()
                .setActivityType(DetectedActivity.RUNNING)
                .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
                .build()
        )

        val request = ActivityTransitionRequest(transitions)

        // The "PendingIntent" is the message we send to the Receiver
        val intent = Intent(context, ActivityTransitionReceiver::class.java)
        intent.action = "com.example.activelife.ACTION_PROCESS_ACTIVITY_TRANSITIONS"

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            // IMMUTABLE is required for Android 12+ (API 31+)
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        val client = ActivityRecognition.getClient(context)

        client.requestActivityTransitionUpdates(request, pendingIntent)
            .addOnSuccessListener {
                // DEBUG: Success!
                Toast.makeText(context, "Tracking Started Successfully!", Toast.LENGTH_SHORT).show()
                Log.d("Repo", "Transition Updates Connected")
            }
            .addOnFailureListener { e ->
                // DEBUG: Failure! (This tells us WHY)
                Toast.makeText(context, "Tracking Failed: ${e.message}", Toast.LENGTH_LONG).show()
                Log.e("Repo", "Transition Failed", e)
            }
    }

    @RequiresPermission(Manifest.permission.ACTIVITY_RECOGNITION)
    fun stopTracking() {
        client.removeActivityUpdates(pendingIntent)
        workManager.cancelUniqueWork("SittingCheck")
        // FIX 3: Removed stepSensor.stopListening()
    }

    suspend fun saveDailySteps(steps: Int) {
        try {
            val today = LocalDate.now().toString()
            Log.d("DB_TEST", "Attempting to save $steps steps for date: $today")

            val entry = DailySteps(date = today, stepCount = steps)
            activityDao.insertDailySteps(entry)

            Log.d("DB_TEST", "SUCCESS: Saved to Database!")
        } catch (e: Exception) {
            Log.e("DB_TEST", "CRITICAL ERROR saving steps: ${e.message}", e)
        }
    }

    // In data/ActivityRepository.kt

    suspend fun getLogsSince(startTime: Long): List<ActivityLog> {
        return activityDao.getLogsSince(startTime)
    }
    // In ActivityRepository.kt
    fun getLastLogFlow() = activityDao.getLastLogFlow()
    // In data/ActivityRepository.kt

    fun getWeeklySteps() = activityDao.getLast7Days()

    fun getRecentLogsFlow() = activityDao.getRecentLogsFlow()
}