package com.example.savestate.ui.components.gamedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.savestate.data.models.GameStatus
import com.example.savestate.data.models.RawgGameDetail
import com.example.savestate.ui.screens.gamedetail.GameDetailUiState
val StarYellow = Color(0xFFFFD700)

@Composable
fun GameDetailContent(
    game: RawgGameDetail,
    uiState: GameDetailUiState,
    onBack: () -> Unit,
    onStatusSelected: (GameStatus) -> Unit,
    onNotesChanged: (String) -> Unit,
    onPersonalRatingChanged: (Float) -> Unit,
    onAchievementToggled: (Int, Boolean) -> Unit,
    onSessionToggled: () -> Unit,
    modifier: Modifier = Modifier
) {
    // does not show game data if the game is just in the wishlist
    val showGameData = uiState.userGame != null && uiState.userGame.status != GameStatus.WISHLIST
    var achievementsExpanded by rememberSaveable { mutableStateOf(false) }

    Box(modifier = modifier.fillMaxSize()) {
        // renders the whole page as a lazy column
        // because hundreds of achievements could be loaded.
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                bottom = if (uiState.userGame != null) 80.dp else 16.dp
            )
        ) {
            // game banner
            item {
                GameBanner(game = game, onBack = onBack)
            }

            // status chips
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    GameStatus.entries.forEach { status ->
                        FilterChip(
                            selected = uiState.userGame?.status == status,
                            onClick = { onStatusSelected(status) },
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

            // game general information
            item {
                GeneralInformationSection(
                    expandable = showGameData,
                    game = game,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // user game data (if present)
            if (showGameData) {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    UserGameSection(
                        uiState = uiState,
                        onNotesChanged = onNotesChanged,
                        onPersonalRatingChanged = onPersonalRatingChanged,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // game achievements...
                when {
                    // it's fetching the achievements
                    uiState.isLoadingAchievements -> {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Loading achievements, please wait...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // achievements available. Shows them to the user
                    uiState.achievements.isNotEmpty() -> {
                        item {
                            AchievementsSectionHeader(
                                expanded = achievementsExpanded,
                                completedCount = uiState.completedAchievementsCount,
                                totalCount = uiState.achievements.size,
                                onToggle = { achievementsExpanded = !achievementsExpanded },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }

                        if (achievementsExpanded) {
                            items(
                                items = uiState.achievements,
                                key = { it.achievementId }
                            ) { achievement ->
                                AchievementRow(
                                    achievement = achievement,
                                    onToggled = { onAchievementToggled(achievement.achievementId, it) },
                                    modifier = Modifier.padding(horizontal = 16.dp)
                                )
                            }
                            item { Spacer(modifier = Modifier.height(4.dp)) }
                        }
                    }

                    // achievement data unavailable
                    else -> {
                        item {
                            Text(
                                text = "Achievement data is not available for this game",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // session button (only if in library)
        if (showGameData) {
            Button(
                onClick = onSessionToggled,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                colors = if (uiState.isSessionActive) {
                    ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                } else {
                    ButtonDefaults.buttonColors()
                }
            ) {
                Icon(
                    imageVector = if (uiState.isSessionActive) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(if (uiState.isSessionActive) "Stop session" else "Start session")
            }
        }
    }
}