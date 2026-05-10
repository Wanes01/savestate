package com.example.savestate.ui.screens.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.savestate.AppViewModel
import com.example.savestate.ui.components.stats.SummaryRow
import com.example.savestate.ui.components.stats.WeeklyChart
import com.example.savestate.ui.components.stats.WeeklySessionsList
import com.example.savestate.ui.components.stats.XpCard
import org.koin.androidx.compose.koinViewModel
import kotlin.math.roundToInt

@Composable
fun StatsScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel,
) {
    val viewModel: StatsViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        appViewModel.setTopBar(title = "Statistics")
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item { XpCard(uiState) }
        item { SummaryRow(uiState) }
        item { WeeklyChart(uiState.weeklyHours) }
        item { WeeklySessionsList(uiState.weeklySessions) }
    }
}

// utility to display playtime in the format "#h#m"
fun Float.toHoursAndMinutes(): String {
    val totalMinutes = (this * 60).roundToInt()
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours == 0 -> "${minutes}m"
        minutes == 0 -> "${hours}h"
        else -> "${hours}h${minutes}m"
    }
}