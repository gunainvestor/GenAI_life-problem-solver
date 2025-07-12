package com.lifeproblemsolver.app.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlueLight,
    onPrimary = Neutral50,
    primaryContainer = PrimaryBlue,
    onPrimaryContainer = Neutral100,
    secondary = SecondaryTealLight,
    onSecondary = Neutral50,
    secondaryContainer = SecondaryTeal,
    onSecondaryContainer = Neutral100,
    tertiary = AccentGoldLight,
    onTertiary = Neutral50,
    tertiaryContainer = AccentGold,
    onTertiaryContainer = Neutral100,
    background = BackgroundDark,
    onBackground = Neutral100,
    surface = BackgroundDarkSecondary,
    onSurface = Neutral100,
    surfaceVariant = Neutral800,
    onSurfaceVariant = Neutral300,
    outline = Neutral600,
    outlineVariant = Neutral700,
    error = ErrorRedLight,
    onError = Neutral50,
    errorContainer = ErrorRed,
    onErrorContainer = Neutral100,
    inverseSurface = Neutral100,
    inverseOnSurface = Neutral900,
    inversePrimary = PrimaryBlueLight
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    onPrimary = Neutral50,
    primaryContainer = PrimaryBlueLight,
    onPrimaryContainer = Neutral900,
    secondary = SecondaryTeal,
    onSecondary = Neutral50,
    secondaryContainer = SecondaryTealLight,
    onSecondaryContainer = Neutral900,
    tertiary = AccentGold,
    onTertiary = Neutral50,
    tertiaryContainer = AccentGoldLight,
    onTertiaryContainer = Neutral900,
    background = BackgroundPrimary,
    onBackground = Neutral900,
    surface = Neutral50,
    onSurface = Neutral900,
    surfaceVariant = Neutral100,
    onSurfaceVariant = Neutral700,
    outline = Neutral300,
    outlineVariant = Neutral200,
    error = ErrorRed,
    onError = Neutral50,
    errorContainer = ErrorRedLight,
    onErrorContainer = Neutral900,
    inverseSurface = Neutral900,
    inverseOnSurface = Neutral100,
    inversePrimary = PrimaryBlue
)

@Composable
fun LifeProblemSolverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled for consistent premium branding
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Set status bar to match the app's premium theme
            window.statusBarColor = if (darkTheme) BackgroundDark.toArgb() else BackgroundPrimary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
} 