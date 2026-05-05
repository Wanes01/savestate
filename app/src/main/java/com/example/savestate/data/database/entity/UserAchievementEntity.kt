package com.example.savestate.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "user_achievements",
    foreignKeys = [
        ForeignKey(
            entity = UserGameEntity::class,
            parentColumns = ["gameId"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId")]
)
data class UserAchievementEntity(
    @PrimaryKey
    val achievementId: Int,
    val gameId: Int,
    val name: String,
    val description: String,
    val image: String?,
    val percent: Float,
    val isCompleted: Boolean
)