package com.example.savestate.ui.screens.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.database.entity.UserGameEntity
import com.example.savestate.data.models.AchievementProgress
import com.example.savestate.data.models.GameStatus
import com.example.savestate.data.repositories.LibraryRepository
import com.example.savestate.domain.ActiveSession
import com.example.savestate.domain.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LibraryGameItem(
    val game: UserGameEntity,
    // null if the game does not have achievements
    val achievementProgress: AchievementProgress?,
    val totalMinutes: Int = 0
)

data class LibraryUiState(
    val filter: GameStatus? = null, // shows all if null
    val allGames: List<LibraryGameItem> = emptyList(),
    val isLoading: Boolean = true,
) {
    val filteredGames: List<LibraryGameItem>
        get() = if (filter == null)
            allGames
        else
            allGames.filter { it.game.status == filter }
}

class LibraryViewModel(
    private val libraryRepository: LibraryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()
    val activeSession: StateFlow<ActiveSession?> = sessionManager.activeSession

    init {
        viewModelScope.launch {
            combine(
                libraryRepository.getAllGames(),
                libraryRepository.getAchievementProgress(),
                libraryRepository.getPlaytimeByGame()
            ) { games, progressList, playtimeList ->
                val progressMap = progressList.associateBy { it.gameId }
                val playtimeMap = playtimeList.associateBy { it.gameId }
                games.map { game ->
                    LibraryGameItem(
                        game = game,
                        achievementProgress = progressMap[game.gameId],
                        totalMinutes = playtimeMap[game.gameId]?.totalMinutes ?: 0
                    )
                }
            }.collect { items ->
                _uiState.update { it.copy(allGames = items, isLoading = false) }
            }
        }
    }

    fun onFilterSelected(status: GameStatus?) {
        _uiState.update { current ->
            current.copy(filter = if (current.filter == status) null else status)
        }
    }

    fun stopActiveSession() {
        viewModelScope.launch {
            sessionManager.stopSession()
        }
    }
}