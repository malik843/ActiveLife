package com.example.activelife.worker

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.room.Room
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.activelife.MainActivity
import com.example.activelife.R
import com.example.activelife.data.AppDatabase

class SittingWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val database = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "activelife-db"
        ).build()

        val lastLog = database.activityDao().getLastLog()

        // logic: If last known state is STILL...
        if (lastLog != null && lastLog.activityType == "STILL") {

            // ...and it started more than 2 hours ago (Using 15 seconds for testing)
            // val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
            val testTimeAgo = System.currentTimeMillis() - (15 * 1000)

            if (lastLog.timestamp < testTimeAgo) {
                sendNotification()

                database.activityDao().insertNotification(
                    com.example.activelife.data.NotificationEntity(
                        title = "🚶 Stand up now",
                        message = "You've been inactive for 2 hours. Time to stretch!",
                        isWarning = true
                    )
                )
            }
        }

        return Result.success()
    }

    private fun sendNotification() {
        val channelId = "SITTING_CHANNEL_ID"

        // 1. Android 8.0+ REQUIRES a Notification Channel to be created first
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Health Nudges",
                NotificationManager.IMPORTANCE_HIGH // High importance makes it pop up on screen
            )
            val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        // 2. Check Permissions
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("SittingWorker", "Notification permission not granted!")
            return
        }

        // 3. Make the notification clickable so it opens your app
        val intent = Intent(applicationContext, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Build the Notification matching the UI Redesign specs
        val builder = NotificationCompat.Builder(applicationContext, channelId)
            .setSmallIcon(R.drawable.ic_walk) // Using your new vector icon!
            .setContentTitle("🚶 Stand up now")
            .setContentText("You've been inactive for 2 hours. Time to stretch!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 5. Fire the notification
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(1001, builder.build())

        Log.d("SittingWorker", "Notification Sent Successfully!")
    }
}