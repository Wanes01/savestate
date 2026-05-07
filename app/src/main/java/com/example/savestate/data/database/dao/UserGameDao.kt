package com.example.savestate.data.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.example.savestate.data.database.entity.UserGameEntity
import com.example.savestate.data.models.GameStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface UserGameDao {

    /**
     * Inserts a game into the user's library.
     * Replaces the existing entry if the game is already it's already there.
     */
    @Upsert
    suspend fun upsertGame(game: UserGameEntity)

    /**
     * Removes a game from the user's library.
     */
    @Delete
    suspend fun deleteGame(game: UserGameEntity)

    /**
     * Returns all games in the user's library, ordered by most recently added.
     */
    @Query("SELECT * FROM user_games ORDER BY addedAt DESC")
    fun getAllGames(): Flow<List<UserGameEntity>>

    /**
     * Returns all games in the user's library with a specific status.
     */
    @Query("SELECT * FROM user_games WHERE status = :status ORDER BY addedAt DESC")
    fun getGamesByStatus(status: GameStatus): Flow<List<UserGameEntity>>

    /**
     * Returns a single game by its RAWG id, null if it's not in the library.
     */
    @Query("SELECT * FROM user_games WHERE gameId = :gameId")
    fun getGameById(gameId: Int): Flow<UserGameEntity?>

    /**
     * Updates the status of a game and the relevant timestamps.
     */
    @Query(
        """
        UPDATE user_games SET 
            status = :status,
            startedAt = CASE WHEN :status = 'IN_PROGRESS' THEN :now ELSE startedAt END,
            completedAt = CASE WHEN :status = 'COMPLETED' THEN :now ELSE completedAt END
        WHERE gameId = :gameId
    """
    )
    suspend fun updateStatus(
        gameId: Int,
        status: GameStatus,
        now: Long = System.currentTimeMillis()
    )

    /**
     * Updates the user's personal notes for a game.
     */
    @Query("UPDATE user_games SET notes = :notes WHERE gameId = :gameId")
    suspend fun updateNotes(gameId: Int, notes: String)

    /**
     * Updates the user's personal rating for a game.
     */
    @Query("UPDATE user_games SET personalRating = :rating WHERE gameId = :gameId")
    suspend fun updatePersonalRating(gameId: Int, rating: Float)
}