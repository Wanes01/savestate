package com.example.savestate.data.repositories

import com.example.savestate.data.models.RawgAchievementsResponse
import com.example.savestate.data.models.RawgGameDetail
import com.example.savestate.data.models.RawgGameListResponse
import com.example.savestate.data.models.SearchFilters
import com.example.savestate.data.network.RawgDataSource

class RawgRepository(private val dataSource: RawgDataSource) {
    suspend fun searchGames(
        query: String,
        page: Int = 1,
        pageSize: Int = 20,
        filters: SearchFilters = SearchFilters()
    ): Result<RawgGameListResponse> {
        return try {
            Result.success(dataSource.searchGames(query, page, pageSize, filters))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGameDetail(gameId: Int): Result<RawgGameDetail> {
        return try {
            Result.success(dataSource.getGameDetail(gameId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getGameAchievements(gameId: Int): Result<RawgAchievementsResponse> {
        return try {
            Result.success(dataSource.getGameAchievements(gameId))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}