package com.example.savestate.domain

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.savestate.data.database.dao.GameSessionDao
import com.example.savestate.data.database.entity.GameSessionEntity
import com.example.savestate.data.datastore.UserPreferences
import com.example.savestate.services.SessionForegroundService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// the current active session
data class ActiveSession(
    val gameId: Int,
    val gameName: String,
    val startTime: Long = System.currentTimeMillis()
)

class SessionManager(
    private val context: Context,
    private val sessionDao: GameSessionDao,
    private val userPreferences: UserPreferences
) {
    private val _activeSession = MutableStateFlow<ActiveSession?>(null)
    val activeSession: StateFlow<ActiveSession?> = _activeSession.asStateFlow()

    fun startSession(gameId: Int, gameName: String) {
        if (_activeSession.value?.gameId == gameId) return
        val session = ActiveSession(gameId = gameId, gameName = gameName)
        _activeSession.value = session

        // starts the chronometer service
        CoroutineScope(Dispatchers.IO).launch {
            val notifEnabled = userPreferences.notificationPreferences.first().sessionEnabled
            if (notifEnabled) {
                val intent = SessionForegroundService.startIntent(
                    context, gameName, session.startTime
                )
                ContextCompat.startForegroundService(context, intent)
            }
        }
    }

    suspend fun stopSession() {
        val session = _activeSession.value ?: return
        _activeSession.value = null

        // stops the service even if notification was not enabled, just to be sure
        context.startService(SessionForegroundService.stopIntent(context))

        val endTime = System.currentTimeMillis()
        val durationMinutes = session.startTime.deltaToMinutes(endTime)
        if (durationMinutes < 1) return

        userPreferences.updateStreak()
        sessionDao.insertSession(
            GameSessionEntity(
                gameId = session.gameId,
                startTime = session.startTime,
                endTime = endTime,
                durationMinutes = durationMinutes
            )
        )

        val xpDiff =
            XpSystem.xpForSession(durationMinutes, userPreferences.userXp.first().dayStreak)
        val notifEnabled = userPreferences.notificationPreferences.first().levelEnabled
        userPreferences.addXpWithLevelUp(xpDiff, context, notifEnabled)
    }

    private fun Long.deltaToMinutes(endTime: Long) = ((endTime - this) / 1000 / 60).toInt()
}

fun Long.toFormattedTime(): String {
    val h = this / 3600
    val m = (this % 3600) / 60
    val s = this % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s)
    else "%02d:%02d".format(m, s)
}