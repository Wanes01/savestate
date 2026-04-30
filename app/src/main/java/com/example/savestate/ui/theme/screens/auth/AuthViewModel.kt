package com.example.savestate.ui.theme.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.repositories.AuthRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AuthUIState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    val uiState: StateFlow<AuthUIState> = authRepository.userData
        .map { AuthUIState(isLoggedIn = it.isLoggedIn) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthUIState(isLoading = true)
        )

    fun login(email: String, password: String) {
        viewModelScope.launch {
            // TODO: qua metti la validazione di email e password


            authRepository.loginWithEmail(email, password)
                .onFailure { error ->
                    // gestione futura dell'errore di login.
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }
}