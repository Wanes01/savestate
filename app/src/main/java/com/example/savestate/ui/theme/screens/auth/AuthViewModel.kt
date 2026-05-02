package com.example.savestate.ui.theme.screens.auth

import android.content.Context
import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

data class AuthUIState(
    val isLoading: Boolean = false,
    val error: String? = null
)

class AuthViewModel(private val authRepository: AuthRepository) : ViewModel() {

    companion object {
        const val MIN_PASSWORD_LENGTH = 8;
    }

    private val _uiState = MutableStateFlow(AuthUIState())
    val uiState: StateFlow<AuthUIState> = _uiState.asStateFlow()

    fun register(email: String, password: String, confirmPassword: String) {
        val emailError: String? = validateEmail(email)
        val passwordError: String? = validatePassword(password, confirmPassword)

        // an error occurred, updates the current error to show
        if (emailError != null || passwordError != null) {
            _uiState.update { it.copy(error = emailError ?: passwordError) }
            return
        }

        // no client side error occurred. Tries to register the user
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.registerWithEmail(email, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun login(email: String, password: String) {
        val emailError = validateEmail(email)

        // does not contact the server. Bad email.
        emailError?.let {
            _uiState.update { it.copy(error = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.loginWithEmail(email, password)
                .onSuccess {
                    _uiState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            authRepository.loginWithGoogle(context)
                .onSuccess { _uiState.update { it.copy(isLoading = false) } }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return "Email cannot be empty"
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Invalid email address"
        return null
    }

    private fun validatePassword(password: String, confirmPassword: String): String? {
        if (password.length < MIN_PASSWORD_LENGTH)
            return "Password must be at least $MIN_PASSWORD_LENGTH characters."
        if (password != confirmPassword) return "Passwords do not match."
        if (!password.any { it.isDigit() }) return "Password must contain a number."
        if (!password.any { it.isUpperCase() }) return "Password must contain an uppercase letter."
        if (!password.any { it.isLowerCase() }) return "Password must contain a lowercase letter."
        if (!password.any { !it.isLetterOrDigit() }) return "Password must contain a special character."
        return null
    }
}