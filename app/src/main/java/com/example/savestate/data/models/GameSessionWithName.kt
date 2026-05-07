package com.example.savestate.data.models

data class GameSessionWithName(
    val sessionId: Int,
    val gameId: Int,
    val gameName: String,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int
)