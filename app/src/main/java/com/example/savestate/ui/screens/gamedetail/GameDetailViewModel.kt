package com.example.savestate.ui.screens.gamedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.database.entity.GameSessionEntity
import com.example.savestate.data.database.entity.UserAchievementEntity
import com.example.savestate.data.database.entity.UserGameEntity
import com.example.savestate.data.datastore.UserPreferences
import com.example.savestate.data.models.GameStatus
import com.example.savestate.data.models.RawgCompany
import com.example.savestate.data.models.RawgEsrbRating
import com.example.savestate.data.models.RawgGameDetail
import com.example.savestate.data.models.RawgGenre
import com.example.savestate.data.models.RawgPlatformInfo
import com.example.savestate.data.models.RawgPlatformWrapper
import com.example.savestate.data.repositories.LibraryRepository
import com.example.savestate.data.repositories.RawgRepository
import com.example.savestate.domain.XpSystem
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

data class GameDetailUiState(
    // base state
    val game: RawgGameDetail? = null,
    val isLoading: Boolean = true,
    val isLoadingAchievements: Boolean = false,
    val achievementsUnavailable: Boolean = false,
    val error: String? = null,

    // library state
    val userGame: UserGameEntity? = null, // null if not in user's library
    val achievements: List<UserAchievementEntity> = emptyList(),
    val completedAchievementsCount: Int = 0,
    val totalMinutesPlayed: Int = 0,
    val lastSession: GameSessionEntity? = null,

    // session timer
    val isSessionActive: Boolean = false,
    val sessionStartTime: Long? = null
)

@OptIn(FlowPreview::class)
class GameDetailViewModel(
    private val rawgRepository: RawgRepository,
    private val libraryRepository: LibraryRepository,
    private val userPreferences: UserPreferences
) : ViewModel() {

    companion object {
        private const val NOTE_EDIT_DEBOUNCE_DELAY_MS = 500L
    }

    private val _pendingNotes = MutableStateFlow<Pair<Int, String>?>(null)
    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _pendingNotes
                .filterNotNull()
                // waits some time before saving the note
                // (waits for the user to stop typing)
                .debounce(NOTE_EDIT_DEBOUNCE_DELAY_MS)
                .collectLatest { (gameId, notes) ->
                    libraryRepository.updateNotes(gameId, notes)
                }
        }
    }

    /**
     * Tries to load the specified game by the local database.
     * Fallbacks to RAWG repository to make the request
     * (used if the game is not in the user's library)
     *
     * @param gameId the RAWG id of the game to fetch
     */
    fun loadGame(gameId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            libraryRepository.getGameById(gameId).collect { userGame ->
                // the game is present in the library
                if (userGame != null) {
                    _uiState.update {
                        it.copy(
                            game = userGame.toRawgGameDetail(),
                            userGame = userGame,
                            isLoading = false
                        )
                    }
                    launch {
                        libraryRepository.getAchievementsByGame(gameId).collect { achievements ->
                            _uiState.update { it.copy(achievements = achievements) }
                        }
                    }
                    launch {
                        libraryRepository.getCompletedAchievementsCount(gameId).collect { count ->
                            _uiState.update { it.copy(completedAchievementsCount = count) }
                        }
                    }
                    launch {
                        libraryRepository.getTotalMinutesByGame(gameId).collect { minutes ->
                            _uiState.update { it.copy(totalMinutesPlayed = minutes) }
                        }
                    }
                    launch {
                        libraryRepository.getLastSessionByGame(gameId).collect { session ->
                            _uiState.update { it.copy(lastSession = session) }
                        }
                    }
                } else {
                    // the game is not present in the user's library.
                    // Fetches the game data from rawg
                    _uiState.update {
                        it.copy(
                            userGame = null,
                            achievements = emptyList(),
                            completedAchievementsCount = 0,
                            totalMinutesPlayed = 0,
                            lastSession = null
                        )
                    }
                    rawgRepository.getGameDetail(gameId)
                        .onSuccess { game ->
                            _uiState.update { it.copy(game = game, isLoading = false) }
                        }
                        .onFailure { error ->
                            _uiState.update { it.copy(isLoading = false, error = error.message) }
                        }
                }
            }
        }
    }

    // converts the game entity in the database to the format expected by the UI
    fun UserGameEntity.toRawgGameDetail(): RawgGameDetail = RawgGameDetail(
        id = gameId,
        name = name,
        backgroundImage = backgroundImage,
        rating = rating,
        ratingsCount = ratingsCount,
        genres = genres.split(",").filter { it.isNotBlank() }.map { RawgGenre(id = 0, name = it) },
        platforms = platforms.split(",").filter { it.isNotBlank() }
            .map { RawgPlatformWrapper(platform = RawgPlatformInfo(id = 0, name = it)) },
        developers = developers.split(",").filter { it.isNotBlank() }
            .map { RawgCompany(id = 0, name = it) },
        publishers = publishers.split(",").filter { it.isNotBlank() }
            .map { RawgCompany(id = 0, name = it) },
        descriptionRaw = description,
        website = website,
        playtime = playtime,
        metacritic = metacritic,
        released = released,
        esrbRating = esrbRating?.let { RawgEsrbRating(name = it) }
    )

    // LIBRARY ACTIONS

    /**
     * Adds the game to the library with the given status,
     * or updates the status if already in the library.
     * If the new status is equal to the previews one, removes the game
     * from the library.
     *
     * Gives / removes xp on game completition.
     */
    fun onStatusSelected(status: GameStatus) {
        // if no data is available there is nothing to do
        val game = _uiState.value.game ?: return
        val currentUserGame = _uiState.value.userGame

        viewModelScope.launch {
            val userXpData = userPreferences.userXp.first()
            val xpDiff = XpSystem.xpForGameCompleted(userXpData.dayStreak)

            when {
                // same status tapped again, remove from library
                currentUserGame?.status == status -> {
                    // game was completed, removes xps
                    if (status == GameStatus.COMPLETED) userPreferences.addXp(-xpDiff)

                    // removes completed achievement xps
                    val achievementsXp =
                        libraryRepository.getAchievementsByGame(currentUserGame.gameId)
                            .first()
                            .filter { it.isCompleted }
                            .sumOf {
                                XpSystem.xpForAchievement(
                                    it.percent,
                                    1
                                )
                            }
                    if (achievementsXp > 0) userPreferences.addXp(-achievementsXp)

                    // removes the sessions playtime xps
                    val sessionsXp =
                        libraryRepository.getSessionsByGame(currentUserGame.gameId)
                            .first()
                            .sumOf {
                                XpSystem.xpForSession(it.durationMinutes, 1)
                            }

                    if (sessionsXp > 0) userPreferences.addXp(-sessionsXp)

                    libraryRepository.removeGame(currentUserGame)
                }
                // already in library, updates status
                currentUserGame != null -> {
                    libraryRepository.updateStatus(game.id, status)
                    if (
                        status != GameStatus.COMPLETED
                        && currentUserGame.status == GameStatus.COMPLETED
                    ) { // completed to any other status
                        userPreferences.addXp(-xpDiff)
                    } else if (status == GameStatus.COMPLETED) { // any other status to completed
                        userPreferences.addXp(xpDiff)
                    }
                }
                // adds the game to the library
                else -> {
                    // the very first status is completed. Adds xps
                    if (status == GameStatus.COMPLETED) {
                        userPreferences.addXp(xpDiff)
                    }
                    _uiState.update {
                        it.copy(isLoadingAchievements = true)
                    }
                    libraryRepository.addGame(game, status)
                    _uiState.update { it.copy(isLoadingAchievements = false) }
                }
            }
        }
    }

    fun onNotesChanged(notes: String) {
        val gameId = _uiState.value.game?.id ?: return
        _pendingNotes.value = gameId to notes
    }

    /**
     * Sets the rating of the user for this game.
     * Assigns xps if it's the first time the user
     * rates this game.
     */
    fun onPersonalRatingChanged(rating: Float) {
        val gameId = _uiState.value.game?.id ?: return
        viewModelScope.launch {
            _uiState.value.userGame?.let {
                // the user never rated the game before
                if (it.personalRating == null) {
                    val userXpData = userPreferences.userXp.first()
                    val xpDiff = XpSystem.xpForGameRating(userXpData.dayStreak)
                    userPreferences.addXp(xpDiff)
                }
            }
            libraryRepository.updatePersonalRating(gameId, rating)
        }
    }

    /**
     * Toggles an achievement status to completed or not.
     *
     * Assigns / detracts the achievement xp based on completion.
     */
    fun onAchievementToggled(achievementId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            libraryRepository.updateAchievementCompleted(achievementId, isCompleted)

            val completionPerc = _uiState
                .value
                .achievements
                .first { it.achievementId == achievementId }
                .percent
            // assigns / removes xp
            val userXpData = userPreferences.userXp.first()
            val xpDiff = XpSystem.xpForAchievement(completionPerc, userXpData.dayStreak)
            userPreferences.addXp(if (isCompleted) xpDiff else -xpDiff)
        }
    }

    // SESSION TIMER ACTIONS

    /**
     * Starts/stops the game session timer.
     * When stopped, saves the session to the database.
     * Updates the current day streak when a new
     * session gets registered.
     *
     * Adds xps based on session length
     */
    fun onSessionToggled() {
        val gameId = _uiState.value.game?.id ?: return

        if (_uiState.value.isSessionActive) {
            // stop session and save it
            val startTime = _uiState.value.sessionStartTime ?: return
            val endTime = System.currentTimeMillis()
            val durationMinutes = startTime.deltaToMinutes(endTime)

            // only save sessions longer than 1 minute
            // in order to not pollute the database
            if (durationMinutes >= 1) {
                viewModelScope.launch {
                    // updates the day streak
                    userPreferences.updateStreak()

                    // saves the game session
                    libraryRepository.insertSession(
                        GameSessionEntity(
                            gameId = gameId,
                            startTime = startTime,
                            endTime = endTime,
                            durationMinutes = durationMinutes
                        )
                    )

                    // adds xps
                    val userXpData = userPreferences.userXp.first()
                    val xpDiff = XpSystem.xpForSession(durationMinutes, userXpData.dayStreak)
                    userPreferences.addXp(xpDiff)
                }
            }
            _uiState.update { it.copy(isSessionActive = false, sessionStartTime = null) }
        } else {
            // starts a new session
            _uiState.update {
                it.copy(isSessionActive = true, sessionStartTime = System.currentTimeMillis())
            }
        }
    }

    // DEBUG FUNCTION: REMOVE IN PRODUCTION
    // ADDS AN HOUR SESSION OF THE GAME IN THE DATABASE
    fun onDebugSession() {
        val gameId = _uiState.value.game?.id ?: return

        viewModelScope.launch {
            userPreferences.updateStreak()

            val now = LocalDateTime.now()
            val sessions = (0..6).map { daysAgo ->
                val randomHour = (8..22).random()
                val randomMinute = (0..59).random()

                var startDateTime = now.minusDays(daysAgo.toLong())
                    .withHour(randomHour)
                    .withMinute(randomMinute)
                    .withSecond(0)

                // makes sure that the session is not in the future
                if (startDateTime.isAfter(now)) {
                    startDateTime = now.minusHours(1)
                }

                val maxDuration = ChronoUnit.MINUTES.between(startDateTime, now).toInt()
                val duration = (1..maxOf(1, minOf(180, maxDuration))).random()

                val startTime = startDateTime.toInstant(ZoneOffset.UTC).toEpochMilli()

                GameSessionEntity(
                    gameId = gameId,
                    startTime = startTime,
                    endTime = startTime + duration * 60_000L,
                    durationMinutes = duration
                )
            }

            sessions.forEach {
                libraryRepository.insertSession(it)

                val userXpData = userPreferences.userXp.first()
                val xpDiff = XpSystem.xpForSession(it.durationMinutes, userXpData.dayStreak)
                userPreferences.addXp(xpDiff)
            }
        }
    }

    private fun Long.deltaToMinutes(endTime: Long) = ((endTime - this) / 1000 / 60).toInt()
}