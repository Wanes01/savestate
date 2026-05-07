package com.example.savestate.data.datastore

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.savestate.data.models.UserData
import com.example.savestate.data.models.UserXp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class UserPreferences(private val dataStore: DataStore<Preferences>) {
    companion object {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val USER_ID = stringPreferencesKey("user_id")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val EMAIL = stringPreferencesKey("email")
        val PHOTO_URL = stringPreferencesKey("photo_url")
        val XP = intPreferencesKey("xp")
        val DAY_STREAK = intPreferencesKey("day_streak")
        val LAST_SESSION_DATE = stringPreferencesKey("last_session_date")
    }

    // the current user data
    val userData: Flow<UserData> = dataStore.data.map { prefs ->
        UserData(
            isLoggedIn = prefs[IS_LOGGED_IN] ?: false,
            userId = prefs[USER_ID] ?: "",
            displayName = prefs[DISPLAY_NAME] ?: "",
            email = prefs[EMAIL] ?: "",
            photoUrl = prefs[PHOTO_URL],
        )
    }

    // current user xp data
    val userXp : Flow<UserXp> = dataStore.data.map { prefs ->
        UserXp(
            xp = prefs[XP] ?: 0,
            dayStreak = prefs[DAY_STREAK] ?: 0
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

    suspend fun saveXpData(userXp: UserXp) {
        dataStore.edit { prefs ->
            prefs[XP] = userXp.xp
            prefs[DAY_STREAK] = userXp.dayStreak
        }
    }

    /**
     * Removes the user data from the data store.
     */
    suspend fun clearUser() {
        dataStore.edit { prefs ->
            prefs.remove(IS_LOGGED_IN)
            prefs.remove(USER_ID)
            prefs.remove(DISPLAY_NAME)
            prefs.remove(EMAIL)
            prefs.remove(PHOTO_URL)
            prefs.remove(XP)
            prefs.remove(DAY_STREAK)
            prefs.remove(LAST_SESSION_DATE)
        }
    }

    /**
     * Adds an XP difference to the current xp amount.
     * XPs are never set below 0.
     * The quantity can be either positive or negative.
     */
    suspend fun addXp(xpDiff: Int) {
        dataStore.edit { prefs ->
            val currXp = prefs[XP] ?: 0
            prefs[XP] = (currXp + xpDiff).coerceAtLeast(0)
        }
    }

    /**
     * Updates the day streak session value based
     * on the last time it was updated.
     * Calling this functions multiple times will
     * produce no side effects.
     */
    suspend fun updateStreak() {
        dataStore.edit { prefs ->
            val today = LocalDate.now()
            val lastSessionStr = prefs[LAST_SESSION_DATE]
            val lastSession = lastSessionStr?.let { LocalDate.parse(it) }

            when (lastSession) {
                // first session at all
                null -> {
                    prefs[DAY_STREAK] = 1
                }
                // session already registered today, does nothing
                today -> return@edit
                // last session was yesterday, increases the streak
                today.minusDays(1) -> {
                    prefs[DAY_STREAK] = (prefs[DAY_STREAK] ?: 0) + 1
                }
                // the gap is longer than a day. Resets the streak.
                else -> {
                    prefs[DAY_STREAK] = 1
                }
            }

            prefs[LAST_SESSION_DATE] = today.toString()
        }
    }
}