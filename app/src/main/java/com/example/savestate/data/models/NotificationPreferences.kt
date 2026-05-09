package com.example.savestate.data.models

data class NotificationPreferences (
    val streakEnabled: Boolean = false,
    val streakHour: Int = 20,
    val streakMinute: Int = 0,
    val levelEnabled: Boolean = false
)