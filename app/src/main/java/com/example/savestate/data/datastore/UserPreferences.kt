package com.example.savestate.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class UserData(
    val isLoggedIn: Boolean,
    val userId: String,
    val displayName: String,
    val email: String,
    val photoUrl: String?
)

class UserPreferences(private val dataStore: DataStore<Preferences>) {
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")
        val PHOTO_URL = stringPreferencesKey("photo_url")
    }

    // the current user data
    val userData: Flow<UserData> = dataStore.data.map { prefs ->
        UserData(
            isLoggedIn = prefs[IS_LOGGED_IN] ?: false,
            userId = prefs[USER_ID] ?: "",
            displayName = prefs[DISPLAY_NAME] ?: "",
            email = prefs[EMAIL] ?: "",
            photoUrl = prefs[PHOTO_URL]
        )
    }

    /**
     * saves the user data in the data store.
     * This can happen only if the users is logged in.
     */
    suspend fun saveUser(userData: UserData) {
        dataStore.edit { prefs ->
            prefs[IS_LOGGED_IN] = true
            prefs[USER_ID] = userData.userId
            prefs[DISPLAY_NAME] = userData.displayName
            prefs[EMAIL] = userData.email
            userData.photoUrl?.let { prefs[PHOTO_URL] = it }
        }
    }

    /**
     * Removes the user data from the data store.
     */
    suspend fun clearUser() {
        dataStore.edit { it.clear() }
    }
}