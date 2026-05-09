package com.example.savestate.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.savestate.data.models.NotificationPreferences
import com.example.savestate.data.models.UserData
import com.example.savestate.data.models.UserXp
import com.example.savestate.domain.XpSystem
import com.example.savestate.notification.NotificationScheduler
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
        val NOTIF_STREAK_ENABLED = booleanPreferencesKey("notif_streak_enabled")
        val NOTIF_STREAK_HOUR = intPreferencesKey("notif_streak_hour")
        val NOTIF_STREAK_MINUTE = intPreferencesKey("notif_streak_minute")
        val NOTIF_LEVEL_ENABLED = booleanPreferencesKey("notif_level_enabled")
        val NOTIF_SESSION_ENABLED = booleanPreferencesKey("notif_session_enabled")
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
    val userXp: Flow<UserXp> = dataStore.data.map { prefs ->
        UserXp(
            xp = prefs[XP] ?: 0,
            dayStreak = prefs[DAY_STREAK] ?: 0
        )
    }

    // user's current notification preferences
    val notificationPreferences: Flow<NotificationPreferences> = dataStore.data.map { prefs ->
        NotificationPreferences(
            streakEnabled = prefs[NOTIF_STREAK_ENABLED] ?: false,
            streakHour = prefs[NOTIF_STREAK_HOUR] ?: 20,
            streakMinute = prefs[NOTIF_STREAK_MINUTE] ?: 0,
            levelEnabled = prefs[NOTIF_LEVEL_ENABLED] ?: false,
            sessionEnabled = prefs[NOTIF_SESSION_ENABLED] ?: false
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
            setOf(
                IS_LOGGED_IN,
                USER_ID,
                DISPLAY_NAME,
                EMAIL,
                PHOTO_URL,
                XP,
                DAY_STREAK,
                LAST_SESSION_DATE,
                NOTIF_STREAK_ENABLED,
                NOTIF_STREAK_HOUR,
                NOTIF_STREAK_MINUTE,
                NOTIF_LEVEL_ENABLED,
                NOTIF_SESSION_ENABLED
            ).forEach { prefs.remove(it) }
        }
    }

    /**
     * Adds an XP difference to the current xp amount.
     * If the user levels up calls an optional function onLevelUp
     * which has access to the new level and its title.
     * XPs are never set below 0.
     * The quantity can be either positive or negative.
     */
    suspend fun addXp(
        xpDiff: Int,
        onLevelUp: ((level: Int, levelTitle: String) -> Unit)? = null
    ) {
        dataStore.edit { prefs ->
            val currXp = prefs[XP] ?: 0
            val newXp = (currXp + xpDiff).coerceAtLeast(0)

            if (onLevelUp != null) {
                val oldLevel = XpSystem.levelFromXp(currXp)
                val newLevel = XpSystem.levelFromXp(newXp)
                if (newLevel > oldLevel) {
                    onLevelUp(newLevel, XpSystem.levelTitle(newLevel))
                }
            }

            prefs[XP] = newXp
        }
    }

    /**
     * Utility to add the specified xp level and notify the user about
     * a level up if possible
     */
    suspend fun addXpWithLevelUp(xpDiff: Int, context: Context, notifEnabled: Boolean) {
        addXp(xpDiff) { level, levelTitle ->
            if (notifEnabled) {
                NotificationScheduler.notifyLevelUp(context, level, levelTitle)
            }
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

    /**
     * Saves the specified notification preferences of the user
     */
    suspend fun saveNotificationPreferences(notifPrefs: NotificationPreferences) {
        dataStore.edit { prefs ->
            prefs[NOTIF_STREAK_ENABLED] = notifPrefs.streakEnabled
            prefs[NOTIF_STREAK_HOUR] = notifPrefs.streakHour
            prefs[NOTIF_STREAK_MINUTE] = notifPrefs.streakMinute
            prefs[NOTIF_LEVEL_ENABLED] = notifPrefs.levelEnabled
            prefs[NOTIF_SESSION_ENABLED] = notifPrefs.sessionEnabled
        }
    }
}