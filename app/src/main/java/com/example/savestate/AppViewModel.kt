package com.example.savestate

import android.util.Log
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.database.SavestateDatabase
import com.example.savestate.data.database.dao.GameSessionDao
import com.example.savestate.data.database.dao.UserAchievementDao
import com.example.savestate.data.database.dao.UserGameDao
import com.example.savestate.data.database.entity.GameSessionEntity
import com.example.savestate.data.datastore.ThemePreferences
import com.example.savestate.data.datastore.UserPreferences
import com.example.savestate.data.models.Theme
import com.example.savestate.data.models.UserData
import com.example.savestate.data.models.UserXp
import com.example.savestate.data.repositories.AuthRepository
import com.example.savestate.data.repositories.FirestoreSyncRepository
import com.example.savestate.data.repositories.LibraryRepository
import com.example.savestate.domain.XpSystem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TopBarState(
    val title: String = "",
    val actions: (@Composable RowScope.() -> Unit)? = null,
    val isTopBarVisible: Boolean = true,
)

class AppViewModel(
    private val themePreferences: ThemePreferences,
    private val userPreferences: UserPreferences,
    private val authRepository: AuthRepository,
    private val firestoreSyncRepository: FirestoreSyncRepository,
    private val userGameDao: UserGameDao,
    private val userAchievementDao: UserAchievementDao,
    private val gameSessionDao: GameSessionDao,
    private val database: SavestateDatabase
) : ViewModel() {
    private val _topBarState = MutableStateFlow(TopBarState())
    val topBarState: StateFlow<TopBarState> = _topBarState.asStateFlow()

    val userData: StateFlow<UserData> = userPreferences.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserData()
        )

    val userXp: StateFlow<UserXp> = userPreferences.userXp
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserXp()
        )


    val theme: StateFlow<Theme> = themePreferences.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Theme.SYSTEM
        )

    // becomes true when the datastore becomes readable
    val isReady: StateFlow<Boolean> = combine(
        userPreferences.userData,
        themePreferences.theme
    ) { _, _ -> true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setTopBar(
        visible: Boolean = true,
        title: String = "",
        actions: (@Composable RowScope.() -> Unit)? = null
    ) {
        _topBarState.update { TopBarState(title, actions, visible) }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch { themePreferences.setTheme(theme) }
    }

    fun logout() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val userId = userPreferences.userData.first().userId
                if (userId.isNotBlank()) {
                    val games = userGameDao.getAllGamesOnce()
                    val achievements = userAchievementDao.getAllAchievementsOnce()
                    val sessions = gameSessionDao.getAllSessionsOnce()
                    val xp = userPreferences.userXp.first().xp

                    // overwrites firestore user's data
                    firestoreSyncRepository.uploadUserData(
                        userId = userId,
                        games = games,
                        achievements = achievements,
                        sessions = sessions,
                        xp = xp
                    )
                }
            } catch (e: Exception) {
                Log.w("AppViewModel", "Firestore sync failed at logout", e)
            } finally {
                // logs out even if the sync failed
                database.clearAllTables()
                authRepository.logout()
            }
        }
    }
}