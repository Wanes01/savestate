package com.example.savestate.ui.theme.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.models.RawgGame
import com.example.savestate.data.repositories.RawgRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Represents the UI state of the search screen
 *
 * @param games the list of games fetched from RAWG
 * @param isLoading true during the first load (new query)
 * @param isLoadingMore true while fetching the next page of games
 * @param error non-null if an error occurs
 * @param query the current search query typed by the user
 * @param currentPage the last page fetched from RAWG
 * @param hasMore true if RAWG has more pages to fetch
 */
data class SearchUiState(
    val games: List<RawgGame> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: String? = null,
    val query: String = "",
    val currentPage: Int = 1,
    val hasMore: Boolean = true
)

class SearchViewModel(private val rawgRepository: RawgRepository) : ViewModel() {

    companion object {
        private const val QUERY_INPUT_REQUEST_DELAY_MS: Long = 500
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    /*
    Used to cancel coroutines in order to
    avoid making a request at every character input
     */
    private var searchJob: Job? = null

    /**
     * Called every time the user types in the search bar.
     * Note: it cancels any pending search and stars a new one after
     * some time, so that RAWG is only contacted wehn the user pauses typing.
     */
    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }

        /* debounce: waits some time before making
        * the request: the user may have not finished
        * writing yet */
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(QUERY_INPUT_REQUEST_DELAY_MS)
            searchGames(reset = true)
        }
    }

    /**
     * Called when the user scrolls near the end of the list.
     * Fetches the next page from RWAG and appends it to the current list.
     * Does nothing if already loading or if there are no more pages.
     */
    fun loadMore() {
        if (_uiState.value.isLoadingMore || !_uiState.value.hasMore) return
        viewModelScope.launch { searchGames(reset = false) }
    }

    /**
     * Fetches games from RAWG for the current query.
     *
     * @param reset clears the current list and fetches the first page of RAWG (new query) if true,
     * fetches the next page and appends the results to the existing list otherwise.
     */
    private suspend fun searchGames(reset: Boolean) {
        val query = _uiState.value.query
        if (query.isBlank()) {
            // resets the page when the search bar gets empty
            _uiState.update { SearchUiState() }
            return
        }

        // loads the next page of games if possible
        val page = if (reset) 1 else _uiState.value.currentPage + 1

        if (reset) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        } else {
            _uiState.update { it.copy(isLoadingMore = true) }
        }

        rawgRepository.searchGames(query, page)
            .onSuccess { response ->
                _uiState.update {
                    it.copy(
                        // creates a single dynamic list
                        games = if (reset) response.results else it.games + response.results,
                        isLoading = false,
                        isLoadingMore = false,
                        currentPage = page,
                        hasMore = response.next != null
                    )
                }
            }
            .onFailure { error ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoadingMore = false,
                        error = error.message
                    )
                }
            }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}