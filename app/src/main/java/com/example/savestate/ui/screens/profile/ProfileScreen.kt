package com.example.savestate.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.savestate.AppViewModel
import com.example.savestate.data.models.NotificationPreferences
import com.example.savestate.data.models.Theme
import com.example.savestate.domain.XpSystem
import com.example.savestate.notification.NotificationHelper
import com.example.savestate.ui.components.profile.NotificationsCard
import com.example.savestate.ui.components.profile.ProfileHeader
import com.example.savestate.ui.components.profile.ThemeSelector
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel,
) {
    val profileViewModel: ProfileViewModel = koinViewModel()

    val userData by appViewModel.userData.collectAsStateWithLifecycle()
    val userXp by appViewModel.userXp.collectAsStateWithLifecycle()
    val theme by appViewModel.theme.collectAsStateWithLifecycle()

    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val firebaseUser = FirebaseAuth.getInstance().currentUser

    var hasNotifPermission by remember { mutableStateOf(NotificationHelper.hasPermission(context)) }
    val notifPrefs by profileViewModel.notificationPreferences.collectAsStateWithLifecycle()

    // camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) profileViewModel.onPhotoCaptured(firebaseUser, userData)
    }

    // gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            profileViewModel.onGalleryPhotoPicked(
                firebaseUser,
                it,
                userData
            )
        }
    }

    // camera permission launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            val uri = profileViewModel.createTempPhotoUri()
            cameraLauncher.launch(uri)
        }
    }

    // notification launcher
    val notifPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasNotifPermission = granted
        if (granted) {
            profileViewModel.onNotifPermissionGranted(context, notifPrefs)
        }
    }

    LaunchedEffect(Unit) {
        appViewModel.setTopBar(
            title = "Profile & Settings"
        ) {
            TextButton(
                onClick = { appViewModel.logout() },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text(text = "Log out")
                Spacer(Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null
                )
            }
        }
    }

    Surface(
        modifier = modifier
            .fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            // shown if the user didn't grant notification permission
            if (uiState.showNotifRationale) {
                AlertDialog(
                    onDismissRequest = { profileViewModel.onRationaleDismissed() },
                    icon = {
                        Icon(Icons.Default.Notifications, contentDescription = null)
                    },
                    title = { Text("Enable notifications") },
                    text = {
                        Text(
                            "Savestate needs notification permission to remind you of your daily " +
                                    "streak, notify you when you reach a new level, and show you the " +
                                    "duration of your current gameplay session as a notification"
                        )
                    },
                    confirmButton = {
                        TextButton(onClick = {
                            profileViewModel.onRationaleDismissed()
                            // displayed only if the permission is needed
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notifPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }) { Text("Continue") }
                    },
                    dismissButton = {
                        TextButton(onClick = { profileViewModel.onRationaleDismissed() }) {
                            Text("Not now")
                        }
                    }
                )
            }

            val level = XpSystem.levelFromXp(userXp.xp)
            val levelTitle = XpSystem.levelTitle(level)
            // profile info
            ProfileHeader(
                nickname = userData.displayName,
                email = userData.email,
                levelLabel = "Level $level · $levelTitle",
                photoUri = userData.photoUrl?.toUri(),
                refreshKey = uiState.photoRefreshKey,
                onPickFromCamera = {
                    val granted = ContextCompat.checkSelfPermission(
                        context, Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED

                    if (granted) {
                        val uri = profileViewModel.createTempPhotoUri()
                        cameraLauncher.launch(uri)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onPickFromGallery = {
                    galleryLauncher.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                },
                onUpdateNickname = {
                    profileViewModel.updateNickname(it, userData)
                }
            )
            AppSettings(
                selectedTheme = theme,
                onThemeSelected = { appViewModel.setTheme(it) },
                notifPrefs = notifPrefs,
                hasNotifPermission = hasNotifPermission,
                onStreakToggled = { enabled ->
                    profileViewModel.onNotifToggled(
                        context,
                        notifPrefs,
                        notifPrefs.copy(streakEnabled = enabled)
                    )
                },
                onLevelToggled = { enabled ->
                    profileViewModel.onNotifToggled(
                        context,
                        notifPrefs,
                        notifPrefs.copy(levelEnabled = enabled)
                    )
                },
                onSessionToggled = { enabled ->
                    profileViewModel.onNotifToggled(
                        context,
                        notifPrefs,
                        notifPrefs.copy(sessionEnabled = enabled)
                    )
                },
                onStreakTimeSelected = { hour, minute ->
                    profileViewModel.onStreakTimeChanged(context, hour, minute, notifPrefs)
                },
                onRequestPermission = {
                    profileViewModel.requestNotifPermission()
                }
            )
        }
    }
}

@Composable
fun AppSettings(
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    notifPrefs: NotificationPreferences,
    hasNotifPermission: Boolean,
    onStreakToggled: (Boolean) -> Unit,
    onLevelToggled: (Boolean) -> Unit,
    onSessionToggled: (Boolean) -> Unit,
    onStreakTimeSelected: (hour: Int, minute: Int) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSectionLabel("Appearance")
        ThemeSelector(selectedTheme, onThemeSelected)
        SettingsSectionLabel("Notifications")
        NotificationsCard(
            notifPrefs = notifPrefs,
            hasPermission = hasNotifPermission,
            onStreakToggled = onStreakToggled,
            onLevelToggled = onLevelToggled,
            onSessionToggled = onSessionToggled,
            onStreakTimeSelected = onStreakTimeSelected,
            onRequestPermission = onRequestPermission
        )
    }
}

@Composable
private fun SettingsSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
    )
}