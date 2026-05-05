package com.example.savestate.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.savestate.data.models.GameStatus

@Entity(tableName = "user_games")
data class UserGameEntity(
    @PrimaryKey
    val gameId: Int,

    // game info cached from RAWG to avoid re-fetching for the library screen
    val name: String,
    val backgroundImage: String?,
    val rating: Float,
    val genres: String, // gets stored as a comma separated string

    // user data
    val status: GameStatus,
    val personalRating: Float?,
    val notes: String,

    // timestamps
    val addedAt: Long,
    val startedAt: Long?,
    val completedAt: Long?
)