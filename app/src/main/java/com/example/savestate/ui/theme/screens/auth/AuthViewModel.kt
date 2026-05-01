package com.example.savestate.ui.theme.screens.auth

import android.util.Patterns
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

    companion object {
        const val MIN_PASSWORD_LENGTH = 8;
    }
    val uiState: StateFlow<AuthUIState> = authRepository.userData
        .map { AuthUIState(isLoggedIn = it.isLoggedIn) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AuthUIState(isLoading = true)
        )

    fun register(email: String, password: String) {
        // email validation
        require(isEmailValid(email)) { "Provide a valid email address." }
        require(isPasswordValid(password)) {
            "The password is invalid. The password must be at least $MIN_PASSWORD_LENGTH " +
                    "characters long and include all of the following: lowercase letters, " +
                    "uppercase letters, special characters, and numbers."
        }
    }

    // checks if the provided string is a valid email address
    private fun isEmailValid(email: String): Boolean =
        email.isNotBlank() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    // checks if the provide string is a valid password
    private fun isPasswordValid(password: String): Boolean {
        val hasNumber = password.any { it.isDigit() }
        val hasUpperCase = password.any { it.isUpperCase() }
        val hasLowerCase = password.any { it.isLowerCase() }
        val hasSpecialChar = password.any { !it.isLetterOrDigit() }

        return password.length >= MIN_PASSWORD_LENGTH &&
                hasNumber &&
                hasUpperCase &&
                hasLowerCase &&
                hasSpecialChar
    }

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