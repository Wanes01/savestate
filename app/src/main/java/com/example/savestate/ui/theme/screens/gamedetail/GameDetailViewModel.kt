package com.example.savestate.ui.theme.screens.gamedetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.models.RawgGameDetail
import com.example.savestate.data.repositories.RawgRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class GameDetailUiState(
    val game: RawgGameDetail? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class GameDetailViewModel(
    private val rawgRepository: RawgRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(GameDetailUiState())
    val uiState: StateFlow<GameDetailUiState> = _uiState.asStateFlow()

    /**
     * Fetches the details of a single game from RAWG.
     * Called once when the screen is first shown.
     *
     * @param gameId the RAWG id of the game to fetch
     */
    fun loadGame(gameId: Int) {
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
    }
}