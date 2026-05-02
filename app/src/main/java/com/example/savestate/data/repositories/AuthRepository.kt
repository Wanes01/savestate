package com.example.savestate.data.repositories

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.savestate.data.datastore.UserData
import com.example.savestate.data.datastore.UserPreferences
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import com.example.savestate.R
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

class AuthRepository(
    private val userPreferences: UserPreferences,
    private val firebaseAuth: FirebaseAuth,
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

    suspend fun syncAuthState() {
        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser != null) {
            userPreferences.saveUser(firebaseUser.toUserData())
        } else {
            userPreferences.clearUser()
        }
    }
}