package com.example.savestate.ui.theme.components.gamedetail

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import coil.compose.AsyncImage
import com.example.savestate.data.models.RawgGameDetail

const val MAX_STRING_CONCAT = 3

@Composable
fun GameDetailContent(
    game: RawgGameDetail,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // game banner
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
            // a gradient overlay gets applied for readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))
                        )
                    )
            )
            // back button over the image
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
            // game title on the image
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
                val subtitle = buildString {
                    val devNames = game.developers
                        .take(MAX_STRING_CONCAT)
                        .joinToString(", ") { it.name }
                    val genreNames = game.genres
                        .take(MAX_STRING_CONCAT)
                        .joinToString(", ") { it.name }
                    if (devNames.isNotEmpty()) append(devNames)
                    if (devNames.isNotEmpty() && genreNames.isNotEmpty()) append(" · ")
                    if (genreNames.isNotEmpty()) append(genreNames)
                }
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
            }
        }

        // INFO SECTION
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "General information",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            DetailRow(
                label = "Rating",
                value = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("${game.rating}/5")
                        Spacer(Modifier.width(4.dp))
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )

            // very few games seem to have a metacritic on rawg
            if (game.metacritic != null) {
                DetailRow(
                    label = "Metacritic",
                    value = { Text("${game.metacritic}/100") }
                )
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
                DetailRow(
                    label = "Released",
                    value = { Text(game.released) }
                )
            }

            if (game.playtime > 0) {
                DetailRow(
                    label = "Avg. playtime",
                    value = { Text("${game.playtime}h") }
                )
            }

            if (game.esrbRating != null) {
                DetailRow(
                    label = "ESRB rating",
                    value = { Text(game.esrbRating.name) }
                )
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

            // game description
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

            // official website
            if (!game.website.isNullOrBlank()) {
                DetailRow(
                    label = "Official website",
                    value = {
                        Text(
                            text = "Open",
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    game.website.toUri()
                                )
                                context.startActivity(intent)
                            }
                        )
                    }
                )
            }
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

@Composable
private fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 4
) {
    var expanded by rememberSaveable { mutableStateOf(false) }

    Column(modifier = modifier) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = if (expanded) Int.MAX_VALUE else collapsedMaxLines,
            overflow = if (expanded) TextOverflow.Clip else TextOverflow.Ellipsis
        )
        TextButton(onClick = { expanded = !expanded }) {
            Text(if (expanded) "Show less" else "Show more")
        }
    }
}