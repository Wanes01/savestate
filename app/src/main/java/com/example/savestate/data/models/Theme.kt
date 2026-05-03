package com.example.savestate.data.models

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.Color
import com.example.savestate.ui.theme.colors.NintendoColors
import com.example.savestate.ui.theme.colors.PlayStationColors
import com.example.savestate.ui.theme.colors.XboxColors

enum class Theme {
    SYSTEM, LIGHT, DARK, PLAYSTATION, XBOX, NINTENDO;

    fun toLabel(): String = when(this) {
        PLAYSTATION -> "PlayStation"
        else -> this.name.lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}