package com.example.savestate.data.repositories

import com.example.savestate.data.datastore.UserData
import com.example.savestate.data.datastore.UserPreferences
import kotlinx.coroutines.flow.Flow

class AuthRepository(private val userPreferences: UserPreferences) {

    val userData: Flow<UserData> = userPreferences.userData

    suspend fun loginWithEmail(email: String, password: String): Result<UserData> {
        return try {
            // TEST
            val user = UserData(
                isLoggedIn = true,
                userId = "123",
                displayName = email.substringBefore("@"),
                email = email,
                photoUrl = null
            )
            userPreferences.saveUser(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logout() {
        userPreferences.clearUser()
    }
}