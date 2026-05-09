package com.example.savestate.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.concurrent.TimeUnit

object NotificationScheduler {

    private const val STREAK_WORK_NAME = "streak_reminder"

    /**
     * Schedules or replaces the daily remainder of the streak.
     * Computes the delay in minutes between now and the next time chosen by the user.
     */
    fun scheduleStreakReminder(context: Context, hour: Int, minute: Int) {
        val now = LocalDateTime.now()
        var target = now.withHour(hour).withMinute(minute).withSecond(0)

        // if today's time has passed, schedule for tomorrow
        if (target.isBefore(now)) {
            target = target.plusDays(1)
        }

        val delay = ChronoUnit.MINUTES.between(now, target)
        val request = PeriodicWorkRequestBuilder<StreakReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(delay, TimeUnit.MINUTES)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            STREAK_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE, // gets replaced if it already exists
            request
        )
    }

    /**
     * Cancels the streak reminder
     */
    fun cancelStreakReminder(context: Context) {
        WorkManager.getInstance(context).cancelUniqueWork(STREAK_WORK_NAME)
    }

    /**
     * One shot notification: to be called when the user levels up
     */
    fun notifyLevelUp(context: Context, level: Int, levelTitle: String) {
        val data = workDataOf(
            "level" to level,
            "level_title" to levelTitle
        )

        val request = OneTimeWorkRequestBuilder<LevelUpWorker>()
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }
}