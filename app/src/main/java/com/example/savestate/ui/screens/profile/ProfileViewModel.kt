package com.example.savestate.ui.screens.profile

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.util.copy
import com.example.savestate.data.datastore.UserPreferences
import com.example.savestate.data.models.NotificationPreferences
import com.example.savestate.data.models.UserData
import com.example.savestate.notification.NotificationHelper
import com.example.savestate.notification.NotificationScheduler
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File

data class ProfileUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    /* timestamp updated every time a new photo is saved. AsyncImage uses this
    * as a cache key, so Coils reloads the image even if the file path has not changed */
    val photoRefreshKey: Long = 0L,
    val showNotifRationale: Boolean = false
)

/*
AndroidViewModel is used instead of ViewModel because we need the application
context to access internal storage and the FileProvider, which are unavailable
in a ViewModel
 */
class ProfileViewModel(
    private val application: Application,
    private val userPreferences: UserPreferences
) : AndroidViewModel(application) {

    companion object {
        private const val NICKNAME_MIN_LENGTH = 3
        private const val NICKNAME_MAX_LENGTH = 30
    }

    /*
    Holds the FileProvider URI of the temporary file passed to the camera intent.
    The camera writes the captured photo directly to this file.
     */
    private var pendingPhotoUri: Uri? = null

    private val _uiState = MutableStateFlow(ProfileUIState())
    val uiState: StateFlow<ProfileUIState> = _uiState.asStateFlow()

    val notificationPreferences: StateFlow<NotificationPreferences> =
        userPreferences.notificationPreferences
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = NotificationPreferences()
            )


    /**
     * Creates a temporary file in cache and returns its FileProvider uri,
     * which is passed to the camera intent as the output destination.
     */
    fun createTempPhotoUri(): Uri {
        val photoDir = application.cacheDir
            .resolve("profile_photos")
            .also { it.mkdirs() }
        val photoFile = File(photoDir, "profile_photo_${System.currentTimeMillis()}.jpg")
        val uri = FileProvider.getUriForFile(
            application,
            "${application.packageName}.fileprovider",
            photoFile
        )
        pendingPhotoUri = uri
        return uri
    }


    /**
     * Copies the temp file from cache to internal storage, then persists it.
     * To be used in case of camera input.
     * The photo gets written is the app cache, it does not get saved in the
     * user's storage.
     */
    fun onPhotoCaptured(firebaseUser: FirebaseUser?, currentUserData: UserData) {
        val tempUri = pendingPhotoUri ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { copyToInternalStorage(tempUri) }
                .onSuccess { savedFile ->
                    persistPhoto(firebaseUser, savedFile.toUri(), currentUserData)
                    pendingPhotoUri = null
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    /**
     * Copies the temp file from cache to internal storage, then persists it.
     * To be used in case of gallery input.
     */
    fun onGalleryPhotoPicked(
        firebaseUser: FirebaseUser?,
        sourceUri: Uri,
        currentUserData: UserData
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            runCatching { copyToInternalStorage(sourceUri) }
                .onSuccess { savedFile ->
                    persistPhoto(firebaseUser, savedFile.toUri(), currentUserData)
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Persists the new photo uri to datastore and attempts to update Firebase Auth.
     * Datastore is updated first as it is the source for the UI.
     * If the firebase update fails, the local photo is still saved and displayed correctly.
     */
    private suspend fun persistPhoto(
        firebaseUser: FirebaseUser?,
        photoUri: Uri,
        currentUserData: UserData
    ) {
        userPreferences.saveUser(currentUserData.copy(photoUrl = photoUri.toString()))

        runCatching {
            firebaseUser?.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setPhotoUri(photoUri)
                    .build()
            )?.await()
        }

        _uiState.update { it.copy(isLoading = false, photoRefreshKey = System.currentTimeMillis()) }
    }

    /**
     * Copies a file from the given uri to a fixed destination in internal storage.
     * Always overwrites the same file since only one profile photo is needed.
     */
    private fun copyToInternalStorage(sourceUri: Uri): File {
        val destDir = application.filesDir
            .resolve("profile_photos")
            .also { it.mkdirs() }
        val destFile = File(destDir, "profile_photo.jpg")

        application.contentResolver.openInputStream(sourceUri)?.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: error("Could not open input stream for URI: $sourceUri")

        return destFile
    }

    /**
     * Validates and saves the user's nickname to datastore.
     */
    fun updateNickname(newNickname: String, currentUserData: UserData) {
        val trimmed = newNickname.trim()

        if (trimmed.isBlank()) return
        if (trimmed == currentUserData.displayName) return
        if (trimmed.length < NICKNAME_MIN_LENGTH) return
        if (trimmed.length > NICKNAME_MAX_LENGTH) return

        viewModelScope.launch {
            userPreferences.saveUser(currentUserData.copy(displayName = trimmed))
        }
    }

    // notification settings handling

    // Chiamato quando l'utente tocca un toggle notifiche.
    // Se il permesso non è concesso mostra il rationale dialog invece di agire subito.
    fun onNotifToggled(
        context: Context,
        current: NotificationPreferences,
        updated: NotificationPreferences
    ) {
        if (!NotificationHelper.hasPermission(context)) {
            _uiState.update { it.copy(showNotifRationale = true) }
            return
        }
        applyNotifPreferences(context, current, updated)
    }

    private fun applyNotifPreferences(
        context: Context,
        current: NotificationPreferences,
        updated: NotificationPreferences
    ) {
        viewModelScope.launch {
            userPreferences.saveNotificationPreferences(updated)

            // streak
            if (updated.streakEnabled) {
                NotificationScheduler.scheduleStreakReminder(
                    context, updated.streakHour, updated.streakMinute
                )
            } else if (current.streakEnabled) {
                NotificationScheduler.cancelStreakReminder(context)
            }
        }
    }

    fun onStreakTimeChanged(
        context: Context,
        hour: Int, minute: Int,
        current: NotificationPreferences
    ) {
        val updated = current.copy(streakHour = hour, streakMinute = minute)
        applyNotifPreferences(context, current, updated)
    }

    fun onRationaleDismissed() {
        _uiState.update { it.copy(showNotifRationale = false) }
    }

    // Chiamato dopo che il permesso viene concesso dal sistema.
    // Riattiva il primo toggle che l'utente aveva tentato di attivare.
    fun onNotifPermissionGranted(context: Context, current: NotificationPreferences) {
        val updated = current.copy(streakEnabled = true)
        applyNotifPreferences(context, current, updated)
    }

    fun requestNotifPermission() {
        _uiState.update { it.copy(showNotifRationale = true) }
    }
}