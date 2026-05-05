package com.example.savestate.ui.theme.screens.gamedetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.savestate.AppViewModel
import com.example.savestate.ui.theme.components.gamedetail.GameDetailContent
import org.koin.androidx.compose.koinViewModel

@Composable
fun GameDetailsScreen(
    modifier: Modifier = Modifier,
    gameId: Int,
    appViewModel: AppViewModel,
    onGoBack: () -> Unit
) {
    val gameDetailViewModel: GameDetailViewModel = koinViewModel()
    val uiState by gameDetailViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(gameId) {
        gameDetailViewModel.loadGame(gameId)
        appViewModel.setTopBar(visible = false)
    }

    when {
        // it's currently fetching the game information
        uiState.isLoading -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        // an error occurred
        uiState.error != null -> {
            Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = uiState.error!!,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }

        // the game information have been fetched
        uiState.game != null -> {
            GameDetailContent(
                game = uiState.game!!,
                uiState = uiState,
                modifier = modifier,
                onBack = onGoBack,
                onStatusSelected = { gameDetailViewModel.onStatusSelected(it) },
                onNotesChanged = { gameDetailViewModel.onNotesChanged(it) },
                onPersonalRatingChanged = { gameDetailViewModel.onPersonalRatingChanged(it) },
                onAchievementToggled = { id, completed ->
                    gameDetailViewModel.onAchievementToggled(id, completed)
                },
                onSessionToggled = { gameDetailViewModel.onSessionToggled() }
            )
        }
    }
}