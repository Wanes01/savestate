package com.example.savestate.ui.theme.components.gamedetail

import android.content.Intent
import android.text.format.DateUtils
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.savestate.data.database.entity.UserAchievementEntity
import com.example.savestate.data.models.GameStatus
import com.example.savestate.data.models.RawgGameDetail
import com.example.savestate.ui.theme.components.ExpandableText
import com.example.savestate.ui.theme.components.Section
import com.example.savestate.ui.theme.screens.gamedetail.GameDetailUiState

private const val MAX_STRING_CONCAT = 3
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
                }

                // game achievements (if any)
                if (uiState.achievements.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(12.dp))
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

                        // Bottom spacing after last achievement
                        item { Spacer(modifier = Modifier.height(4.dp)) }
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

/**
 * Game banner. Displays the game image banner,
 * its name, the major producer and the main genres
 */
@Composable
private fun GameBanner(
    game: RawgGameDetail,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
    ) {
        AsyncImage(
            model = game.backgroundImage,
            contentDescription = game.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                    )
                )
        )
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(8.dp)
                .background(Color.Black.copy(alpha = 0.4f), shape = CircleShape)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Default.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                text = game.name,
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            val subtitle = listOf(
                game.developers.take(MAX_STRING_CONCAT).joinToString(", ") { it.name },
                game.genres.take(MAX_STRING_CONCAT).joinToString(", ") { it.name }
            ).filter { it.isNotEmpty() }.joinToString(" · ")

            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}


/**
 * Displays data about how the user
 * interacted with this game.
 * (Displayed only if it's in the user's library)
 */
@Composable
private fun UserGameSection(
    uiState: GameDetailUiState,
    onNotesChanged: (String) -> Unit,
    onPersonalRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMinutes = uiState.totalMinutesPlayed
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    Section(
        title = "My game data",
        expandable = true,
        initiallyExpanded = true,
        modifier = modifier
    ) {
        DetailRow(
            label = "Time played",
            value = { Text(if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m") }
        )

        if (uiState.lastSession != null) {
            val lastPlayed = DateUtils.getRelativeTimeSpanString(
                uiState.lastSession.startTime,
                System.currentTimeMillis(),
                DateUtils.DAY_IN_MILLIS
            )
            DetailRow(label = "Last played", value = { Text(lastPlayed.toString()) })
        }

        if (uiState.achievements.isNotEmpty()) {
            DetailRow(
                label = "Achievements",
                value = { Text("${uiState.completedAchievementsCount}/${uiState.achievements.size}") }
            )
        }

        // ── Personal rating ──────────────────────────────────────────────
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My rating",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                (1..5).forEach { star ->
                    Icon(
                        imageVector = if ((uiState.userGame?.personalRating ?: 0f) >= star)
                            Icons.Default.Star else Icons.Default.StarOutline,
                        contentDescription = "$star stars",
                        tint = StarYellow,
                        modifier = Modifier
                            .size(36.dp)
                            .clickable { onPersonalRatingChanged(star.toFloat()) }
                            .padding(4.dp)
                    )
                }
            }
        }

        // ── Notes ────────────────────────────────────────────────────────
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "My notes",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        var notesValue by rememberSaveable { mutableStateOf(uiState.userGame?.notes ?: "") }
        LaunchedEffect(uiState.userGame?.notes) {
            val incoming = uiState.userGame?.notes ?: ""
            if (incoming != notesValue) notesValue = incoming
        }

        OutlinedTextField(
            value = notesValue,
            onValueChange = {
                notesValue = it
                onNotesChanged(it)
            },
            placeholder = { Text("Add your notes here...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
    }
}

/**
 * Clickable surface that expands the
 * achievements in the main lazy column
 */
@Composable
private fun AchievementsSectionHeader(
    expanded: Boolean,
    completedCount: Int,
    totalCount: Int,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = if (!expanded)
            MaterialTheme.shapes.large
        else
            RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
            ),
        color = MaterialTheme.colorScheme.surfaceContainer,
        tonalElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Achievements",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "$completedCount/$totalCount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = if (expanded) "Collapse" else "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * Displays the information about
 * a specific achievement
 */
@Composable
private fun AchievementRow(
    achievement: UserAchievementEntity,
    onToggled: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggled(!achievement.isCompleted) }
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Checkbox(
                checked = achievement.isCompleted,
                onCheckedChange = onToggled
            )
            AsyncImage(
                model = achievement.image,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .clip(MaterialTheme.shapes.small)
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = achievement.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    textDecoration = if (achievement.isCompleted) TextDecoration.LineThrough else null
                )
                if (achievement.description.isNotBlank()) {
                    Text(
                        text = achievement.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${achievement.percent}% of players",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
private fun GeneralInformationSection(
    expandable: Boolean,
    game: RawgGameDetail,
    modifier: Modifier = Modifier
) {
    Section(
        title = "General information",
        expandable = expandable,
        initiallyExpanded = false,
        modifier = modifier
    ) {
        DetailRow(
            label = "Rating",
            value = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("${game.rating}/5")
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        Icons.Default.Star,
                        contentDescription = null,
                        tint = StarYellow,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        )
        if (game.metacritic != null) {
            DetailRow(label = "Metacritic", value = { Text("${game.metacritic}/100") })
        }
        if (!game.platforms.isNullOrEmpty()) {
            DetailRow(
                label = "Platforms",
                value = {
                    Text(
                        text = game.platforms.joinToString(", ") { it.platform.name },
                        textAlign = TextAlign.End
                    )
                }
            )
        }
        if (game.released != null) {
            DetailRow(label = "Released", value = { Text(game.released) })
        }
        if (game.playtime > 0) {
            DetailRow(label = "Avg. playtime", value = { Text("${game.playtime}h") })
        }
        if (game.esrbRating != null) {
            DetailRow(label = "ESRB rating", value = { Text(game.esrbRating.name) })
        }
        if (game.publishers.isNotEmpty()) {
            DetailRow(
                label = "Publishers",
                value = {
                    Text(
                        text = game.publishers.joinToString(", ") { it.name },
                        textAlign = TextAlign.End
                    )
                }
            )
        }
        if (!game.descriptionRaw.isNullOrBlank()) {
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Text(
                text = "Description",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            ExpandableText(text = game.descriptionRaw)
        }

        val context = LocalContext.current
        if (!game.website.isNullOrBlank()) {
            DetailRow(
                label = "Official website",
                value = {
                    Text(
                        text = "Open",
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable {
                            context.startActivity(
                                Intent(Intent.ACTION_VIEW, game.website.toUri())
                            )
                        }
                    )
                }
            )
        }
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: @Composable () -> Unit,
    modifier: Modifier = Modifier
) {
    HorizontalDivider()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(0.4f)
        )
        Box(modifier = Modifier.weight(0.6f), contentAlignment = Alignment.CenterEnd) {
            value()
        }
    }
}