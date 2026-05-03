package com.example.savestate.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.savestate.data.models.Theme
import com.example.savestate.ui.theme.colors.NintendoColors
import com.example.savestate.ui.theme.colors.PlayStationColors
import com.example.savestate.ui.theme.colors.SystemColors
import com.example.savestate.ui.theme.colors.XboxColors

// App's default schemes
private val LightColorScheme = lightColorScheme(
    primary = SystemColors.primaryLight,
    onPrimary = SystemColors.onPrimaryLight,
    primaryContainer = SystemColors.primaryContainerLight,
    onPrimaryContainer = SystemColors.onPrimaryContainerLight,
    secondary = SystemColors.secondaryLight,
    onSecondary = SystemColors.onSecondaryLight,
    secondaryContainer = SystemColors.secondaryContainerLight,
    onSecondaryContainer = SystemColors.onSecondaryContainerLight,
    tertiary = SystemColors.tertiaryLight,
    onTertiary = SystemColors.onTertiaryLight,
    tertiaryContainer = SystemColors.tertiaryContainerLight,
    onTertiaryContainer = SystemColors.onTertiaryContainerLight,
    error = SystemColors.errorLight,
    onError = SystemColors.onErrorLight,
    errorContainer = SystemColors.errorContainerLight,
    onErrorContainer = SystemColors.onErrorContainerLight,
    background = SystemColors.backgroundLight,
    onBackground = SystemColors.onBackgroundLight,
    surface = SystemColors.surfaceLight,
    onSurface = SystemColors.onSurfaceLight,
    surfaceVariant = SystemColors.surfaceVariantLight,
    onSurfaceVariant = SystemColors.onSurfaceVariantLight,
    outline = SystemColors.outlineLight,
    outlineVariant = SystemColors.outlineVariantLight,
    scrim = SystemColors.scrimLight,
    inverseSurface = SystemColors.inverseSurfaceLight,
    inverseOnSurface = SystemColors.inverseOnSurfaceLight,
    inversePrimary = SystemColors.inversePrimaryLight,
    surfaceDim = SystemColors.surfaceDimLight,
    surfaceBright = SystemColors.surfaceBrightLight,
    surfaceContainerLowest = SystemColors.surfaceContainerLowestLight,
    surfaceContainerLow = SystemColors.surfaceContainerLowLight,
    surfaceContainer = SystemColors.surfaceContainerLight,
    surfaceContainerHigh = SystemColors.surfaceContainerHighLight,
    surfaceContainerHighest = SystemColors.surfaceContainerHighestLight,
)

private val DarkColorScheme = darkColorScheme(
    primary = SystemColors.primaryDark,
    onPrimary = SystemColors.onPrimaryDark,
    primaryContainer = SystemColors.primaryContainerDark,
    onPrimaryContainer = SystemColors.onPrimaryContainerDark,
    secondary = SystemColors.secondaryDark,
    onSecondary = SystemColors.onSecondaryDark,
    secondaryContainer = SystemColors.secondaryContainerDark,
    onSecondaryContainer = SystemColors.onSecondaryContainerDark,
    tertiary = SystemColors.tertiaryDark,
    onTertiary = SystemColors.onTertiaryDark,
    tertiaryContainer = SystemColors.tertiaryContainerDark,
    onTertiaryContainer = SystemColors.onTertiaryContainerDark,
    error = SystemColors.errorDark,
    onError = SystemColors.onErrorDark,
    errorContainer = SystemColors.errorContainerDark,
    onErrorContainer = SystemColors.onErrorContainerDark,
    background = SystemColors.backgroundDark,
    onBackground = SystemColors.onBackgroundDark,
    surface = SystemColors.surfaceDark,
    onSurface = SystemColors.onSurfaceDark,
    surfaceVariant = SystemColors.surfaceVariantDark,
    onSurfaceVariant = SystemColors.onSurfaceVariantDark,
    outline = SystemColors.outlineDark,
    outlineVariant = SystemColors.outlineVariantDark,
    scrim = SystemColors.scrimDark,
    inverseSurface = SystemColors.inverseSurfaceDark,
    inverseOnSurface = SystemColors.inverseOnSurfaceDark,
    inversePrimary = SystemColors.inversePrimaryDark,
    surfaceDim = SystemColors.surfaceDimDark,
    surfaceBright = SystemColors.surfaceBrightDark,
    surfaceContainerLowest = SystemColors.surfaceContainerLowestDark,
    surfaceContainerLow = SystemColors.surfaceContainerLowDark,
    surfaceContainer = SystemColors.surfaceContainerDark,
    surfaceContainerHigh = SystemColors.surfaceContainerHighDark,
    surfaceContainerHighest = SystemColors.surfaceContainerHighestDark,
)

// PlayStation schemes
private val PlaystationLightScheme = lightColorScheme(
    primary = PlayStationColors.primaryLight,
    onPrimary = PlayStationColors.onPrimaryLight,
    primaryContainer = PlayStationColors.primaryContainerLight,
    onPrimaryContainer = PlayStationColors.onPrimaryContainerLight,
    secondary = PlayStationColors.secondaryLight,
    onSecondary = PlayStationColors.onSecondaryLight,
    secondaryContainer = PlayStationColors.secondaryContainerLight,
    onSecondaryContainer = PlayStationColors.onSecondaryContainerLight,
    tertiary = PlayStationColors.tertiaryLight,
    onTertiary = PlayStationColors.onTertiaryLight,
    tertiaryContainer = PlayStationColors.tertiaryContainerLight,
    onTertiaryContainer = PlayStationColors.onTertiaryContainerLight,
    error = PlayStationColors.errorLight,
    onError = PlayStationColors.onErrorLight,
    errorContainer = PlayStationColors.errorContainerLight,
    onErrorContainer = PlayStationColors.onErrorContainerLight,
    background = PlayStationColors.backgroundLight,
    onBackground = PlayStationColors.onBackgroundLight,
    surface = PlayStationColors.surfaceLight,
    onSurface = PlayStationColors.onSurfaceLight,
    surfaceVariant = PlayStationColors.surfaceVariantLight,
    onSurfaceVariant = PlayStationColors.onSurfaceVariantLight,
    outline = PlayStationColors.outlineLight,
    outlineVariant = PlayStationColors.outlineVariantLight,
    scrim = PlayStationColors.scrimLight,
    inverseSurface = PlayStationColors.inverseSurfaceLight,
    inverseOnSurface = PlayStationColors.inverseOnSurfaceLight,
    inversePrimary = PlayStationColors.inversePrimaryLight,
    surfaceDim = PlayStationColors.surfaceDimLight,
    surfaceBright = PlayStationColors.surfaceBrightLight,
    surfaceContainerLowest = PlayStationColors.surfaceContainerLowestLight,
    surfaceContainerLow = PlayStationColors.surfaceContainerLowLight,
    surfaceContainer = PlayStationColors.surfaceContainerLight,
    surfaceContainerHigh = PlayStationColors.surfaceContainerHighLight,
    surfaceContainerHighest = PlayStationColors.surfaceContainerHighestLight,
)

private val PlaystationDarkScheme = darkColorScheme(
    primary = PlayStationColors.primaryDark,
    onPrimary = PlayStationColors.onPrimaryDark,
    primaryContainer = PlayStationColors.primaryContainerDark,
    onPrimaryContainer = PlayStationColors.onPrimaryContainerDark,
    secondary = PlayStationColors.secondaryDark,
    onSecondary = PlayStationColors.onSecondaryDark,
    secondaryContainer = PlayStationColors.secondaryContainerDark,
    onSecondaryContainer = PlayStationColors.onSecondaryContainerDark,
    tertiary = PlayStationColors.tertiaryDark,
    onTertiary = PlayStationColors.onTertiaryDark,
    tertiaryContainer = PlayStationColors.tertiaryContainerDark,
    onTertiaryContainer = PlayStationColors.onTertiaryContainerDark,
    error = PlayStationColors.errorDark,
    onError = PlayStationColors.onErrorDark,
    errorContainer = PlayStationColors.errorContainerDark,
    onErrorContainer = PlayStationColors.onErrorContainerDark,
    background = PlayStationColors.backgroundDark,
    onBackground = PlayStationColors.onBackgroundDark,
    surface = PlayStationColors.surfaceDark,
    onSurface = PlayStationColors.onSurfaceDark,
    surfaceVariant = PlayStationColors.surfaceVariantDark,
    onSurfaceVariant = PlayStationColors.onSurfaceVariantDark,
    outline = PlayStationColors.outlineDark,
    outlineVariant = PlayStationColors.outlineVariantDark,
    scrim = PlayStationColors.scrimDark,
    inverseSurface = PlayStationColors.inverseSurfaceDark,
    inverseOnSurface = PlayStationColors.inverseOnSurfaceDark,
    inversePrimary = PlayStationColors.inversePrimaryDark,
    surfaceDim = PlayStationColors.surfaceDimDark,
    surfaceBright = PlayStationColors.surfaceBrightDark,
    surfaceContainerLowest = PlayStationColors.surfaceContainerLowestDark,
    surfaceContainerLow = PlayStationColors.surfaceContainerLowDark,
    surfaceContainer = PlayStationColors.surfaceContainerDark,
    surfaceContainerHigh = PlayStationColors.surfaceContainerHighDark,
    surfaceContainerHighest = PlayStationColors.surfaceContainerHighestDark,
)

// Xbox schemes
private val XboxLightScheme = lightColorScheme(
    primary = XboxColors.primaryLight,
    onPrimary = XboxColors.onPrimaryLight,
    primaryContainer = XboxColors.primaryContainerLight,
    onPrimaryContainer = XboxColors.onPrimaryContainerLight,
    secondary = XboxColors.secondaryLight,
    onSecondary = XboxColors.onSecondaryLight,
    secondaryContainer = XboxColors.secondaryContainerLight,
    onSecondaryContainer = XboxColors.onSecondaryContainerLight,
    tertiary = XboxColors.tertiaryLight,
    onTertiary = XboxColors.onTertiaryLight,
    tertiaryContainer = XboxColors.tertiaryContainerLight,
    onTertiaryContainer = XboxColors.onTertiaryContainerLight,
    error = XboxColors.errorLight,
    onError = XboxColors.onErrorLight,
    errorContainer = XboxColors.errorContainerLight,
    onErrorContainer = XboxColors.onErrorContainerLight,
    background = XboxColors.backgroundLight,
    onBackground = XboxColors.onBackgroundLight,
    surface = XboxColors.surfaceLight,
    onSurface = XboxColors.onSurfaceLight,
    surfaceVariant = XboxColors.surfaceVariantLight,
    onSurfaceVariant = XboxColors.onSurfaceVariantLight,
    outline = XboxColors.outlineLight,
    outlineVariant = XboxColors.outlineVariantLight,
    scrim = XboxColors.scrimLight,
    inverseSurface = XboxColors.inverseSurfaceLight,
    inverseOnSurface = XboxColors.inverseOnSurfaceLight,
    inversePrimary = XboxColors.inversePrimaryLight,
    surfaceDim = XboxColors.surfaceDimLight,
    surfaceBright = XboxColors.surfaceBrightLight,
    surfaceContainerLowest = XboxColors.surfaceContainerLowestLight,
    surfaceContainerLow = XboxColors.surfaceContainerLowLight,
    surfaceContainer = XboxColors.surfaceContainerLight,
    surfaceContainerHigh = XboxColors.surfaceContainerHighLight,
    surfaceContainerHighest = XboxColors.surfaceContainerHighestLight,
)

private val XboxDarkScheme = darkColorScheme(
    primary = XboxColors.primaryDark,
    onPrimary = XboxColors.onPrimaryDark,
    primaryContainer = XboxColors.primaryContainerDark,
    onPrimaryContainer = XboxColors.onPrimaryContainerDark,
    secondary = XboxColors.secondaryDark,
    onSecondary = XboxColors.onSecondaryDark,
    secondaryContainer = XboxColors.secondaryContainerDark,
    onSecondaryContainer = XboxColors.onSecondaryContainerDark,
    tertiary = XboxColors.tertiaryDark,
    onTertiary = XboxColors.onTertiaryDark,
    tertiaryContainer = XboxColors.tertiaryContainerDark,
    onTertiaryContainer = XboxColors.onTertiaryContainerDark,
    error = XboxColors.errorDark,
    onError = XboxColors.onErrorDark,
    errorContainer = XboxColors.errorContainerDark,
    onErrorContainer = XboxColors.onErrorContainerDark,
    background = XboxColors.backgroundDark,
    onBackground = XboxColors.onBackgroundDark,
    surface = XboxColors.surfaceDark,
    onSurface = XboxColors.onSurfaceDark,
    surfaceVariant = XboxColors.surfaceVariantDark,
    onSurfaceVariant = XboxColors.onSurfaceVariantDark,
    outline = XboxColors.outlineDark,
    outlineVariant = XboxColors.outlineVariantDark,
    scrim = XboxColors.scrimDark,
    inverseSurface = XboxColors.inverseSurfaceDark,
    inverseOnSurface = XboxColors.inverseOnSurfaceDark,
    inversePrimary = XboxColors.inversePrimaryDark,
    surfaceDim = XboxColors.surfaceDimDark,
    surfaceBright = XboxColors.surfaceBrightDark,
    surfaceContainerLowest = XboxColors.surfaceContainerLowestDark,
    surfaceContainerLow = XboxColors.surfaceContainerLowDark,
    surfaceContainer = XboxColors.surfaceContainerDark,
    surfaceContainerHigh = XboxColors.surfaceContainerHighDark,
    surfaceContainerHighest = XboxColors.surfaceContainerHighestDark,
)

// Nintendo schemes
private val NintendoLightScheme = lightColorScheme(
    primary = NintendoColors.primaryLight,
    onPrimary = NintendoColors.onPrimaryLight,
    primaryContainer = NintendoColors.primaryContainerLight,
    onPrimaryContainer = NintendoColors.onPrimaryContainerLight,
    secondary = NintendoColors.secondaryLight,
    onSecondary = NintendoColors.onSecondaryLight,
    secondaryContainer = NintendoColors.secondaryContainerLight,
    onSecondaryContainer = NintendoColors.onSecondaryContainerLight,
    tertiary = NintendoColors.tertiaryLight,
    onTertiary = NintendoColors.onTertiaryLight,
    tertiaryContainer = NintendoColors.tertiaryContainerLight,
    onTertiaryContainer = NintendoColors.onTertiaryContainerLight,
    error = NintendoColors.errorLight,
    onError = NintendoColors.onErrorLight,
    errorContainer = NintendoColors.errorContainerLight,
    onErrorContainer = NintendoColors.onErrorContainerLight,
    background = NintendoColors.backgroundLight,
    onBackground = NintendoColors.onBackgroundLight,
    surface = NintendoColors.surfaceLight,
    onSurface = NintendoColors.onSurfaceLight,
    surfaceVariant = NintendoColors.surfaceVariantLight,
    onSurfaceVariant = NintendoColors.onSurfaceVariantLight,
    outline = NintendoColors.outlineLight,
    outlineVariant = NintendoColors.outlineVariantLight,
    scrim = NintendoColors.scrimLight,
    inverseSurface = NintendoColors.inverseSurfaceLight,
    inverseOnSurface = NintendoColors.inverseOnSurfaceLight,
    inversePrimary = NintendoColors.inversePrimaryLight,
    surfaceDim = NintendoColors.surfaceDimLight,
    surfaceBright = NintendoColors.surfaceBrightLight,
    surfaceContainerLowest = NintendoColors.surfaceContainerLowestLight,
    surfaceContainerLow = NintendoColors.surfaceContainerLowLight,
    surfaceContainer = NintendoColors.surfaceContainerLight,
    surfaceContainerHigh = NintendoColors.surfaceContainerHighLight,
    surfaceContainerHighest = NintendoColors.surfaceContainerHighestLight,
)

private val NintendoDarkScheme = darkColorScheme(
    primary = NintendoColors.primaryDark,
    onPrimary = NintendoColors.onPrimaryDark,
    primaryContainer = NintendoColors.primaryContainerDark,
    onPrimaryContainer = NintendoColors.onPrimaryContainerDark,
    secondary = NintendoColors.secondaryDark,
    onSecondary = NintendoColors.onSecondaryDark,
    secondaryContainer = NintendoColors.secondaryContainerDark,
    onSecondaryContainer = NintendoColors.onSecondaryContainerDark,
    tertiary = NintendoColors.tertiaryDark,
    onTertiary = NintendoColors.onTertiaryDark,
    tertiaryContainer = NintendoColors.tertiaryContainerDark,
    onTertiaryContainer = NintendoColors.onTertiaryContainerDark,
    error = NintendoColors.errorDark,
    onError = NintendoColors.onErrorDark,
    errorContainer = NintendoColors.errorContainerDark,
    onErrorContainer = NintendoColors.onErrorContainerDark,
    background = NintendoColors.backgroundDark,
    onBackground = NintendoColors.onBackgroundDark,
    surface = NintendoColors.surfaceDark,
    onSurface = NintendoColors.onSurfaceDark,
    surfaceVariant = NintendoColors.surfaceVariantDark,
    onSurfaceVariant = NintendoColors.onSurfaceVariantDark,
    outline = NintendoColors.outlineDark,
    outlineVariant = NintendoColors.outlineVariantDark,
    scrim = NintendoColors.scrimDark,
    inverseSurface = NintendoColors.inverseSurfaceDark,
    inverseOnSurface = NintendoColors.inverseOnSurfaceDark,
    inversePrimary = NintendoColors.inversePrimaryDark,
    surfaceDim = NintendoColors.surfaceDimDark,
    surfaceBright = NintendoColors.surfaceBrightDark,
    surfaceContainerLowest = NintendoColors.surfaceContainerLowestDark,
    surfaceContainerLow = NintendoColors.surfaceContainerLowDark,
    surfaceContainer = NintendoColors.surfaceContainerDark,
    surfaceContainerHigh = NintendoColors.surfaceContainerHighDark,
    surfaceContainerHighest = NintendoColors.surfaceContainerHighestDark,
)

    @Composable
    fun SavestateTheme(
        theme: Theme,
        content: @Composable () -> Unit
    ) {
        val darkTheme = when (theme) {
            Theme.SYSTEM -> isSystemInDarkTheme()
            Theme.LIGHT -> false
            Theme.DARK -> true
            Theme.PLAYSTATION, Theme.XBOX, Theme.NINTENDO -> isSystemInDarkTheme()
        }

        val colorScheme = when (theme) {
            Theme.SYSTEM -> if (darkTheme) DarkColorScheme else LightColorScheme
            Theme.LIGHT -> LightColorScheme
            Theme.DARK -> DarkColorScheme
            Theme.PLAYSTATION -> if (darkTheme) PlaystationDarkScheme else PlaystationLightScheme
            Theme.XBOX -> if (darkTheme) XboxDarkScheme else XboxLightScheme
            Theme.NINTENDO -> if (darkTheme) NintendoDarkScheme else NintendoLightScheme
        }

        val view = LocalView.current
        if (!view.isInEditMode) {
            SideEffect {
                val window = (view.context as Activity).window
                window.statusBarColor = colorScheme.primaryContainer.toArgb()
                WindowCompat
                    .getInsetsController(window, view)
                    .isAppearanceLightStatusBars = !darkTheme
            }
        }

        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }