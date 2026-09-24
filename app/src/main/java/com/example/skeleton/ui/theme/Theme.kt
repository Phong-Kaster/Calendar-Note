package com.example.skeleton.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * Fixed dark color scheme for the entire app.
 * Every Material 3 color role is explicitly assigned from named tokens in Color.kt.
 * This scheme is always used regardless of system settings or Android version.
 *
 * @author Phong-Kaster
 */
private val FixedDarkColorScheme = darkColorScheme(
    // Primary colors (blue)
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,

    // Secondary colors (cyan)
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,

    // Tertiary colors (orange-yellow)
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,

    // Background and surface
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkSurfaceTint,

    // Inverse colors
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,

    // Error colors
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,

    // Outlines
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,

    // Surface container variants (ordered by elevation: dim/lowest → brightest/highest)
    surfaceBright = DarkSurfaceBright,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceDim = DarkSurfaceDim,
)

/**
 * Application theme using a fixed dark color scheme.
 *
 * The theme always uses the dark scheme regardless of system settings.
 * The [darkTheme] and [dynamicColor] parameters are provided for API compatibility
 * but are ignored — this theme is always dark with no dynamic colors.
 *
 * @param darkTheme Ignored; the theme is always dark.
 * @param dynamicColor Ignored; the theme uses explicit fixed colors from [Color.kt].
 * @param content The composable content to render within this theme.
 * @author Phong-Kaster
 */
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = FixedDarkColorScheme,
        typography = Typography,
        content = content
    )
}
