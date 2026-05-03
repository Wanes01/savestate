package com.example.savestate.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.savestate.data.models.Theme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ThemePreferences(private val dataStore: DataStore<Preferences>) {
    companion object {
        private val THEME_KEY = stringPreferencesKey("theme")
    }

    val theme: Flow<Theme> = dataStore.data
        .map { prefs ->
            // if no theme is saved use the system theme
            val themeName = prefs[THEME_KEY] ?: Theme.System.name
            Theme.valueOf(themeName)
        }

    suspend fun setTheme(theme: Theme) {
        dataStore.edit { prefs ->
            prefs[THEME_KEY] = theme.name
        }
    }
}