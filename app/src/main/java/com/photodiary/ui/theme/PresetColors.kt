package com.photodiary.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

fun presetColorScheme(preset: ThemePreset, darkTheme: Boolean): ColorScheme {
    return when (preset) {
        ThemePreset.TERRACOTTA -> if (darkTheme) TerracottaDark else TerracottaLight
        ThemePreset.OCEAN_BLUE -> if (darkTheme) OceanBlueDark else OceanBlueLight
        ThemePreset.FOREST_GREEN -> if (darkTheme) ForestGreenDark else ForestGreenLight
        ThemePreset.LAVENDER -> if (darkTheme) LavenderDark else LavenderLight
        ThemePreset.SUNSET_ORANGE -> if (darkTheme) SunsetOrangeDark else SunsetOrangeLight
        ThemePreset.MONOCHROME -> if (darkTheme) MonochromeDark else MonochromeLight
        ThemePreset.CUSTOM -> if (darkTheme) TerracottaDark else TerracottaLight
    }
}

fun customColorScheme(primaryColor: Color, darkTheme: Boolean): ColorScheme {
    val (hue, sat, _) = primaryColor.toHsl()

    if (darkTheme) {
        val primary = primaryColor
        val onPrimary = Color.Black
        val primaryContainer = Color.hsl(hue, sat, 0.18f)
        val onPrimaryContainer = Color.hsl(hue, sat * 0.7f, 0.82f)
        val secondary = Color.hsl((hue + 30f) % 360f, sat * 0.5f, 0.72f)
        val onSecondary = Color.Black
        val secondaryContainer = Color.hsl((hue + 30f) % 360f, sat * 0.4f, 0.18f)
        val onSecondaryContainer = Color.hsl((hue + 30f) % 360f, sat * 0.3f, 0.85f)
        val tertiary = Color.hsl((hue + 180f) % 360f, sat * 0.35f, 0.70f)
        val onTertiary = Color.Black
        val tertiaryContainer = Color.hsl((hue + 180f) % 360f, sat * 0.3f, 0.18f)
        val onTertiaryContainer = Color.hsl((hue + 180f) % 360f, sat * 0.25f, 0.82f)
        val surface = Color.hsl(hue, sat * 0.08f, 0.10f)
        val onSurface = Color.hsl(hue, sat * 0.04f, 0.90f)
        val surfaceVariant = Color.hsl(hue, sat * 0.06f, 0.16f)
        val onSurfaceVariant = Color.hsl(hue, sat * 0.04f, 0.78f)
        val background = Color.hsl(hue, sat * 0.06f, 0.07f)
        val onBackground = Color.hsl(hue, sat * 0.04f, 0.90f)
        val outline = Color.hsl(hue, sat * 0.04f, 0.30f)
        val outlineVariant = Color.hsl(hue, sat * 0.06f, 0.18f)

        return darkColorScheme(
            primary = primary, onPrimary = onPrimary,
            primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
            secondary = secondary, onSecondary = onSecondary,
            secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary, onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
            background = background, onBackground = onBackground,
            surface = surface, onSurface = onSurface,
            surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
            error = ErrorDark, onError = OnErrorDark,
            errorContainer = ErrorContainerDark, onErrorContainer = OnErrorContainerDark,
            outline = outline, outlineVariant = outlineVariant
        )
    } else {
        val primary = primaryColor
        val onPrimary = Color.White
        val primaryContainer = Color.hsl(hue, sat * 0.25f, 0.92f)
        val onPrimaryContainer = Color.hsl(hue, sat * 0.9f, 0.18f)
        val secondary = Color.hsl((hue + 30f) % 360f, sat * 0.45f, 0.42f)
        val onSecondary = Color.White
        val secondaryContainer = Color.hsl((hue + 30f) % 360f, sat * 0.15f, 0.92f)
        val onSecondaryContainer = Color.hsl((hue + 30f) % 360f, sat * 0.6f, 0.22f)
        val tertiary = Color.hsl((hue + 180f) % 360f, sat * 0.35f, 0.40f)
        val onTertiary = Color.White
        val tertiaryContainer = Color.hsl((hue + 180f) % 360f, sat * 0.12f, 0.92f)
        val onTertiaryContainer = Color.hsl((hue + 180f) % 360f, sat * 0.5f, 0.20f)
        val surface = Color.hsl(hue, sat * 0.06f, 0.99f)
        val onSurface = Color.hsl(hue, sat * 0.08f, 0.10f)
        val surfaceVariant = Color.hsl(hue, sat * 0.08f, 0.94f)
        val onSurfaceVariant = Color.hsl(hue, sat * 0.05f, 0.32f)
        val background = Color.hsl(hue, sat * 0.04f, 0.98f)
        val onBackground = Color.hsl(hue, sat * 0.08f, 0.10f)
        val outline = Color.hsl(hue, sat * 0.03f, 0.78f)
        val outlineVariant = Color.hsl(hue, sat * 0.03f, 0.88f)

        return lightColorScheme(
            primary = primary, onPrimary = onPrimary,
            primaryContainer = primaryContainer, onPrimaryContainer = onPrimaryContainer,
            secondary = secondary, onSecondary = onSecondary,
            secondaryContainer = secondaryContainer, onSecondaryContainer = onSecondaryContainer,
            tertiary = tertiary, onTertiary = onTertiary,
            tertiaryContainer = tertiaryContainer, onTertiaryContainer = onTertiaryContainer,
            background = background, onBackground = onBackground,
            surface = surface, onSurface = onSurface,
            surfaceVariant = surfaceVariant, onSurfaceVariant = onSurfaceVariant,
            error = ErrorLight, onError = OnErrorLight,
            errorContainer = ErrorContainerLight, onErrorContainer = OnErrorContainerLight,
            outline = outline, outlineVariant = outlineVariant
        )
    }
}

// Error colors shared across all presets
private val ErrorLight = Color(0xFFBA1A1A)
private val OnErrorLight = Color(0xFFFFFFFF)
private val ErrorContainerLight = Color(0xFFFFDAD6)
private val OnErrorContainerLight = Color(0xFF410002)
private val ErrorDark = Color(0xFFFFB4AB)
private val OnErrorDark = Color(0xFF690005)
private val ErrorContainerDark = Color(0xFF93000A)
private val OnErrorContainerDark = Color(0xFFFFB4AB)

// ============ TERRACOTTA ============

private val TerracottaLight = lightColorScheme(
    primary = Color(0xFFB8503E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFEDE1),
    onPrimaryContainer = Color(0xFF7A2A1A),
    secondary = Color(0xFF7D6054),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFF5E6DC),
    onSecondaryContainer = Color(0xFF5C4034),
    tertiary = Color(0xFFC4A35A),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFEED0),
    onTertiaryContainer = Color(0xFF6B4A00),
    background = Color(0xFFFDF8F4),
    onBackground = Color(0xFF2D2420),
    surface = Color(0xFFFDF8F4),
    onSurface = Color(0xFF2D2420),
    surfaceVariant = Color(0xFFF6EFE8),
    onSurfaceVariant = Color(0xFF6B5F58),
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = Color(0xFFE8E0D8),
    outlineVariant = Color(0xFFD4CCC4)
)

private val TerracottaDark = darkColorScheme(
    primary = Color(0xFFE0806A),
    onPrimary = Color(0xFF4A150A),
    primaryContainer = Color(0xFF7A2A1A),
    onPrimaryContainer = Color(0xFFFFEDE1),
    secondary = Color(0xFFC4A898),
    onSecondary = Color(0xFF3A2218),
    secondaryContainer = Color(0xFF5C4034),
    onSecondaryContainer = Color(0xFFF5E6DC),
    tertiary = Color(0xFFD4B86A),
    onTertiary = Color(0xFF3A2A00),
    tertiaryContainer = Color(0xFF6B4A00),
    onTertiaryContainer = Color(0xFFFFEED0),
    background = Color(0xFF11100E),
    onBackground = Color(0xFFEDE4DC),
    surface = Color(0xFF1A1614),
    onSurface = Color(0xFFEDE4DC),
    surfaceVariant = Color(0xFF2A2622),
    onSurfaceVariant = Color(0xFFCCC0B4),
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = Color(0xFF3A3430),
    outlineVariant = Color(0xFF2A2622)
)

// ============ OCEAN BLUE ============

private val OceanBlueLight = lightColorScheme(
    primary = Color(0xFF1976D2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD1E4FF),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF00897B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFA7F5EC),
    onSecondaryContainer = Color(0xFF002019),
    tertiary = Color(0xFFF9A825),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFF0C0),
    onTertiaryContainer = Color(0xFF4A3000),
    background = Color(0xFFF5F9FC),
    onBackground = Color(0xFF171C21),
    surface = Color(0xFFFCFEFF),
    onSurface = Color(0xFF171C21),
    surfaceVariant = Color(0xFFE1E8F0),
    onSurfaceVariant = Color(0xFF44474E),
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = Color(0xFFCCCED4),
    outlineVariant = Color(0xFFBCC0C8)
)

private val OceanBlueDark = darkColorScheme(
    primary = Color(0xFF64B5F6),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = Color(0xFFD1E4FF),
    secondary = Color(0xFF80CBC4),
    onSecondary = Color(0xFF003730),
    secondaryContainer = Color(0xFF005048),
    onSecondaryContainer = Color(0xFFA7F5EC),
    tertiary = Color(0xFFFFF176),
    onTertiary = Color(0xFF4A3000),
    tertiaryContainer = Color(0xFF6B4A00),
    onTertiaryContainer = Color(0xFFFFF0C0),
    background = Color(0xFF0D151C),
    onBackground = Color(0xFFE2E2E7),
    surface = Color(0xFF151C24),
    onSurface = Color(0xFFE2E2E7),
    surfaceVariant = Color(0xFF40484F),
    onSurfaceVariant = Color(0xFFC4C6CF),
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = Color(0xFF505860),
    outlineVariant = Color(0xFF303840)
)

// ============ FOREST GREEN ============

private val ForestGreenLight = lightColorScheme(
    primary = Color(0xFF388E3C),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFC8E6C9),
    onPrimaryContainer = Color(0xFF002106),
    secondary = Color(0xFF6D4C41),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDBD1),
    onSecondaryContainer = Color(0xFF2C160C),
    tertiary = Color(0xFFCDDC39),
    onTertiary = Color(0xFF000000),
    tertiaryContainer = Color(0xFFF4FF81),
    onTertiaryContainer = Color(0xFF2A3000),
    background = Color(0xFFF6F8F2),
    onBackground = Color(0xFF181D15),
    surface = Color(0xFFFCFEF8),
    onSurface = Color(0xFF181D15),
    surfaceVariant = Color(0xFFDFE4D8),
    onSurfaceVariant = Color(0xFF43483F),
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = Color(0xFFC4C9BE),
    outlineVariant = Color(0xFFB4B9AF)
)

private val ForestGreenDark = darkColorScheme(
    primary = Color(0xFF81C784),
    onPrimary = Color(0xFF003910),
    primaryContainer = Color(0xFF005319),
    onPrimaryContainer = Color(0xFFC8E6C9),
    secondary = Color(0xFFBCAAA4),
    onSecondary = Color(0xFF2C160C),
    secondaryContainer = Color(0xFF463024),
    onSecondaryContainer = Color(0xFFFFDBD1),
    tertiary = Color(0xFFE6EE9C),
    onTertiary = Color(0xFF353D00),
    tertiaryContainer = Color(0xFF4D5600),
    onTertiaryContainer = Color(0xFFF4FF81),
    background = Color(0xFF101510),
    onBackground = Color(0xFFE0E3DA),
    surface = Color(0xFF181C16),
    onSurface = Color(0xFFE0E3DA),
    surfaceVariant = Color(0xFF42473F),
    onSurfaceVariant = Color(0xFFC2C7BD),
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = Color(0xFF5C6056),
    outlineVariant = Color(0xFF383D34)
)

// ============ LAVENDER ============

private val LavenderLight = lightColorScheme(
    primary = Color(0xFF7B1FA2),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE1BEE7),
    onPrimaryContainer = Color(0xFF2D004B),
    secondary = Color(0xFFC2185B),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFD9E6),
    onSecondaryContainer = Color(0xFF3E001D),
    tertiary = Color(0xFF0288D1),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFB3E5FC),
    onTertiaryContainer = Color(0xFF001F33),
    background = Color(0xFFFBF7FC),
    onBackground = Color(0xFF1C1A1F),
    surface = Color(0xFFFFF8FF),
    onSurface = Color(0xFF1C1A1F),
    surfaceVariant = Color(0xFFEBE0EC),
    onSurfaceVariant = Color(0xFF4B454D),
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = Color(0xFFCDC3D0),
    outlineVariant = Color(0xFFBDB3C0)
)

private val LavenderDark = darkColorScheme(
    primary = Color(0xFFCE93D8),
    onPrimary = Color(0xFF3A005B),
    primaryContainer = Color(0xFF540080),
    onPrimaryContainer = Color(0xFFE1BEE7),
    secondary = Color(0xFFF48FB1),
    onSecondary = Color(0xFF4E0028),
    secondaryContainer = Color(0xFF72003C),
    onSecondaryContainer = Color(0xFFFFD9E6),
    tertiary = Color(0xFF81D4FA),
    onTertiary = Color(0xFF003352),
    tertiaryContainer = Color(0xFF004B74),
    onTertiaryContainer = Color(0xFFB3E5FC),
    background = Color(0xFF131217),
    onBackground = Color(0xFFE6E1E6),
    surface = Color(0xFF1B181C),
    onSurface = Color(0xFFE6E1E6),
    surfaceVariant = Color(0xFF4B454D),
    onSurfaceVariant = Color(0xFFCCC4CE),
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = Color(0xFF626066),
    outlineVariant = Color(0xFF3A3840)
)

// ============ SUNSET ORANGE ============

private val SunsetOrangeLight = lightColorScheme(
    primary = Color(0xFFE64A19),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFFFDBD1),
    onPrimaryContainer = Color(0xFF3C0700),
    secondary = Color(0xFFF57C00),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFFFDCC2),
    onSecondaryContainer = Color(0xFF2E1500),
    tertiary = Color(0xFFD32F2F),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFFFDAD6),
    onTertiaryContainer = Color(0xFF410002),
    background = Color(0xFFFEF8F4),
    onBackground = Color(0xFF201A17),
    surface = Color(0xFFFFFBF8),
    onSurface = Color(0xFF201A17),
    surfaceVariant = Color(0xFFF4E0D8),
    onSurfaceVariant = Color(0xFF514540),
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = Color(0xFFD4C4BC),
    outlineVariant = Color(0xFFC4B4AC)
)

private val SunsetOrangeDark = darkColorScheme(
    primary = Color(0xFFFF8A65),
    onPrimary = Color(0xFF550D00),
    primaryContainer = Color(0xFF7A1A00),
    onPrimaryContainer = Color(0xFFFFDBD1),
    secondary = Color(0xFFFFB74D),
    onSecondary = Color(0xFF4A2800),
    secondaryContainer = Color(0xFF6B3C00),
    onSecondaryContainer = Color(0xFFFFDCC2),
    tertiary = Color(0xFFEF9A9A),
    onTertiary = Color(0xFF5A0004),
    tertiaryContainer = Color(0xFF82000A),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = Color(0xFF191310),
    onBackground = Color(0xFFEDE0DA),
    surface = Color(0xFF201814),
    onSurface = Color(0xFFEDE0DA),
    surfaceVariant = Color(0xFF514540),
    onSurfaceVariant = Color(0xFFD5C3BC),
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = Color(0xFF6B5E58),
    outlineVariant = Color(0xFF423630)
)

// ============ MONOCHROME ============

private val MonochromeLight = lightColorScheme(
    primary = Color(0xFF616161),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE0E0E0),
    onPrimaryContainer = Color(0xFF1A1A1A),
    secondary = Color(0xFF424242),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC8C8C8),
    onSecondaryContainer = Color(0xFF0E0E0E),
    tertiary = Color(0xFF78909C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFCFD8DC),
    onTertiaryContainer = Color(0xFF141F26),
    background = Color(0xFFF5F5F5),
    onBackground = Color(0xFF1C1B1B),
    surface = Color(0xFFFEFEFE),
    onSurface = Color(0xFF1C1B1B),
    surfaceVariant = Color(0xFFE8E8E8),
    onSurfaceVariant = Color(0xFF464646),
    error = ErrorLight,
    onError = OnErrorLight,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    outline = Color(0xFFCDCDCD),
    outlineVariant = Color(0xFFBDBDBD)
)

private val MonochromeDark = darkColorScheme(
    primary = Color(0xFFBDBDBD),
    onPrimary = Color(0xFF282828),
    primaryContainer = Color(0xFF424242),
    onPrimaryContainer = Color(0xFFE0E0E0),
    secondary = Color(0xFF9E9E9E),
    onSecondary = Color(0xFF1E1E1E),
    secondaryContainer = Color(0xFF2E2E2E),
    onSecondaryContainer = Color(0xFFC8C8C8),
    tertiary = Color(0xFFB0BEC5),
    onTertiary = Color(0xFF203038),
    tertiaryContainer = Color(0xFF344955),
    onTertiaryContainer = Color(0xFFCFD8DC),
    background = Color(0xFF111111),
    onBackground = Color(0xFFE2E2E2),
    surface = Color(0xFF181818),
    onSurface = Color(0xFFE2E2E2),
    surfaceVariant = Color(0xFF404040),
    onSurfaceVariant = Color(0xFFC6C6C6),
    error = ErrorDark,
    onError = OnErrorDark,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    outline = Color(0xFF5A5A5A),
    outlineVariant = Color(0xFF3A3A3A)
)
