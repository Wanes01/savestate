package com.example.savestate.services

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.savestate.MainActivity
import com.example.savestate.R
import com.example.savestate.notification.NotificationHelper

/**
 * Foreground service that displays a persistent notification with a live
 * chronometer while a game session is active.
 * Stopping the session triggers SessionStopReceiver, which calls the
 * SessionManager to correctly stop the session and the service.
 *
 * The chronometer is handled entirely by the system via
 * NotificationCompat.Builder.setUsesChronometer
 */
class SessionForegroundService : Service() {

    companion object {
        const val ACTION_START = "action_start"
        const val ACTION_STOP = "action_stop"
        const val EXTRA_GAME_NAME = "extra_game_name"
        const val EXTRA_START_TIME = "extra_start_time"

        /**
         * Returns an intent that starts the service and passes the session data
         */
        fun startIntent(context: Context, gameName: String, startTime: Long): Intent =
            Intent(context, SessionForegroundService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_GAME_NAME, gameName)
                putExtra(EXTRA_START_TIME, startTime)
            }

        /**
         * Returns an intent that stops the service
         */
        fun stopIntent(context: Context): Intent =
            Intent(context, SessionForegroundService::class.java).apply {
                action = ACTION_STOP
            }
    }

    private var gameName: String = ""
    private var startTime: Long = 0L

    /**
     * Handles incoming interns. On:
     *
     * ACTION_START: reads session data from extras and starts the foreground notification.
     * ACTION_STOP: stops the service and removes the notification.
     *
     * Returns START_NOT_STICKY so the service is not recreated by the system if it is killed
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                gameName = intent.getStringExtra(EXTRA_GAME_NAME) ?: ""
                startTime = intent.getLongExtra(EXTRA_START_TIME, System.currentTimeMillis())
                startForeground(
                    NotificationHelper.NOTIF_ID_SESSION,
                    buildNotification()
                )
            }
            ACTION_STOP -> stopSelf()
        }
        return START_NOT_STICKY
    }

    /**
     * Builds the persistent session notification.
     *
     * The chronometer starts from startTime and counts up automatically.
     * Tapping the notification brings the app back to the foreground.
     * The "stop" action sends a broadcast to SessionStopReceiver so the
     * session is correctly saved first
     */
    private fun buildNotification(): Notification {
        val openAppIntent = PendingIntent.getActivity(
            this,
            0,
            Intent(this, MainActivity::class.java).apply {
                // does not create another instance of MainActivity
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat
            .Builder(this, NotificationHelper.CHANNEL_SESSION)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(gameName)
            .setContentText("Session in progress")
            .setWhen(startTime)
            .setUsesChronometer(true)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(openAppIntent)
            .setAutoCancel(false)
            .addAction(0, "Stop", SessionStopReceiver.pendingIntent(this))
            .build()
    }

    // this service does not need binding
    override fun onBind(intent: Intent?): IBinder? = null
}