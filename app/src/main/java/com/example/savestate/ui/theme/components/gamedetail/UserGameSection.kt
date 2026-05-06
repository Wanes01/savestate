package com.example.savestate.ui.theme.components.gamedetail

import android.text.format.DateUtils
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.savestate.ui.theme.components.DetailRow
import com.example.savestate.ui.theme.components.Section
import com.example.savestate.ui.theme.screens.gamedetail.GameDetailUiState

/**
 * Displays data about how the user
 * interacted with this game.
 * (Displayed only if it's in the user's library)
 */
@Composable
fun UserGameSection(
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

        // user rating of the game
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

        // user notes of the game
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