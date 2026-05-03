package com.example.savestate.data.models

data class UserData(
    val isLoggedIn: Boolean = false,
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null
)