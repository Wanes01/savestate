package com.example.savestate.ui.theme.screens.search

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.savestate.AppViewModel
import com.example.savestate.ui.theme.components.search.GameCard
import com.example.savestate.ui.theme.components.search.SearchBar
import com.example.savestate.ui.theme.components.search.SearchEmptyPrompt
import com.example.savestate.ui.theme.components.search.SearchErrorMessage
import com.example.savestate.ui.theme.components.search.SearchNoResults
import org.koin.androidx.compose.koinViewModel

// at how many items from the end of the lazy list will trigger the load more function
private const val LOAD_MORE_OFFSET = 3

@Composable
fun SearchScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel,
    onGameClick: (Int) -> Unit
) {
    LaunchedEffect(Unit) {
        appViewModel.setTopBar(
            title = "Find games"
        )
    }
    val searchViewModel: SearchViewModel = koinViewModel()
    val uiState by searchViewModel.uiState.collectAsStateWithLifecycle()
    // to know what the user is seeing
    val listState = rememberLazyListState()

    // triggers loadMore when the user is LOAD_MORE_OFFSET items from the end
    // triggers only when listState.layoutInfo changes and triggers recomposition
    // only when the boolean value changes
    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisible = listState
                .layoutInfo
                .visibleItemsInfo
                .lastOrNull()
                ?.index ?: 0
            val total = listState.layoutInfo.totalItemsCount
            lastVisible >= total - LOAD_MORE_OFFSET && total > 0
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) searchViewModel.loadMore()
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // search bar
            SearchBar(
                query = uiState.query,
                onQueryChange = { query -> searchViewModel.onQueryChange(query) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            HorizontalDivider()

            // content area
            when {
                // new query, first load
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }

                // an error occurred
                uiState.error != null -> {
                    SearchErrorMessage(
                        error = uiState.error!!, // it enters here only if an error occurs
                        onDismiss = { searchViewModel.clearError() }
                    )
                }

                uiState.query.isBlank() -> {
                    SearchEmptyPrompt()
                }

                // tells the user that the research did not produce results
                uiState.games.isEmpty() -> {
                    SearchNoResults(query = uiState.query)
                }

                // the list of games have been fetched successfully
                else -> {
                    LazyColumn(
                        state = listState,
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(uiState.games, key = { it.id }) { game ->
                            GameCard(
                                game = game,
                                // user selected a specific game
                                onClick = { onGameClick(game.id) }
                            )
                        }

                        // spinner at the bottom of the page to load more games
                        // from the same query
                        if (uiState.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // floating filter button
        ExtendedFloatingActionButton(
            onClick = { /* TODO -> redirects to the game page */ },
            icon = { Icon(Icons.Default.FilterList, contentDescription = "Filters") },
            text = { Text("Filters") },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }
}

