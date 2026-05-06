package com.example.savestate.ui.theme.screens.gamedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.database.entity.GameSessionEntity
import com.example.savestate.data.database.entity.UserAchievementEntity
import com.example.savestate.data.database.entity.UserGameEntity
import com.example.savestate.data.models.GameStatus
import com.example.savestate.data.models.RawgGameDetail
import com.example.savestate.data.repositories.LibraryRepository
import com.example.savestate.data.repositories.RawgRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameDetailUiState(
    // base state
    val game: RawgGameDetail? = null,
    val isLoading: Boolean = true,
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
    private val libraryRepository: LibraryRepository
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
     * Fetches the details of a single game from RAWG.
     * Called once when the screen is first shown.
     *
     * @param gameId the RAWG id of the game to fetch
     */
    fun loadGame(gameId: Int) {
        // fetches the game form rawg
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            rawgRepository.getGameDetail(gameId)
                .onSuccess { game ->
                    _uiState.update { it.copy(game = game, isLoading = false) }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }

        // observe the library state
        viewModelScope.launch {
            libraryRepository.getGameById(gameId).collect { userGame ->
                _uiState.update { it.copy(userGame = userGame) }
            }
        }

        // observes achievements, minutes played and last session if the game is in the library
        viewModelScope.launch {
            libraryRepository.getGameById(gameId).collect { userGame ->
                if (userGame != null) {
                    // achievements
                    launch {
                        libraryRepository.getAchievementsByGame(gameId).collect { achievements ->
                            _uiState.update { it.copy(achievements = achievements) }
                        }
                    }
                    // number of completed achievements
                    launch {
                        libraryRepository.getCompletedAchievementsCount(gameId).collect { count ->
                            _uiState.update { it.copy(completedAchievementsCount = count) }
                        }
                    }
                    // total playtime in minutes
                    launch {
                        libraryRepository.getTotalMinutesByGame(gameId).collect { minutes ->
                            _uiState.update { it.copy(totalMinutesPlayed = minutes) }
                        }
                    }
                    // the last registered gaming session
                    launch {
                        libraryRepository.getLastSessionByGame(gameId).collect { session ->
                            _uiState.update { it.copy(lastSession = session) }
                        }
                    }
                }
            }
        }
    }

    // LIBRARY ACTIONS

    /**
     * Adds the game to the library with the given status,
     * or updates the status if already in the library.
     * If the new status is equal to the previews one, removes the game
     * from the library.
     */
    fun onStatusSelected(status: GameStatus) {
        // if no data is available there is nothing to do
        val game = _uiState.value.game ?: return
        val currentUserGame = _uiState.value.userGame

        viewModelScope.launch {
            when {
                // same status tapped again, remove from library
                currentUserGame?.status == status -> {
                    libraryRepository.removeGame(currentUserGame)
                }
                // already in library, updates status
                currentUserGame != null -> {
                    libraryRepository.updateStatus(game.id, status)
                }
                // adds the game to the library
                else -> {
                    libraryRepository.addGame(game, status)
                }
            }
        }
    }

    fun onNotesChanged(notes: String) {
        val gameId = _uiState.value.game?.id ?: return
        _pendingNotes.value = gameId to notes
    }

    fun onPersonalRatingChanged(rating: Float) {
        val gameId = _uiState.value.game?.id ?: return
        viewModelScope.launch { libraryRepository.updatePersonalRating(gameId, rating) }
    }

    fun onAchievementToggled(achievementId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            libraryRepository.updateAchievementCompleted(achievementId, isCompleted)
        }
    }

    // SESSION TIMER ACTIONS

    /**
     * Starts/stops the game session timer.
     * When stopped, saves the session to the database.
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
                    libraryRepository.insertSession(
                        GameSessionEntity(
                            gameId = gameId,
                            startTime = startTime,
                            endTime = endTime,
                            durationMinutes = durationMinutes
                        )
                    )
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

    private fun Long.deltaToMinutes(endTime: Long) = ((endTime - this) / 1000 / 60).toInt()
}