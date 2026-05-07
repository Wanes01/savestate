package com.example.savestate.data.models

enum class Theme {
    SYSTEM, LIGHT, DARK, PLAYSTATION, XBOX, NINTENDO;

    fun toLabel(): String = when (this) {
        PLAYSTATION -> "PlayStation"
        else -> this.name.lowercase()
            .replaceFirstChar { it.uppercase() }
    }
}