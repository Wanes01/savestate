package com.example.savestate.notification

import android.Manifest
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.savestate.R

// daily streak notification
class StreakReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_STREAK)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Don't break your streak! \uD83D\uDD25")
            .setContentText("Log a game session today to keep your streak alive")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NotificationHelper.NOTIF_ID_STREAK, notification)

        return Result.success()
    }
}