package com.example.savestate.data.repositories

import com.example.savestate.data.datastore.UserData
import com.example.savestate.data.datastore.UserPreferences
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val userPreferences: UserPreferences,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    val userData: Flow<UserData> = userPreferences.userData

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
}