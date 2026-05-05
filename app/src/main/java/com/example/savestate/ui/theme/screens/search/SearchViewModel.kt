package com.example.savestate.ui.theme.screens.search

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.savestate.data.models.RawgGame
import com.example.savestate.data.models.SearchFilters
import com.example.savestate.data.repositories.RawgRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.net.UnknownHostException

/**
 * Represents the UI state of the search screen
 *
 * @param games the list of games fetched from RAWG
 * @param isLoading true during the first load (new query)
 * @param isLoadingMore true while fetching the next page of games
 * @param error non-null if an error occurs
 * @param isNetworkError true if the error is caused by a connection error
 * @param query the current search query typed by the user
 * @param currentPage the last page fetched from RAWG
 * @param hasMore true if RAWG has more pages to fetch
 * @param filters the filters to use in the games search
 * @param isFilterSheetOpen true if the filter sheet is open
 */
data class SearchUiState(
    val games: List<RawgGame> = emptyList(),
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val query: String = "",
    val currentPage: Int = 1,
    val hasMore: Boolean = true,

    val error: String? = null,
    val isNetworkError: Boolean = false,

    val filters: SearchFilters = SearchFilters(),
    val isFilterSheetOpen: Boolean = false
)

class SearchViewModel(private val rawgRepository: RawgRepository) : ViewModel() {

    companion object {
        private const val QUERY_INPUT_REQUEST_DELAY_MS: Long = 500
        private const val MIN_QUERY_LENGTH = 3
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

        // too short queries are not useful. Waits for the user to type more characters.
        if (query.isNotBlank() && query.length < MIN_QUERY_LENGTH) {
            _uiState.update { it.copy(games = emptyList(), currentPage = 1, hasMore = true) }
            return
        }

        // loads the next page of games if possible
        val page = if (reset) 1 else _uiState.value.currentPage + 1

        if (reset) {
            _uiState.update { it.copy(isLoading = true, error = null) }
        } else {
            _uiState.update { it.copy(isLoadingMore = true) }
        }

        rawgRepository.searchGames(query, page, filters = _uiState.value.filters)
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
                        isNetworkError = error is UnknownHostException,
                        error = when (error) {
                            /*
                            It is common for users to type a few characters and then delete
                            them immediately, so it is believed that displaying this
                            exception may be redundant
                             */
                            is CancellationException -> null
                            is UnknownHostException -> "No internet connection"
                            else -> error.message
                        }
                    )
                }
            }
    }

    /**
     * Loads the games when the query is blank
     */
    fun loadDefaultGames() {
        viewModelScope.launch { searchGames(reset = true) }
    }

    /**
     * Clears the errors to show
     */
    fun clearError() {
        _uiState.update { it.copy(error = null, isNetworkError = false) }
    }

    /**
     * Opens or closes the filter bottom sheet.
     */
    fun setFilterSheetOpen(open: Boolean) {
        _uiState.update { it.copy(isFilterSheetOpen = open) }
    }

    /**
     * Applies the given filters and re-fetches the games from page 1.
     */
    fun applyFilters(filters: SearchFilters) {
        _uiState.update { it.copy(filters = filters, isFilterSheetOpen = false) }
        viewModelScope.launch { searchGames(reset = true) }
    }

}