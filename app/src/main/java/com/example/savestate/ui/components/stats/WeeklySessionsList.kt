package com.example.savestate.ui.components.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLocale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.savestate.data.models.GameSessionWithName
import com.example.savestate.ui.screens.stats.toHoursAndMinutes
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import kotlin.collections.forEach

@Composable
fun WeeklySessionsList(sessions: List<GameSessionWithName>) {
    if (sessions.isEmpty()) return

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Sessions this week",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(Modifier.height(8.dp))

            sessions.forEach { session ->
                SessionRow(session)
                if (session != sessions.last()) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: GameSessionWithName) {
    val startDate = Instant.ofEpochMilli(session.startTime)
        .atZone(ZoneId.systemDefault())

    val dateLabel = startDate.toLocalDate().let { date ->
        when (date) {
            LocalDate.now() -> "Today"
            LocalDate.now().minusDays(1) -> "Yesterday"
            else -> date.dayOfWeek.getDisplayName(TextStyle.FULL, LocalLocale.current.platformLocale)
        }
    }

    val timeLabel = startDate.format(DateTimeFormatter.ofPattern("HH:mm"))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = session.gameName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "$timeLabel - $dateLabel",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Text(
            text = (session.durationMinutes / 60f).toHoursAndMinutes(),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}