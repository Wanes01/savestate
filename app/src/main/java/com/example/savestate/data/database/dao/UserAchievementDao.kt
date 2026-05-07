package com.example.savestate.data.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.example.savestate.data.database.entity.UserAchievementEntity
import com.example.savestate.data.models.AchievementProgress
import kotlinx.coroutines.flow.Flow

@Dao
interface UserAchievementDao {

    /**
     * Inserts all achievements for a game.
     * Replaces existing entries if they already exist.
     */
    @Upsert
    suspend fun upsertAchievements(achievements: List<UserAchievementEntity>)

    /**
     * Returns all achievements for a game, ordered by percentage of competition.
     */
    @Query("SELECT * FROM user_achievements WHERE gameId = :gameId ORDER BY percent ASC")
    fun getAchievementsByGame(gameId: Int): Flow<List<UserAchievementEntity>>

    /**
     * Returns the number of completed achievements for a game.
     */
    @Query("SELECT COUNT(*) FROM user_achievements WHERE gameId = :gameId AND isCompleted = 1")
    fun getCompletedCount(gameId: Int): Flow<Int>

    /**
     * Toggles the completion status of an achievement.
     */
    @Query("UPDATE user_achievements SET isCompleted = :isCompleted WHERE achievementId = :achievementId")
    suspend fun updateAchievementCompleted(achievementId: Int, isCompleted: Boolean)

    /**
     * Deletes all achievements for a game (to be called when the game is removed from the library).
     */
    @Query("DELETE FROM user_achievements WHERE gameId = :gameId")
    suspend fun deleteAchievementsByGame(gameId: Int)

    @Query(
        """
        SELECT gameId, COUNT(*) as total, SUM(CASE WHEN isCompleted = 1 THEN 1 ELSE 0 END) as completed
        FROM user_achievements
        GROUP BY gameId
    """
    )
    fun getAchievementProgressByGame(): Flow<List<AchievementProgress>>
}