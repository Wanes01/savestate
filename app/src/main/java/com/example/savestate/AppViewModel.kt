package com.example.savestate

import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.datastore.UserData
import com.example.savestate.data.repositories.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class TopBarState(
    val title: String = "",
    val actions: (@Composable RowScope.() -> Unit)? = null
)

class AppViewModel(private val authRepository: AuthRepository) : ViewModel() {
    private val _topBarState = MutableStateFlow(TopBarState())
    val topBarState: StateFlow<TopBarState> = _topBarState.asStateFlow()

    val userData: StateFlow<UserData> = authRepository.userData
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserData()
        )

    // becomes true when the datastore becomes readable
    val isReady: StateFlow<Boolean> = authRepository.userData
        .map { true }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    fun setTopBar(title: String, actions: (@Composable RowScope.() -> Unit)? = null) {
        _topBarState.update { TopBarState(title, actions) }
    }

    fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}