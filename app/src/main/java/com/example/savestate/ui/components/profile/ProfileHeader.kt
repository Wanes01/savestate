package com.example.savestate.ui.components.profile

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.savestate.R
import java.io.File

@Composable
fun ProfileHeader(
    nickname: String,
    email: String,
    levelLabel: String,
    photoUri: Uri?,
    onPickFromCamera: () -> Unit,
    onPickFromGallery: () -> Unit,
    onUpdateNickname: (String) -> Unit,
    modifier: Modifier = Modifier,
    refreshKey: Long = 0L,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var nicknameInput by remember(nickname) { mutableStateOf(nickname) }

    // nickname edit dialog
    // shows after the user clicks on their nickname
    if (showEditDialog) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit nickname") },
            text = {
                OutlinedTextField(
                    value = nicknameInput,
                    onValueChange = { nicknameInput = it },
                    singleLine = true,
                    label = { Text("Nickname") }
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    onUpdateNickname(nicknameInput)
                    showEditDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // avatar and camera icon
        Box(contentAlignment = Alignment.BottomEnd) {
            // user image or default image
            if (photoUri != null) {
                AsyncImage(
                    /* adds a timestamp to the uri in order to
                    make Coil take the last cached image
                    if it changes */
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(photoUri)
                        .memoryCacheKey("profile_photo_$refreshKey")
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .build(),
                    contentDescription = "Profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.default_avatar),
                    contentDescription = "Default profile photo",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                )
            }
            // camera button
            Box(contentAlignment = Alignment.Center) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(32.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, CircleShape)
                        .clickable { menuExpanded = true }
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Change profile photo",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }

                DropdownMenu(
                    offset = DpOffset(x = 0.dp, y = 8.dp),
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Camera") },
                        leadingIcon = {
                            Icon(Icons.Default.CameraAlt, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onPickFromCamera()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Gallery") },
                        leadingIcon = {
                            Icon(Icons.Default.Photo, contentDescription = null)
                        },
                        onClick = {
                            menuExpanded = false
                            onPickFromGallery()
                        }
                    )
                }
            }
        }
        // nickname, email and level
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                // shows the edit dialog if clicked
                modifier = Modifier.clickable { showEditDialog = true }
            ) {
                Text(
                    text = nickname,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit nickname",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = email,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.secondaryContainer,
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                tonalElevation = 0.dp
            ) {
                Text(
                    text = levelLabel,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}
