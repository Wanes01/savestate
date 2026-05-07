package com.example.savestate.data.repositories

import com.example.savestate.data.database.dao.GameSessionDao
import com.example.savestate.data.database.dao.UserAchievementDao
import com.example.savestate.data.database.dao.UserGameDao
import com.example.savestate.data.database.entity.GameSessionEntity
import com.example.savestate.data.database.entity.UserAchievementEntity
import com.example.savestate.data.database.entity.UserGameEntity
import com.example.savestate.data.models.AchievementProgress
import com.example.savestate.data.models.GamePlaytime
import com.example.savestate.data.models.GameStatus
import com.example.savestate.data.models.RawgGameDetail
import kotlinx.coroutines.flow.Flow

class LibraryRepository(
    private val userGameDao: UserGameDao,
    private val userAchievementDao: UserAchievementDao,
    private val gameSessionDao: GameSessionDao,
    private val rawgRepository: RawgRepository
) {

    fun getAllGames(): Flow<List<UserGameEntity>> = userGameDao.getAllGames()

    /**
     * Returns whether a game is in the user's library as a flow
     */
    fun getGameById(gameId: Int): Flow<UserGameEntity?> = userGameDao.getGameById(gameId)

    fun getPlaytimeByGame(): Flow<List<GamePlaytime>> =
        gameSessionDao.getTotalMinutesByGame()

    /**
     * Adds a game to the user's library and fetches its achievements from RAWG.
     *
     * @param game the RAWG game detail to add
     * @param status the initial status of the game
     */
    suspend fun addGame(game: RawgGameDetail, status: GameStatus) {
        val now = System.currentTimeMillis()
        userGameDao.upsertGame(
            UserGameEntity(
                gameId = game.id,
                name = game.name,
                backgroundImage = game.backgroundImage,
                rating = game.rating,
                ratingsCount = game.ratingsCount,
                genres = game.genres.joinToString(", ") { it.name },
                platforms = game.platforms
                    ?.joinToString(", ") { it.platform.name } ?: "",
                developers = game.developers.joinToString(", ") { it.name },
                publishers = game.publishers.joinToString(", ") { it.name },
                description = game.descriptionRaw,
                website = game.website,
                playtime = game.playtime,
                metacritic = game.metacritic,
                released = game.released,
                esrbRating = game.esrbRating?.name,
                status = status,
                personalRating = null,
                notes = "",
                addedAt = now,
                startedAt = if (status == GameStatus.IN_PROGRESS) now else null,
                completedAt = if (status == GameStatus.COMPLETED) now else null
            )
        )

        /* fetch and save all achievements from RAWG
        even if this operation fails the games gets still saved,
        which is the expected behavior */
        rawgRepository.getAllGameAchievements(game.id)
            .onSuccess { achievements ->
                userAchievementDao.upsertAchievements(
                    achievements.map { achievement ->
                        UserAchievementEntity(
                            achievementId = achievement.id,
                            gameId = game.id,
                            name = achievement.name,
                            description = achievement.description,
                            image = achievement.image,
                            percent = achievement.percent.toFloatOrNull() ?: 0f,
                            isCompleted = false
                        )
                    }
                )
            }
    }

    /**
     * Removes a game from the user's library.
     * (achievements and sessions are deleted automatically through CASCADE policy).
     */
    suspend fun removeGame(game: UserGameEntity) = userGameDao.deleteGame(game)

    suspend fun updateStatus(gameId: Int, status: GameStatus) =
        userGameDao.updateStatus(gameId, status)

    suspend fun updateNotes(gameId: Int, notes: String) =
        userGameDao.updateNotes(gameId, notes)

    suspend fun updatePersonalRating(gameId: Int, rating: Float) =
        userGameDao.updatePersonalRating(gameId, rating)

    // achievements

    fun getAchievementsByGame(gameId: Int): Flow<List<UserAchievementEntity>> =
        userAchievementDao.getAchievementsByGame(gameId)

    fun getCompletedAchievementsCount(gameId: Int): Flow<Int> =
        userAchievementDao.getCompletedCount(gameId)

    fun getAchievementProgress(): Flow<List<AchievementProgress>> =
        userAchievementDao.getAchievementProgressByGame()

    suspend fun updateAchievementCompleted(achievementId: Int, isCompleted: Boolean) =
        userAchievementDao.updateAchievementCompleted(achievementId, isCompleted)

    // sessions

    suspend fun insertSession(session: GameSessionEntity) =
        gameSessionDao.insertSession(session)

    fun getTotalMinutesByGame(gameId: Int): Flow<Int> =
        gameSessionDao.getTotalMinutesByGame(gameId)

    fun getLastSessionByGame(gameId: Int): Flow<GameSessionEntity?> =
        gameSessionDao.getLastSessionByGame(gameId)

    fun getSessionsByGame(gameId: Int): Flow<List<GameSessionEntity>> =
        gameSessionDao.getSessionsByGame(gameId)

    fun getSessionsInRange(from: Long, to: Long): Flow<List<GameSessionEntity>> =
        gameSessionDao.getSessionsInRange(from, to)
}