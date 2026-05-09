package com.example.savestate.notification

import android.Manifest
import com.example.savestate.R
import android.content.Context
import androidx.annotation.RequiresPermission
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters

// level up notification
class LevelUpWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : Worker(context, workerParams) {

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    override fun doWork(): Result {
        val level = inputData.getInt("level", 0)
        val levelTitle = inputData.getString("level_title") ?: ""

        val notification = NotificationCompat.Builder(context, NotificationHelper.CHANNEL_LEVEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Level up! \uD83D\uDC7E")
            .setContentText("You reached Level $level · $levelTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context)
            .notify(NotificationHelper.NOTIF_ID_LEVEL, notification)

        return Result.success()
    }
}