package com.example.savestate.data.repositories

import com.example.savestate.data.models.RawgGameListResponse
import com.example.savestate.data.network.RawgDataSource

class RawgRepository(private val dataSource: RawgDataSource) {
    suspend fun searchGames(
        query: String,
        page: Int = 1,
        pageSize: Int = 20
    ): Result<RawgGameListResponse> {
        return try {
            Result.success(dataSource.searchGames(query, page, pageSize))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}