package com.example.savestate.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object NotificationHelper {

    const val CHANNEL_STREAK = "channel_streak"
    const val CHANNEL_LEVEL = "channel_level"

    const val NOTIF_ID_STREAK = 1
    const val NOTIF_ID_LEVEL = 2

    /**
     * Creates a notification channels on the specified context
     */
    fun createChannels(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_STREAK,
                "Streak Reminder",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Daily reminder to keep your streak alive"
            }
        )

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_LEVEL,
                "Level Up",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifies you when you reach a new level"
            }
        )
    }

    /**
     * Checks if the POST_NOTIFICATION permission is granted (required on API 33+)
     */
    fun hasPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            // if API < 33 then the permission doesn't exist
            true
        }
    }
}