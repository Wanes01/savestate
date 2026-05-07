package com.example.savestate.ui.components.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.savestate.data.models.RawgGenreFilter
import com.example.savestate.data.models.RawgOrdering
import com.example.savestate.data.models.RawgPlatformFilter
import com.example.savestate.data.models.SearchFilters

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterBottomSheet(
    currentFilters: SearchFilters,
    onApply: (SearchFilters) -> Unit,
    onDismiss: () -> Unit
) {
    // local copy of filters, applied only when the user presses apply
    var selectedGenres by remember { mutableStateOf(currentFilters.genres) }
    var selectedPlatforms by remember { mutableStateOf(currentFilters.platforms) }
    var minRating by remember { mutableFloatStateOf(currentFilters.minMetacriticRating) }
    var ordering by remember { mutableStateOf(currentFilters.ordering) }

    // bottom sheet
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "Filters",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Genres
            FilterSection(title = "Genre") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RawgGenreFilter.entries.forEach { genre ->
                        FilterChip(
                            selected = genre in selectedGenres,
                            onClick = {
                                selectedGenres = if (genre in selectedGenres) selectedGenres - genre
                                else selectedGenres + genre
                            },
                            label = { Text(genre.displayName) }
                        )
                    }
                }
            }

            // platforms
            FilterSection(title = "Platform") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RawgPlatformFilter.entries.forEach { platform ->
                        FilterChip(
                            selected = platform in selectedPlatforms,
                            onClick = {
                                selectedPlatforms =
                                    if (platform in selectedPlatforms) selectedPlatforms - platform
                                    else selectedPlatforms + platform
                            },
                            label = { Text(platform.displayName) }
                        )
                    }
                }
            }

            // min rating
            FilterSection(
                title = "Min metacritic rating: ${
                    if (minRating == 0f) "Any" else "%.1f".format(
                        minRating
                    )
                }"
            ) {
                Slider(
                    value = minRating,
                    onValueChange = { minRating = it },
                    valueRange = 0f..100f,
                    steps = 19, // uses a 5 step (5, 10, 15, etc...)
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("0", style = MaterialTheme.typography.bodySmall)
                    Text("100", style = MaterialTheme.typography.bodySmall)
                }
            }

            // sort by...
            FilterSection(title = "Sort by") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    RawgOrdering.entries.forEach { order ->
                        FilterChip(
                            selected = order == ordering,
                            onClick = { ordering = order },
                            label = { Text(order.displayName) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // apply and reset filters buttons
            Button(
                onClick = {
                    onApply(SearchFilters(selectedGenres, selectedPlatforms, minRating, ordering))
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply filters")
            }

            TextButton(
                onClick = {
                    // resets all filters to default
                    onApply(SearchFilters())
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset filters")
            }
        }
    }
}

@Composable
private fun FilterSection(
    title: String,
    content: @Composable () -> Unit
) {
    // title
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    // section composable
    content()
    // divider between this and the next section
    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
}