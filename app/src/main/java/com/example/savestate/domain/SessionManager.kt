package com.example.savestate.domain

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// the current active session
data class ActiveSession(
    val gameId: Int,
    val gameName: String,
    val startTime: Long = System.currentTimeMillis()
)

class SessionManager {
    private val _activeSession = MutableStateFlow<ActiveSession?>(null)
    val activeSession: StateFlow<ActiveSession?> = _activeSession.asStateFlow()

    /**
     * Starts a new session for the specified game.
     * Does nothing if there's an active session for the same game.
     */
    fun startSession(gameId: Int, gameName: String) {
        if (_activeSession.value?.gameId == gameId) return
        _activeSession.value = ActiveSession(gameId = gameId, gameName = gameName)
    }

    /**
     * Stops the current active session returns it.
     */
    fun stopSession(): ActiveSession? {
        val session = _activeSession.value
        _activeSession.value = null
        return session
    }

    /**
     * Cancels the current active session.
     */
    fun cancelSession() {
        _activeSession.value = null
    }
}