package com.example.savestate.ui.theme.screens.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.androidx.compose.koinViewModel
import androidx.compose.material3.Text

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    val viewModel: AuthViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Text("AUTENTICAZIONEEEE")
}