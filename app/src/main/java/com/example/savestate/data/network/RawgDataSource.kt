package com.example.savestate.data.network

import com.example.savestate.data.models.RawgGameListResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class RawgDataSource(private val httpClient: HttpClient) {

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