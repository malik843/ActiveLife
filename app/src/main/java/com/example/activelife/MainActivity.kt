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
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.activelife.data.ActivityRepository
import com.example.activelife.data.AppDatabase
import com.example.activelife.sensors.StepSensor
import com.example.activelife.services.ActivityForegroundService
import com.example.activelife.ui.ActivityViewModel
import com.example.activelife.ui.MainScreen
import com.example.activelife.ui.theme.ActiveLifeTheme

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

        // A. Setup Notification Channel (Crucial for alerts to work)
        createNotificationChannel()

        // B. Database & Repository Setup
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "activelife-db"
        ).build()

        val stepSensor = StepSensor(applicationContext)
        val repository = ActivityRepository(db.activityDao(), stepSensor, applicationContext)

        val viewModelFactory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(ActivityViewModel::class.java)) {
                    @Suppress("UNCHECKED_CAST")
                    return ActivityViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }

        val viewModel: ActivityViewModel by viewModels { viewModelFactory }

        viewModel.startMonitoring()

        // C. Check Permissions on Launch
        checkPermissions()
        val serviceIntent = Intent(this, ActivityForegroundService::class.java)
        ContextCompat.startForegroundService(this, serviceIntent)

        setContent {
            ActiveLifeTheme {
                MainScreen(viewModel)
            }
        }
    }

    // --- Helper Functions ---

    private fun checkPermissions() {
        // 1. Check Physical Activity (Android 10+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACTIVITY_RECOGNITION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestActivityPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
        }

        // 2. Check Notifications (Android 13+)
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
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "ActiveLife Alerts"
            val descriptionText = "Reminders to stand up and move"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("SITTING_CHANNEL_ID", name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}