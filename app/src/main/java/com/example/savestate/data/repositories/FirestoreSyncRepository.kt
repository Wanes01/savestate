package com.example.savestate.data.repositories

import com.example.savestate.data.database.entity.GameSessionEntity
import com.example.savestate.data.database.entity.UserAchievementEntity
import com.example.savestate.data.database.entity.UserGameEntity
import com.example.savestate.data.utils.toFirestoreMap
import com.example.savestate.data.utils.toGameSessionEntity
import com.example.savestate.data.utils.toUserAchievementEntity
import com.example.savestate.data.utils.toUserGameEntity
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class FirestoreUserData(
    val xp: Int,
    val games: List<UserGameEntity>,
    val achievements: List<UserAchievementEntity>,
    val sessions: List<GameSessionEntity>
)

class FirestoreSyncRepository(private val firestore: FirebaseFirestore) {

    /**
     * Overwrites firestore data about the specified user.
     */
    suspend fun uploadUserData(
        userId: String,
        games: List<UserGameEntity>,
        achievements: List<UserAchievementEntity>,
        sessions: List<GameSessionEntity>,
        xp: Int
    ) {
        withContext(Dispatchers.IO) {
            val userRef = firestore.collection("users").document(userId)

            // deletes all content before writing is again
            deleteCollection(userRef.collection("games"))
            deleteCollection(userRef.collection("achievements"))
            deleteCollection(userRef.collection("sessions"))

            val batch = firestore.batch()

            batch.set(userRef, mapOf("xp" to xp))

            // writes the data of the games in the library
            games.forEach { game ->
                val ref = userRef.collection("games").document(game.gameId.toString())
                batch.set(ref, game.toFirestoreMap())
            }

            // writes achievement data
            achievements.forEach { achievement ->
                val ref = userRef.collection("achievements")
                    .document(achievement.achievementId.toString())
                batch.set(ref, achievement.toFirestoreMap())
            }

            // write sessions data
            sessions.forEach { session ->
                val ref = userRef.collection("sessions")
                    .document(session.sessionId.toString())
                batch.set(ref, session.toFirestoreMap())
            }

            // await the end of the writing operation on firestore
            batch.commit().await()
        }
    }

    /**
     * Deletes a specified collection on firebase
     */
    private suspend fun deleteCollection(collectionRef: CollectionReference) {
        val documents = collectionRef.get().await()
        if (documents.isEmpty) return
        val batch = firestore.batch()
        documents.forEach { batch.delete(it.reference) }
        batch.commit().await()
    }

    /**
     * Download the data of the specified firestore user
     * and returns it as a FirestoreUserData
     */
    suspend fun downloadUserData(userId: String): FirestoreUserData =
        withContext(Dispatchers.IO) {
            val userRef = firestore.collection("users").document(userId)

            val profileDoc = userRef.get().await()
            val xp = profileDoc.getLong("xp")?.toInt() ?: 0

            val games = userRef.collection("games").get().await()
                .documents.mapNotNull { it.toUserGameEntity() }

            val achievements = userRef.collection("achievements").get().await()
                .documents.mapNotNull { it.toUserAchievementEntity() }

            val sessions = userRef.collection("sessions").get().await()
                .documents.mapNotNull { it.toGameSessionEntity() }

            FirestoreUserData(xp, games, achievements, sessions)
        }
}