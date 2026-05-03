package com.example.savestate.ui.theme.screens.search

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.savestate.AppViewModel

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel,
) {
    LaunchedEffect(Unit) {
        appViewModel.setTopBar(
            title = "Search for games"
        )
    }

    var searchQuery by rememberSaveable { mutableStateOf("") }
}