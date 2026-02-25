package com.example.activelife.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.room.Room
import com.example.activelife.data.ActivityRepository
import com.example.activelife.data.AppDatabase
import com.example.activelife.sensors.StepSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ActivityForegroundService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private lateinit var repository: ActivityRepository
    private lateinit var stepSensor: StepSensor

    override fun onCreate() {
        super.onCreate()
        // We set up the Database and Sensor inside the Service so it survives when the UI dies
        val db = Room.databaseBuilder(applicationContext, AppDatabase::class.java, "activelife-db").build()
        stepSensor = StepSensor(applicationContext)
        repository = ActivityRepository(db.activityDao(), stepSensor, applicationContext)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        createNotificationChannel()

        // Android REQUIRES a notification to run a foreground service
        val notification = NotificationCompat.Builder(this, "ActiveLifeChannel")
            .setContentTitle("ActiveLife is Active")
            .setContentText("Counting steps in the background...")
            .setSmallIcon(android.R.drawable.ic_menu_compass) // Default Android icon
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(1, notification)

        // Start saving steps automatically in the background
        serviceScope.launch {
            stepSensor.steps.collect { currentSteps ->
                if (currentSteps > 0) {
                    repository.saveDailySteps(currentSteps)
                }
            }
        }

        // Keep Activity Recognition alive
        repository.startTracking()

        // START_STICKY tells Android to restart this service if it ever gets killed for memory
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // We don't need binding for this simple use case
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel() // Clean up coroutines to prevent memory leaks
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "ActiveLifeChannel",
                "Background Tracking",
                NotificationManager.IMPORTANCE_LOW // Low priority means it won't constantly buzz the user
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
}