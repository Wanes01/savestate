package com.example.savestate.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.savestate.data.database.entity.GameSessionEntity
import com.example.savestate.data.models.GamePlaytime
import kotlinx.coroutines.flow.Flow

@Dao
interface GameSessionDao {

    /**
     * Inserts a new game session.
     */
    @Insert
    suspend fun insertSession(session: GameSessionEntity)

    /**
     * Returns all sessions for a game, ordered by most recent first.
     */
    @Query("SELECT * FROM game_sessions WHERE gameId = :gameId ORDER BY startTime DESC")
    fun getSessionsByGame(gameId: Int): Flow<List<GameSessionEntity>>

    /**
     * Returns the total minutes played for a game.
     */
    @Query("SELECT COALESCE(SUM(durationMinutes), 0) FROM game_sessions WHERE gameId = :gameId")
    fun getTotalMinutesByGame(gameId: Int): Flow<Int>

    /**
     * Returns the most recent session for a game.
     */
    @Query("SELECT * FROM game_sessions WHERE gameId = :gameId ORDER BY startTime DESC LIMIT 1")
    fun getLastSessionByGame(gameId: Int): Flow<GameSessionEntity?>

    /**
     * Returns all sessions within a date range (useful for chart visualization).
     */
    @Query("""
        SELECT * FROM game_sessions 
        WHERE startTime >= :from AND startTime <= :to 
        ORDER BY startTime ASC
    """)
    fun getSessionsInRange(from: Long, to: Long): Flow<List<GameSessionEntity>>

    /**
     * Deletes a specific session.
     */
    @Delete
    suspend fun deleteSession(session: GameSessionEntity)

    @Query("""
        SELECT gameId, SUM(durationMinutes) as totalMinutes
        FROM game_sessions
        GROUP BY gameId
    """)
    fun getTotalMinutesByGame(): Flow<List<GamePlaytime>>
}