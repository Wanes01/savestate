package com.example.savestate

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.datastore.ThemePreferences
import com.example.savestate.data.models.Theme
import com.example.savestate.data.models.UserData
import com.example.savestate.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TopBarState(
    val title: String = "",
    val actions: (@Composable RowScope.() -> Unit)? = null
)

/*
data class NotificationSettings(
    val streakReminder: Boolean = true,
    val levelUp: Boolean = true,
    val sessionDuration: Boolean = false,
)
*/

class AppViewModel(
    private val themePreferences: ThemePreferences,
    private val authRepository: AuthRepository
) : ViewModel() {
    private val _topBarState = MutableStateFlow(TopBarState())
    val topBarState: StateFlow<TopBarState> = _topBarState.asStateFlow()

    val userData: StateFlow<UserData> = authRepository.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserData()
        )

    val theme: StateFlow<Theme> = themePreferences.theme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Theme.SYSTEM
        )

    // becomes true when the datastore becomes readable
    val isReady: StateFlow<Boolean> = combine(
        authRepository.userData,
        themePreferences.theme
    ) { _, _ -> true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setTopBar(title: String, actions: (@Composable RowScope.() -> Unit)? = null) {
        _topBarState.update { TopBarState(title, actions) }
    }

    fun setTheme(theme: Theme) {
        viewModelScope.launch { themePreferences.setTheme(theme) }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}