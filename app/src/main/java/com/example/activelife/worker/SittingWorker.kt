package com.example.activelife.worker

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.activelife.R
import com.example.activelife.data.AppDatabase
import androidx.room.Room
import android.util.Log

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

            // ...and it started more than 2 hours ago
           // val twoHoursAgo = System.currentTimeMillis() - (2 * 60 * 60 * 1000)
            val twoHoursAgo = System.currentTimeMillis() - (10 * 1000)

            if (lastLog.timestamp < twoHoursAgo) {
                sendNotification()
            }
        }

        return Result.success()
    }

    private fun sendNotification() {
        // Check for permission (Android 13+)
        if (ActivityCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val builder = NotificationCompat.Builder(applicationContext, "SITTING_CHANNEL_ID")
            .setSmallIcon(android.R.drawable.ic_dialog_alert) // Use a system icon for now
            .setContentTitle("Time to Move!")
            .setContentText("You've been sitting for a while. Take a quick walk!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(1, builder.build())

        Log.d("SittingWorker", "Notification Sent!")
    }
}