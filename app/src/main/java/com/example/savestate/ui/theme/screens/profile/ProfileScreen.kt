package com.example.savestate.ui.theme.screens.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.example.savestate.AppViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel,
) {
    LaunchedEffect(Unit) {
        appViewModel.setTopBar(
            title = "Profile & Settings"
        )
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column() {
            Text("Schermata utente e impostazioni")
        }
    }
}