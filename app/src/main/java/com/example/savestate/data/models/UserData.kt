package com.example.savestate.data.models

data class UserData(
    val isLoggedIn: Boolean = false,
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val xp: Int = 0,
    val dayStreak: Int = 0
)