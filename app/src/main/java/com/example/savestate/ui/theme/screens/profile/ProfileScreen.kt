package com.example.savestate.ui.theme.screens.profile

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
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.savestate.AppViewModel
import com.example.savestate.data.models.Theme
import com.example.savestate.ui.theme.components.profile.ProfileHeader
import com.example.savestate.ui.theme.components.profile.ThemeSelector

@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    appViewModel: AppViewModel,
    onLogOut: () -> Unit
) {
    val userData by appViewModel.userData.collectAsStateWithLifecycle()
    val theme by appViewModel.theme.collectAsStateWithLifecycle()

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
            // profile info
            ProfileHeader(
                nickname = userData.displayName,
                email = userData.email,
                levelLabel = "Level 7 - Veteran TODOOO",
                photoUri = userData.photoUrl?.toUri(),
                onPickPhoto = {}
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