package com.example.savestate.ui.theme.components.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.savestate.data.models.Theme
import com.example.savestate.ui.theme.colors.NintendoColors
import com.example.savestate.ui.theme.colors.PlayStationColors
import com.example.savestate.ui.theme.colors.SystemColors
import com.example.savestate.ui.theme.colors.XboxColors
import kotlin.collections.chunked
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.forEach

@Composable
fun ThemeSelector(
    selected: Theme,
    onSelect: (Theme) -> Unit
) {
    val themes = Theme
        .entries
        .associateWith { it.toLabel() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            themes.entries.chunked(3).forEach { rowThemes ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    rowThemes.forEach { (theme, label) ->
                        ThemeChip(
                            label = label,
                            theme = theme,
                            isSelected = selected == theme,
                            onClick = { onSelect(theme) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeChip(
    theme: Theme,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = if (isSelected)
            MaterialTheme.colorScheme.secondaryContainer
        else
            MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = if (isSelected) 2.dp else 0.5.dp,
            color = if (isSelected)
                MaterialTheme.colorScheme.secondary
            else
                MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ThemeExample(theme = theme)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected)
                    MaterialTheme.colorScheme.onSecondaryContainer
                else
                    MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun ThemeExample(theme: Theme) {
    val darkMode = isSystemInDarkTheme()
    val (colorA, colorB) = when (theme) {
        Theme.SYSTEM -> if (darkMode) SystemColors.primaryDark to SystemColors.onPrimaryDark
            else SystemColors.primaryLight to SystemColors.onPrimaryLight
        Theme.DARK -> SystemColors.primaryDark to SystemColors.onPrimaryDark
        Theme.LIGHT -> SystemColors.primaryLight to SystemColors.onPrimaryLight
        Theme.PLAYSTATION -> if (darkMode) PlayStationColors.primaryDark to PlayStationColors.onPrimaryDark
            else PlayStationColors.primaryLight to PlayStationColors.onPrimaryLight
        Theme.XBOX -> if (darkMode) XboxColors.primaryDark to XboxColors.onPrimaryDark
            else XboxColors.primaryLight to XboxColors.onPrimaryLight
        Theme.NINTENDO -> if (darkMode) NintendoColors.primaryDark to NintendoColors.onPrimaryDark
            else NintendoColors.primaryLight to NintendoColors.onPrimaryLight
    }

    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, CircleShape)
    ) {
        // colored left half
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(0.5f)
                .background(colorA)
        )
        // colored right half
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth()
                .padding(start = 14.dp)
                .background(colorB)
        )
    }
}