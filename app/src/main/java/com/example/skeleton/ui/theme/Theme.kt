package com.example.skeleton.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

/**
 * The one and only colour scheme of the app. It is dark and EVERY role is filled in,
 * so no screen ever falls back to a Material default that might be unreadable on the
 * dark ground (for example black text on a near-black background).
 *
 * @author Phong-Kaster
 */
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = DarkOnPrimary,
    primaryContainer = DarkPrimaryContainer,
    onPrimaryContainer = DarkOnPrimaryContainer,
    inversePrimary = DarkInversePrimary,
    secondary = DarkSecondary,
    onSecondary = DarkOnSecondary,
    secondaryContainer = DarkSecondaryContainer,
    onSecondaryContainer = DarkOnSecondaryContainer,
    tertiary = DarkTertiary,
    onTertiary = DarkOnTertiary,
    tertiaryContainer = DarkTertiaryContainer,
    onTertiaryContainer = DarkOnTertiaryContainer,
    background = DarkBackground,
    onBackground = DarkOnBackground,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    surfaceTint = DarkSurfaceTint,
    inverseSurface = DarkInverseSurface,
    inverseOnSurface = DarkInverseOnSurface,
    error = DarkError,
    onError = DarkOnError,
    errorContainer = DarkErrorContainer,
    onErrorContainer = DarkOnErrorContainer,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    scrim = DarkScrim,
    surfaceBright = DarkSurfaceBright,
    surfaceContainer = DarkSurfaceContainer,
    surfaceContainerHigh = DarkSurfaceContainerHigh,
    surfaceContainerHighest = DarkSurfaceContainerHighest,
    surfaceContainerLow = DarkSurfaceContainerLow,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    surfaceDim = DarkSurfaceDim,
)

/**
 * The app theme. The app is ALWAYS dark: the [darkTheme] flag is kept only so older
 * callers (like `CoreFragment`, which calls `MyApplicationTheme()`) still compile, but it
 * is ignored on purpose. Think of it like a light switch that is taped in the "off" spot.
 *
 * Example:
 * ```kotlin
 * MyApplicationTheme(
 *     content = {
 *         Text(text = "Hi", style = customizedTextStyle(color = MaterialTheme.colorScheme.onBackground))
 *     }
 * )
 * ```
 *
 * @param darkTheme ignored — the app is always dark.
 * @param dynamicColor when true on Android 12+, uses the wallpaper-based DARK scheme; default false.
 * @param content the screen to draw inside the theme.
 * @author Phong-Kaster
 */
@Suppress("UNUSED_PARAMETER")
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        dynamicDarkColorScheme(LocalContext.current)
    } else {
        DarkColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
