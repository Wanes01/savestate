package com.example.savestate.ui.components.library

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.savestate.data.models.AchievementProgress
import com.example.savestate.data.models.GameStatus
import com.example.savestate.ui.components.search.MAX_GENRES_TO_SHOW
import com.example.savestate.ui.screens.library.LibraryGameItem

@Composable
fun LibraryGameCard(
    item: LibraryGameItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val genres = remember(item.game.genres) {
        item.game.genres.split(", ").take(MAX_GENRES_TO_SHOW).joinToString(", ")
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        ) {
            // game image
            AsyncImage(
                model = item.game.backgroundImage,
                contentDescription = item.game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(100.dp)
                    .fillMaxHeight()
            )

            // game info
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // places the game title and title chip in the same row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.game.name,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    GameStatusBadge(status = item.game.status)
                }

                Text(
                    text = genres.ifEmpty { "Unknown" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // does not display achievements and play time if the game it's just in the wishlist
                // as the user may not want spoilers
                if (item.game.status != GameStatus.WISHLIST) {

                    // play time for this game
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = formatPlaytime(item.totalMinutes),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // achievement progress bar (hidden if there are no achievements)
                    item.achievementProgress?.let { progress ->
                        if (progress.total > 0) {
                            AchievementProgressBar(progress = progress)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementProgressBar(progress: AchievementProgress) {
    val fraction = progress.completed.toFloat() / progress.total
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
        )
        Text(
            text = "${progress.completed}/${progress.total} achievements",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// a small badge to display the game status on the card
@Composable
private fun GameStatusBadge(status: GameStatus) {
    val (label, color) = when (status) {
        GameStatus.IN_PROGRESS -> "Playing" to MaterialTheme.colorScheme.primary
        GameStatus.WISHLIST -> "Wishlist" to MaterialTheme.colorScheme.secondary
        GameStatus.COMPLETED -> "Completed" to MaterialTheme.colorScheme.tertiary
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}

// formats the played minutes in hours if possibile
private fun formatPlaytime(minutes: Int): String =
    if (minutes < 60)
        "${minutes}m"
    else
        "${minutes / 60}h ${minutes % 60}m"