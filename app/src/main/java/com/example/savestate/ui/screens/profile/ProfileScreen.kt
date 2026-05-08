package com.example.savestate.ui.screens.profile

import android.Manifest
import android.content.pm.PackageManager
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.savestate.AppViewModel
import com.example.savestate.data.models.Theme
import com.example.savestate.domain.XpSystem
import com.example.savestate.ui.components.profile.ProfileHeader
import com.example.savestate.ui.components.profile.ThemeSelector
import com.google.firebase.auth.FirebaseAuth
import org.koin.androidx.compose.koinViewModel

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel,
    onLogOut: () -> Unit
) {
    val profileViewModel: ProfileViewModel = koinViewModel()

    val userData by appViewModel.userData.collectAsStateWithLifecycle()
    val userXp by appViewModel.userXp.collectAsStateWithLifecycle()
    val theme by appViewModel.theme.collectAsStateWithLifecycle()

    val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val firebaseUser = FirebaseAuth.getInstance().currentUser

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

    LaunchedEffect(Unit) {
        appViewModel.setTopBar(
            title = "Profile & Settings"
        ) {
            TextButton(
                onClick = onLogOut,
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
                onThemeSelected = { appViewModel.setTheme(it) }
            )
        }
    }
}

@Composable
fun AppSettings(
    selectedTheme: Theme,
    onThemeSelected: (Theme) -> Unit,
    // notifSettings: NotificationSettings,
    // onNotifChanged: (NotificationSettings) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        SettingsSectionLabel("Appearance")
        ThemeSelector(selectedTheme, onThemeSelected)

        SettingsSectionLabel("Notifications")
        // NotificationsCard(notifSettings, onNotifChanged)
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