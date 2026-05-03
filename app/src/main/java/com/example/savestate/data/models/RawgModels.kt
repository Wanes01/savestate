package com.example.savestate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawgGenre(
    val id: Int,
    val name: String
)

@Serializable
data class RawgGame(
    val id: Int,
    val name: String,
    @SerialName("background_image")
    val backgroundImage: String?,
    val rating: Float,
    @SerialName("ratings_count")
    val ratingsCount: Int,
    val genres: List<RawgGenre>,
    val released: String?,
    val metacritic: Int?
)

@Serializable
data class RawgGameListResponse(
    val count: Int,
    val next: String?,
    val results: List<RawgGame>
)
