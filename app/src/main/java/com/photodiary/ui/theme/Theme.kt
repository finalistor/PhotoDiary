package com.photodiary.ui.theme

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

@Composable
fun PhotoDiaryTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    themePreset: ThemePreset = ThemePreset.TERRACOTTA,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val targetScheme = presetColorScheme(themePreset, darkTheme)

    val animatedPrimary by animateColorAsState(targetScheme.primary, tween(500))
    val animatedOnPrimary by animateColorAsState(targetScheme.onPrimary, tween(500))
    val animatedPrimaryContainer by animateColorAsState(targetScheme.primaryContainer, tween(500))
    val animatedOnPrimaryContainer by animateColorAsState(targetScheme.onPrimaryContainer, tween(500))
    val animatedSecondary by animateColorAsState(targetScheme.secondary, tween(500))
    val animatedOnSecondary by animateColorAsState(targetScheme.onSecondary, tween(500))
    val animatedSecondaryContainer by animateColorAsState(targetScheme.secondaryContainer, tween(500))
    val animatedOnSecondaryContainer by animateColorAsState(targetScheme.onSecondaryContainer, tween(500))
    val animatedTertiary by animateColorAsState(targetScheme.tertiary, tween(500))
    val animatedOnTertiary by animateColorAsState(targetScheme.onTertiary, tween(500))
    val animatedBackground by animateColorAsState(targetScheme.background, tween(500))
    val animatedOnBackground by animateColorAsState(targetScheme.onBackground, tween(500))
    val animatedSurface by animateColorAsState(targetScheme.surface, tween(500))
    val animatedOnSurface by animateColorAsState(targetScheme.onSurface, tween(500))
    val animatedSurfaceVariant by animateColorAsState(targetScheme.surfaceVariant, tween(500))
    val animatedOnSurfaceVariant by animateColorAsState(targetScheme.onSurfaceVariant, tween(500))
    val animatedError by animateColorAsState(targetScheme.error, tween(500))
    val animatedOnError by animateColorAsState(targetScheme.onError, tween(500))
    val animatedErrorContainer by animateColorAsState(targetScheme.errorContainer, tween(500))
    val animatedOnErrorContainer by animateColorAsState(targetScheme.onErrorContainer, tween(500))
    val animatedOutline by animateColorAsState(targetScheme.outline, tween(500))
    val animatedOutlineVariant by animateColorAsState(targetScheme.outlineVariant, tween(500))

    val colorScheme = targetScheme.copy(
        primary = animatedPrimary,
        onPrimary = animatedOnPrimary,
        primaryContainer = animatedPrimaryContainer,
        onPrimaryContainer = animatedOnPrimaryContainer,
        secondary = animatedSecondary,
        onSecondary = animatedOnSecondary,
        secondaryContainer = animatedSecondaryContainer,
        onSecondaryContainer = animatedOnSecondaryContainer,
        tertiary = animatedTertiary,
        onTertiary = animatedOnTertiary,
        background = animatedBackground,
        onBackground = animatedOnBackground,
        surface = animatedSurface,
        onSurface = animatedOnSurface,
        surfaceVariant = animatedSurfaceVariant,
        onSurfaceVariant = animatedOnSurfaceVariant,
        error = animatedError,
        onError = animatedOnError,
        errorContainer = animatedErrorContainer,
        onErrorContainer = animatedOnErrorContainer,
        outline = animatedOutline,
        outlineVariant = animatedOutlineVariant
    )

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = animatedBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
