package com.example.savestate.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "game_sessions",
    foreignKeys = [
        ForeignKey(
            entity = UserGameEntity::class,
            parentColumns = ["gameId"],
            childColumns = ["gameId"],
            // so that if the game gets removed
            // the related sessions gets removes too
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val sessionId: Int = 0,
    val gameId: Int,
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int
)