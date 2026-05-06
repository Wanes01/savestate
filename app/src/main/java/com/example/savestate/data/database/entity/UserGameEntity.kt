package com.example.savestate.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.savestate.data.models.GameStatus

@Entity(tableName = "user_games")
data class UserGameEntity(
    @PrimaryKey
    val gameId: Int,

    // game info cached from RAWG to avoid re-fetching for the library screen
    // (to make the game details available offline)
    val name: String,
    val backgroundImage: String?,
    val rating: Float,
    val ratingsCount: Int,
    // genres, platforms, developers and publishers
    // gets stored as comma separated strings
    val genres: String,
    val platforms: String,
    val developers: String,
    val publishers: String,
    val description: String?,
    val website: String?,
    val playtime: Int,
    val metacritic: Int?,
    val released: String?,
    val esrbRating: String?,

    // user data
    val status: GameStatus,
    val personalRating: Float?,
    val notes: String,

    // timestamps
    val addedAt: Long,
    val startedAt: Long?,
    val completedAt: Long?
)