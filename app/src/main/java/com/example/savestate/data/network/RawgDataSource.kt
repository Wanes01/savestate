package com.example.savestate.data.network

import com.example.savestate.data.models.RawgGameListResponse
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
     */
    suspend fun searchGames(
        query: String,
        page: Int = 1,
        pageSize: Int = 20
    ): RawgGameListResponse =
        httpClient.get("api/games") {
            parameter("search", query)
            parameter("page", page)
            parameter("page_size", pageSize)
        }.body()
}