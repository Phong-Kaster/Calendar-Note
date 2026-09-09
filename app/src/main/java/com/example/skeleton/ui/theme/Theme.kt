package com.example.skeleton.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * The one colour scheme this app has.
 *
 * Every role is filled in, not just primary/secondary/tertiary. Leaving the rest at Material's
 * baseline is what previously made surfaces read as faintly purple in a blue-branded app, and it
 * meant any component reaching for `surfaceVariant` or `onSurfaceVariant` got a colour nobody
 * had chosen.
 */
private val DarkColorScheme = darkColorScheme(
    primary = BlueDarkPrimary,
    onPrimary = BlueDarkOnPrimary,
    primaryContainer = BlueDarkPrimaryContainer,
    onPrimaryContainer = BlueDarkOnPrimaryContainer,
    secondary = BlueDarkSecondary,
    onSecondary = BlueDarkOnSecondary,
    tertiary = BlueDarkTertiary,
    onTertiary = BlueDarkOnTertiary,
    background = BlueDarkBackground,
    onBackground = BlueDarkOnBackground,
    surface = BlueDarkSurface,
    onSurface = BlueDarkOnSurface,
    surfaceVariant = BlueDarkSurfaceVariant,
    onSurfaceVariant = BlueDarkOnSurfaceVariant,
    outline = BlueDarkOutline,
    error = BlueDarkError,
    onError = BlueDarkOnError,
)

/**
 * Applies the app's colour scheme and typography.
 *
 * **This app is dark-only, deliberately.** `CoreLayout` paints a black ground under every screen,
 * so a light scheme could never actually take effect — it only produced screens where some text
 * used `onSurface` (near-black, invisible on black) while other text hardcoded white. Rather than
 * leave a light scheme that renders unusable, there is one scheme and every colour is chosen
 * against black.
 *
 * Dynamic colour is intentionally absent too: the blue is brand, not wallpaper.
 *
 * If light mode is ever wanted, the change is not in this file alone — `CoreLayout`'s hardcoded
 * black ground and `customizedTextStyle`'s white default both have to become theme-driven first.
 */
@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
