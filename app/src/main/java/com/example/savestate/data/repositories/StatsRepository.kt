package com.example.savestate.data.repositories

import com.example.savestate.data.database.dao.GameSessionDao
import com.example.savestate.data.database.dao.UserAchievementDao
import com.example.savestate.data.database.dao.UserGameDao
import com.example.savestate.data.database.entity.GameSessionEntity
import com.example.savestate.data.models.GameSessionWithName
import com.example.savestate.data.models.GameStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.ZoneOffset

class StatsRepository(
    private val userGameDao: UserGameDao,
    private val gameSessionDao: GameSessionDao,
    private val userAchievementDao: UserAchievementDao
) {
    val gamesCompletedCount: Flow<Int> = userGameDao
        .getGamesByStatus(GameStatus.COMPLETED)
        .map { it.size }

    val totalMinutesPlayed: Flow<Int> = gameSessionDao
        .getTotalMinutesByGame()
        .map { list -> list.sumOf { it.totalMinutes } }

    val totalAchievementsCompleted: Flow<Int> = userAchievementDao
        .getAchievementProgressByGame()
        .map { list -> list.sumOf { it.completed } }

    fun getSessionsInRange(from: Long, to: Long): Flow<List<GameSessionEntity>> =
        gameSessionDao.getSessionsInRange(from, to)

    fun getWeeklySessionsWithName(): Flow<List<GameSessionWithName>> {
        val today = LocalDate.now()
        val from = today.minusDays(6).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        val to = today.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
        return gameSessionDao.getSessionsWithNameInRange(from, to)
    }
}