package com.example.savestate.ui.components.profile

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.savestate.data.models.NotificationPreferences
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsCard(
    notifPrefs: NotificationPreferences,
    hasPermission: Boolean,
    onStreakToggled: (Boolean) -> Unit,
    onLevelToggled: (Boolean) -> Unit,
    onSessionToggled: (Boolean) -> Unit,
    onStreakTimeSelected: (hour: Int, minute: Int) -> Unit,
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        // permission banner, displayed only if the notification permission is not granted
        if (!hasPermission) {
            Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clickable { onRequestPermission() }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Tap to enable notifications",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        // daily streak notification toggle
        ListItem(
            headlineContent = { Text("Streak reminder") },
            supportingContent = if (notifPrefs.streakEnabled) {
                {
                    val time = String.format(Locale.getDefault(), "%02d:%02d", notifPrefs.streakHour, notifPrefs.streakMinute)
                    Text(
                        text = "Every day at $time · Tap to change",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.clickable { showTimePicker = true }
                    )
                }
            } else null,
            trailingContent = {
                Switch(
                    checked = notifPrefs.streakEnabled && hasPermission,
                    onCheckedChange = onStreakToggled,
                    enabled = hasPermission
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // level up notification toggle
        ListItem(
            headlineContent = { Text("Level progression") },
            trailingContent = {
                Switch(
                    checked = notifPrefs.levelEnabled && hasPermission,
                    onCheckedChange = onLevelToggled,
                    enabled = hasPermission
                )
            }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

        // session notification toggle
        ListItem(
            headlineContent = { Text("Active session timer") },
            supportingContent = {
                Text(
                    text = "Shows a notification with the elapsed time while a session is active",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingContent = {
                Switch(
                    checked = notifPrefs.sessionEnabled && hasPermission,
                    onCheckedChange = onSessionToggled,
                    enabled = hasPermission
                )
            }
        )
    }

    // time picker dialog for the daily streak
    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = notifPrefs.streakHour,
            initialMinute = notifPrefs.streakMinute
        )
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Reminder time") },
            text = { TimePicker(state = timePickerState) },
            confirmButton = {
                TextButton(onClick = {
                    onStreakTimeSelected(timePickerState.hour, timePickerState.minute)
                    showTimePicker = false
                }) { Text("Confirm") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}