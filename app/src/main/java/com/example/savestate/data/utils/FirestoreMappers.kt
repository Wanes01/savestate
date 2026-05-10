package com.example.savestate.data.utils

import com.example.savestate.data.database.entity.GameSessionEntity
import com.example.savestate.data.database.entity.UserAchievementEntity
import com.example.savestate.data.database.entity.UserGameEntity
import com.example.savestate.data.models.GameStatus
import com.google.firebase.firestore.DocumentSnapshot

/**
 * Mappers for data conversion between firestore's collections
 * and the local models
 */

// user data
fun UserGameEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "gameId" to gameId,
    "name" to name,
    "backgroundImage" to backgroundImage,
    "rating" to rating,
    "ratingsCount" to ratingsCount,
    "genres" to genres,
    "platforms" to platforms,
    "developers" to developers,
    "publishers" to publishers,
    "description" to description,
    "website" to website,
    "playtime" to playtime,
    "metacritic" to metacritic,
    "released" to released,
    "esrbRating" to esrbRating,
    "status" to status.name,
    "personalRating" to personalRating,
    "notes" to notes,
    "addedAt" to addedAt,
    "startedAt" to startedAt,
    "completedAt" to completedAt
)

fun DocumentSnapshot.toUserGameEntity(): UserGameEntity? = try {
    UserGameEntity(
        gameId = getLong("gameId")!!.toInt(),
        name = getString("name")!!,
        backgroundImage = getString("backgroundImage"),
        rating = getDouble("rating")!!.toFloat(),
        ratingsCount = getLong("ratingsCount")!!.toInt(),
        genres = getString("genres")!!,
        platforms = getString("platforms")!!,
        developers = getString("developers")!!,
        publishers = getString("publishers")!!,
        description = getString("description"),
        website = getString("website"),
        playtime = getLong("playtime")!!.toInt(),
        metacritic = getLong("metacritic")?.toInt(),
        released = getString("released"),
        esrbRating = getString("esrbRating"),
        status = GameStatus.valueOf(getString("status")!!),
        personalRating = getDouble("personalRating")?.toFloat(),
        notes = getString("notes") ?: "",
        addedAt = getLong("addedAt")!!,
        startedAt = getLong("startedAt"),
        completedAt = getLong("completedAt")
    )
} catch (e: Exception) {
    null
}

// user's achievements
fun UserAchievementEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "achievementId" to achievementId,
    "gameId" to gameId,
    "name" to name,
    "description" to description,
    "image" to image,
    "percent" to percent,
    "isCompleted" to isCompleted
)

fun DocumentSnapshot.toUserAchievementEntity(): UserAchievementEntity? = try {
    UserAchievementEntity(
        achievementId = getLong("achievementId")!!.toInt(),
        gameId = getLong("gameId")!!.toInt(),
        name = getString("name")!!,
        description = getString("description")!!,
        image = getString("image"),
        percent = getDouble("percent")!!.toFloat(),
        isCompleted = getBoolean("isCompleted")!!
    )
} catch (e: Exception) {
    null
}

// user's game sessions
fun GameSessionEntity.toFirestoreMap(): Map<String, Any?> = mapOf(
    "sessionId" to sessionId,
    "gameId" to gameId,
    "startTime" to startTime,
    "endTime" to endTime,
    "durationMinutes" to durationMinutes
)

fun DocumentSnapshot.toGameSessionEntity(): GameSessionEntity? = try {
    GameSessionEntity(
        sessionId = getLong("sessionId")!!.toInt(),
        gameId = getLong("gameId")!!.toInt(),
        startTime = getLong("startTime")!!,
        endTime = getLong("endTime")!!,
        durationMinutes = getLong("durationMinutes")!!.toInt()
    )
} catch (e: Exception) {
    null
}