package com.example.activelife

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.room.Room
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import com.example.activelife.data.ActivityRepository
import com.example.activelife.data.AppDatabase
import com.example.activelife.sensors.DevicePostureSensor
import com.example.activelife.sensors.StepSensor
import com.example.activelife.services.ActivityForegroundService
import com.example.activelife.ui.ActivityViewModel
import com.example.activelife.ui.MainScreen
import com.example.activelife.ui.ViewModelFactory
import com.example.activelife.ui.theme.ActiveLifeTheme
import com.example.activelife.worker.SittingWorker

class MainActivity : ComponentActivity() {

    // 1. Permission Launcher for ACTIVITY_RECOGNITION (Step Counter)
    private val requestActivityPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Activity Tracking Enabled!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission required to count steps.", Toast.LENGTH_LONG).show()
            }
        }

    // 2. Permission Launcher for POST_NOTIFICATIONS (Android 13+)
    private val requestNotificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(this, "Notifications Enabled!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications disabled. You won't get alerts.", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // A. Setup Notification Channel
        createNotificationChannel()

        // B. Database & Repository Setup
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "activelife-db"
        ).build()

        val stepSensor = StepSensor(applicationContext)
        val repository = ActivityRepository(db.activityDao(), stepSensor, applicationContext)

        // C. Clean ViewModel Initialization
        val postureSensor = DevicePostureSensor(this@MainActivity)
        val factory = ViewModelFactory(repository, postureSensor)
        val owner: ViewModelStoreOwner = this@MainActivity
        val viewModel = ViewModelProvider(owner, factory)[ActivityViewModel::class.java]

        viewModel.startMonitoring()

        // D. Check Permissions & Start Services
        checkPermissions()
        val serviceIntent = Intent(this, ActivityForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        // E. Start the Sitting Reminder Background Engine
        val sittingWorkRequest = PeriodicWorkRequestBuilder<SittingWorker>(
            15, TimeUnit.MINUTES
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SittingReminderWork",
            ExistingPeriodicWorkPolicy.KEEP,
            sittingWorkRequest
        )

        // F. Launch UI
        setContent {
            ActiveLifeTheme {
                MainScreen(viewModel)
            }
        }
    }

    // --- Helper Functions ---
    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestActivityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ActiveLife Alerts"
            val descriptionText = "Reminders to stand up and move"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("SITTING_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}