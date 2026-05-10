package com.example.savestate.data.repositories

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.savestate.R
import com.example.savestate.data.database.dao.GameSessionDao
import com.example.savestate.data.database.dao.UserAchievementDao
import com.example.savestate.data.database.dao.UserGameDao
import com.example.savestate.data.datastore.UserPreferences
import com.example.savestate.data.models.UserData
import com.example.savestate.data.models.UserXp
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val applicationScope: CoroutineScope,
    private val userPreferences: UserPreferences,
    private val firebaseAuth: FirebaseAuth,
    private val firestoreSyncRepository: FirestoreSyncRepository,
    private val userGameDao: UserGameDao,
    private val userAchievementDao: UserAchievementDao,
    private val gameSessionDao: GameSessionDao,
) {
    /**
     * Registers a new user with email and password.
     * Throws an exception if the email is already in use or the password is too weak.
     */
    suspend fun registerWithEmail(email: String, password: String): Result<UserData> =
        try {
            val result = firebaseAuth
                .createUserWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = result.user
                ?: return Result.failure(Exception("Registration failed: user is null."))

            val userData = firebaseUser.toUserData()
            // the user is considered logged in after a successful registration
            userPreferences.saveUser(userData)
            Result.success(userData)

        } catch (_: FirebaseAuthUserCollisionException) {
            Result.failure(Exception("This email address is already in use."))
        } catch (_: FirebaseAuthWeakPasswordException) {
            Result.failure(Exception("Password is too weak."))
        } catch (e: Exception) {
            Result.failure(e)
        }

    /**
     * Logs in an existing user with email and password.
     * Throws an exception in case of a login error.
     */
    suspend fun loginWithEmail(email: String, password: String): Result<UserData> =
        try {
            val result = firebaseAuth
                .signInWithEmailAndPassword(email, password)
                .await()

            val firebaseUser = result.user
                ?: return Result.failure(Exception("Login failed: user is null."))

            val userData = firebaseUser.toUserData()
            userPreferences.saveUser(userData)
            // tries to sync user's data with firestore
            syncAfterLogin(userData)
            Result.success(userData)

        } catch (_: FirebaseAuthInvalidUserException) {
            Result.failure(Exception("No user found with this email."))
        } catch (_: FirebaseAuthInvalidCredentialsException) {
            // wrong password
            Result.failure(Exception("Incorrect password."))
        } catch (e: Exception) {
            Result.failure(e)
        }

    /**
     * Logs in an existing user with Google account credentials.
     * Throws an exception in case of a login error.
     */
    suspend fun loginWithGoogle(context: Context): Result<UserData> {
        return try {
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetSignInWithGoogleOption
                .Builder(serverClientId = context.getString(R.string.default_web_client_id))
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val credentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val googleIdTokenCredential = GoogleIdTokenCredential
                .createFrom(credentialResponse.credential.data)

            val firebaseCredential = GoogleAuthProvider
                .getCredential(googleIdTokenCredential.idToken, null)

            val result = firebaseAuth.signInWithCredential(firebaseCredential).await()

            val firebaseUser = result.user
                ?: return Result.failure(Exception("Google sign-in failed."))

            val userData = firebaseUser.toUserData()
            userPreferences.saveUser(userData)
            // tries to sync user's data with firestore
            syncAfterLogin(userData)
            Result.success(userData)

        } catch (_: GetCredentialCancellationException) {
            Result.failure(Exception("Sign-in cancelled."))
        } catch (e: GetCredentialException) {
            Result.failure(Exception("Google sign-in failed: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Signs out the current user from Firebase and clears local data.
     */
    suspend fun logout() {
        firebaseAuth.signOut()
        userPreferences.clearUser()
    }

    /**
     * Maps a FirebaseUser to the app's UserData model
     */
    private fun FirebaseUser.toUserData() = UserData(
        isLoggedIn = true,
        userId = uid,
        displayName = displayName ?: email?.substringBefore("@") ?: "User",
        email = email ?: "",
        photoUrl = photoUrl?.toString()
    )

    /**
     * Synchronizes the firebase user with the
     * user data saves locally.
     * Should be called on app initialization
     */
    suspend fun syncAuthState() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            userPreferences.saveUser(firebaseUser.toUserData())
        } else {
            userPreferences.clearUser()
        }
    }

    /**
     * Syncs the firestore user's data.
     * Retrieves achievements, games, sessions and xp information
     * to write in the local database.
     * Does nothing if is the first time the user logs in.
     */
    fun syncAfterLogin(userData: UserData) {
        applicationScope.launch {
            try {
                val remote = firestoreSyncRepository.downloadUserData(userData.userId)

                // if there is nothing in firestore then this is the first login
                // does nothing
                if (remote.games.isEmpty() && remote.sessions.isEmpty()) {
                    return@launch
                }

                // replaces all data: firestore becomes the source of truth
                userGameDao.deleteAllGames()       // cascade su achievements
                gameSessionDao.deleteAllSessions()

                remote.games.forEach { userGameDao.upsertGame(it) }
                if (remote.achievements.isNotEmpty()) {
                    userAchievementDao.upsertAchievements(remote.achievements)
                }
                remote.sessions.forEach { gameSessionDao.upsertSession(it) }

                val localXp = userPreferences.userXp.first().xp
                if (remote.xp > localXp) {
                    userPreferences.saveXpData(UserXp(xp = remote.xp, dayStreak = 0))
                }

            } catch (e: Exception) {
                Log.e("FirestoreSync", "Sync failed: ${e.message}", e)
            }
        }
    }
}