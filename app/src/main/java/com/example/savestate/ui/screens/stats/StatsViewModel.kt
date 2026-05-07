package com.example.savestate.ui.screens.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.datastore.UserPreferences
import com.example.savestate.data.models.GameSessionWithName
import com.example.savestate.data.repositories.StatsRepository
import com.example.savestate.domain.XpSystem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

data class StatsUiState(
    val level: Int = 0,
    val levelTitle: String = "",
    val totalXp: Int = 0,
    val xpToNextLevel: Int = 0,
    val levelProgress: Float = 0f,
    val dayStreak: Int = 0,
    val gamesCompleted: Int = 0,
    val totalHoursPlayed: Int = 0,
    val totalAchievements: Int = 0,
    val weeklyHours: Map<LocalDate, Float> = emptyMap(), // maps the date to hours of play time
    val weeklySessions: List<GameSessionWithName> = emptyList() // this week gaming session
)

class StatsViewModel(
    private val statsRepository: StatsRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    init {
        // loads the statics immediately
        loadStats()
    }

    private fun loadStats() {
        viewModelScope.launch {
            combine(
                userPreferences.userXp,
                statsRepository.gamesCompletedCount,
                statsRepository.totalMinutesPlayed,
                statsRepository.totalAchievementsCompleted,
                combine(
                    weeklySessionFlow(),
                    statsRepository.getWeeklySessionsWithName()
                ) { hours, sessions -> Pair(hours, sessions) }
            ) { xpData, gamesCompleted, totalMinutes, achievements, (weeklyHours, weeklySessions) ->

                val level = XpSystem.levelFromXp(xpData.xp)

                StatsUiState(
                    level = level,
                    levelTitle = XpSystem.levelTitle(level),
                    totalXp = xpData.xp,
                    xpToNextLevel = XpSystem.xpToNextLevel(xpData.xp),
                    levelProgress = XpSystem.levelProgress(xpData.xp),
                    dayStreak = xpData.dayStreak,
                    gamesCompleted = gamesCompleted,
                    totalHoursPlayed = totalMinutes / 60,
                    totalAchievements = achievements,
                    weeklyHours = weeklyHours,
                    // the week sessions are already sorted from the most to the least recent
                    weeklySessions = weeklySessions.sortedByDescending { it.startTime }
                )
            }.collect { _uiState.value = it }
        }
    }

    /**
     * Maps the previous 7 days (including today)
     * by the total number of hours played each day
     */
    private fun weeklySessionFlow(): Flow<Map<LocalDate, Float>> {
        val today = LocalDate.now()
        val from = today
            .minusDays(6)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()
        val to = today.plusDays(1)
            .atStartOfDay()
            .toInstant(ZoneOffset.UTC)
            .toEpochMilli()

        return statsRepository.getSessionsInRange(from, to).map { sessions ->
            val minutesByDay = mutableMapOf<LocalDate, Float>()

            sessions.forEach { session ->
                val date = Instant.ofEpochMilli(session.startTime)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                minutesByDay[date] = (minutesByDay[date] ?: 0f) + session.durationMinutes
            }

            // ensures all 7 days are present
            (6 downTo 0).associate { daysAgo ->
                val date = today.minusDays(daysAgo.toLong())
                date to (minutesByDay[date] ?: 0f) / 60f
            }
        }
    }
}