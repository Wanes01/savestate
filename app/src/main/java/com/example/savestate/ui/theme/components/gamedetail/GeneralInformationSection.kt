package com.example.savestate.ui.theme.components.gamedetail

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.example.savestate.data.models.RawgGameDetail
import com.example.savestate.ui.theme.components.ExpandableText
import com.example.savestate.ui.theme.components.Section
import com.example.savestate.ui.theme.components.DetailRow

@Composable
fun GeneralInformationSection(
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