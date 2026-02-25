package com.example.activelife.sensors

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.room.Room
import com.example.activelife.data.ActivityLog
import com.example.activelife.data.AppDatabase
import com.google.android.gms.location.ActivityTransitionResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ActivityTransitionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // 1. Check if the intent contains activity data
        Toast.makeText(context, "Activity Signal Received!", Toast.LENGTH_SHORT).show()

        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent) ?: return

            // 2. Setup Database (Manually, since we can't inject easily here)
            val db = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "activelife-db"
            ).build()

            // 3. Process the Event
            for (event in result.transitionEvents) {
                val activityType = when (event.activityType) {
                    com.google.android.gms.location.DetectedActivity.WALKING -> "WALKING"
                    com.google.android.gms.location.DetectedActivity.RUNNING -> "RUNNING"
                    com.google.android.gms.location.DetectedActivity.STILL -> "STILL"
                    else -> "UNKNOWN"
                }

                val transitionType = if (event.transitionType == com.google.android.gms.location.ActivityTransition.ACTIVITY_TRANSITION_ENTER) "ENTER" else "EXIT"

                Log.d("ActivityReceiver", "Detected: $activityType ($transitionType)")

                // 4. Save to Database (Run in background)
                // We only care when we ENTER a new state
                if (transitionType == "ENTER") {
                    CoroutineScope(Dispatchers.IO).launch {
                        db.activityDao().insertLog(
                            ActivityLog(activityType = activityType, timestamp = System.currentTimeMillis())
                        )
                    }
                }
            }
        }
    }
}