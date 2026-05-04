package com.example.savestate.data.network

import com.example.savestate.data.models.RawgGameListResponse
import com.example.savestate.data.models.RawgOrdering
import com.example.savestate.data.models.SearchFilters
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

/**
 * This class is responsible for making http request to
 * the RAWG endpoints
 */
class RawgDataSource(private val httpClient: HttpClient) {

    /**
     * Fetches the list of games.
     *
     * @param query the query to search
     * @param page the current page of pageSize games
     * @param pageSize how many games to include in the response
     * @param filters the filters to apply to the search
     */
    suspend fun searchGames(
        query: String,
        page: Int = 1,
        pageSize: Int = 20,
        filters: SearchFilters = SearchFilters()
    ): RawgGameListResponse =
        httpClient.get("api/games") {
            parameter("search", query)
            parameter("page", page)
            parameter("page_size", pageSize)

            // appends filter parameters if they are actually set
            if (filters.genres.isNotEmpty()) {
                parameter("genres", filters.genres.joinToString(",") { it.slug })
            }
            if (filters.platforms.isNotEmpty()) {
                parameter("platforms", filters.platforms.joinToString(",") { it.id.toString() })
            }
            if (filters.minRating > 0f) {
                parameter("rating", "${filters.minRating},5")
            }
            if (filters.ordering != RawgOrdering.RELEVANCE) {
                parameter("ordering", filters.ordering.value)
            }
        }.body()
}