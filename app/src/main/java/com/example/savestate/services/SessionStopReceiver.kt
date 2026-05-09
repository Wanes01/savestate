package com.example.savestate.services

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.savestate.domain.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.context.GlobalContext

/**
 * Broadcast receiver that handles the "stop" action from the active session notification.
 *
 * When the user taps "stop" on the notification, a broadcast is sent here to
 * ensure that SessionManager.stopSession gets called.
 */
class SessionStopReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_STOP_SESSION = "com.example.savestate.STOP_SESSION"

        /**
         * Returns a pending intent that broadcast ACTION_STOP_SESSION.
         * This is used as a stop action in the session notification.
         */
        fun pendingIntent(context: Context): PendingIntent =
            PendingIntent.getBroadcast(
                context,
                0,
                Intent(context, SessionStopReceiver::class.java).apply {
                    action = ACTION_STOP_SESSION
                },
                PendingIntent.FLAG_IMMUTABLE
            )
    }

    /**
     * Retrieves SessionManager to stop the active session.
     */
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_STOP_SESSION) return
        // get dependency outside of viewmodel/composable
        val sessionManager = GlobalContext.get().get<SessionManager>()
        CoroutineScope(Dispatchers.IO).launch {
            sessionManager.stopSession()
        }
    }
}