package com.example.savestate.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RawgGenre(
    val id: Int,
    val name: String
)

/**
 * A game fetched from the RAWG api.
 *
 * @param id the game's identifier.
 * @param name the game's name.
 * @param backgroundImage the game's background image to use as a preview
 * @param rating the average rating of the game
 * @param ratingsCount how many people rated the game
 * @param genres a list of genres that can describe the game
 * @param released the release date of the game
 * @param metacritic the rating that metacritic gave to the game
 */
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

/**
 * The RAWG's reponse after a /games request
 *
 * @param count how many games are listed in the response
 * @param next null if there are more games that can be fetched
 * @param results the list of fetched games
 */
@Serializable
data class RawgGameListResponse(
    val count: Int,
    val next: String?,
    val results: List<RawgGame>
)

/**
 * Information about a specific game
 */
@Serializable
data class RawgGameDetail(
    val id: Int,
    val name: String,
    @SerialName("background_image")
    val backgroundImage: String?,
    val rating: Float,
    @SerialName("ratings_count")
    val ratingsCount: Int,
    val genres: List<RawgGenre>,
    val platforms: List<RawgPlatformWrapper>?,
    val developers: List<RawgCompany>,
    val publishers: List<RawgCompany>,
    @SerialName("description_raw")
    val descriptionRaw: String?,
    val website: String?,
    val playtime: Int,
    val metacritic: Int?,
    val released: String?,
    @SerialName("esrb_rating")
    val esrbRating: RawgEsrbRating?
)

@Serializable
data class RawgPlatformWrapper(
    val platform: RawgPlatformInfo
)

@Serializable
data class RawgPlatformInfo(
    val id: Int,
    val name: String
)

@Serializable
data class RawgCompany(
    val id: Int,
    val name: String
)

@Serializable
data class RawgEsrbRating(
    val name: String
)
