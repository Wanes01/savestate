package com.example.savestate.data.repositories

import com.example.savestate.data.models.RawgAchievement
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

    suspend fun getAllGameAchievements(gameId: Int): Result<List<RawgAchievement>> {
        return try {
            val achievements = mutableListOf<RawgAchievement>()
            var page = 1
            var hasMore = true

            while (hasMore) {
                val response = dataSource.getGameAchievements(gameId, page)
                achievements.addAll(response.results)
                hasMore = response.next != null
                page++
            }

            Result.success(achievements)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}