package com.example.savestate.ui.screens.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.savestate.AppViewModel
import com.example.savestate.data.models.GameStatus
import com.example.savestate.ui.components.library.LibraryGameCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun LibraryScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel,
    onGameClick: (Int) -> Unit
) {
    val libraryViewModel: LibraryViewModel = koinViewModel()
    val uiState by libraryViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        appViewModel.setTopBar(title = "My Library")
    }

    Column(modifier = modifier.fillMaxSize()) {
        // filter chips for game status
        LibraryFilterRow(
            activeFilter = uiState.filter,
            onFilterSelected = libraryViewModel::onFilterSelected
        )
        HorizontalDivider()

        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            // no game in the library yet
            uiState.filteredGames.isEmpty() -> {
                LibraryEmptyState(filter = uiState.filter)
            }

            // displays the saved games
            else -> {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(uiState.filteredGames, key = { it.game.gameId }) { item ->
                        LibraryGameCard(
                            item = item,
                            onClick = { onGameClick(item.game.gameId) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun LibraryFilterRow(
    activeFilter: GameStatus?,
    onFilterSelected: (GameStatus?) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        GameStatus.entries.forEach { status ->
            FilterChip(
                selected = activeFilter == status,
                onClick = { onFilterSelected(status) },
                label = {
                    Text(
                        when (status) {
                            GameStatus.WISHLIST -> "Wishlist"
                            GameStatus.IN_PROGRESS -> "Playing"
                            GameStatus.COMPLETED -> "Completed"
                        }
                    )
                }
            )
        }
    }
}

// displayed if the game library is empty
@Composable
private fun LibraryEmptyState(filter: GameStatus?) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.SportsEsports,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (filter == null)
                    "Your library is empty"
                else
                    "No games in this category",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}