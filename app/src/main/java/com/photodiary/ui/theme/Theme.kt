package com.photodiary.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Terracotta,
    onPrimary = OnTerracotta,
    primaryContainer = TerracottaContainer,
    onPrimaryContainer = OnTerracottaContainer,
    secondary = WarmBrown,
    onSecondary = OnWarmBrown,
    secondaryContainer = WarmBrownContainer,
    onSecondaryContainer = OnWarmBrownContainer,
    tertiary = GoldAccent,
    background = BackgroundLight,
    onBackground = InkBlack,
    surface = SurfaceLight,
    onSurface = InkBlack,
    surfaceVariant = PaperCream,
    onSurfaceVariant = InkGray,
    error = ErrorRed,
    onError = OnTerracotta,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFF410002),
    outline = CardBorder,
    outlineVariant = PaperDark
)

private val DarkColorScheme = darkColorScheme(
    primary = TerracottaDark,
    onPrimary = Color(0xFF4A150A),
    primaryContainer = OnTerracottaContainer,
    onPrimaryContainer = TerracottaContainer,
    secondary = WarmBrownDark,
    onSecondary = Color(0xFF3A2218),
    secondaryContainer = OnWarmBrownContainer,
    onSecondaryContainer = WarmBrownContainer,
    tertiary = GoldAccentDark,
    background = BackgroundDark,
    onBackground = Color(0xFFEDE4DC),
    surface = SurfaceDark,
    onSurface = Color(0xFFEDE4DC),
    surfaceVariant = SageMistDark,
    onSurfaceVariant = Color(0xFFCCC0B4),
    error = ErrorRedDark,
    onError = Color(0xFF4A0002),
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorRedDark,
    outline = CardBorderDark,
    outlineVariant = Color(0xFF2A2622)
)

@Composable
fun PhotoDiaryTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
    }
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
