package com.example.skeleton.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

/**
 * The one and only colour scheme this app has.
 *
 * Calendar Note is dark-only on purpose, so there is no light scheme here, nothing reads the
 * system's day/night setting, and Android 12's "dynamic colour" (which repaints an app from the
 * user's wallpaper) is not used — it would hand control of the palette to the wallpaper and the
 * blue accent would stop being blue.
 *
 * **Every role is assigned explicitly.** None is left at Material's default. A half-filled scheme
 * is the sneaky kind of bug: the roles you did set look right, and some component you never thought
 * about quietly picks up a purple nobody chose.
 *
 * That claim is not left to this comment — `DarkColorSchemeTest` fails if any role still matches
 * `darkColorScheme()`'s default, so a Compose version that introduces new roles breaks the test
 * instead of silently reinstating Material's palette. `internal` rather than `private` only so the
 * test can see it.
 *
 * @author Phong-Kaster
 */
internal val DarkColorScheme: ColorScheme = darkColorScheme(
    // --- Primary: the blue the app is built around ---
    primary = ColorBlue2,
    onPrimary = ColorOnBlue,
    primaryContainer = ColorBlueContainer,
    onPrimaryContainer = ColorOnBlueContainer,
    inversePrimary = ColorBlue,

    // --- Secondary: the darker blue, for quieter fills ---
    secondary = ColorBlue,
    onSecondary = ColorTextPrimaryDark,
    secondaryContainer = ColorBlueContainerMuted,
    onSecondaryContainer = ColorOnBlueContainer,

    // --- Tertiary: a teal accent, so a third state never has to borrow the primary ---
    tertiary = ColorTeal,
    onTertiary = ColorOnTeal,
    tertiaryContainer = ColorTealContainer,
    onTertiaryContainer = ColorOnTealContainer,

    // --- Ground ---
    background = ColorBackgroundDark,
    onBackground = ColorTextPrimaryDark,

    // --- Surfaces resting on the ground ---
    surface = ColorSurfaceDark,
    onSurface = ColorTextPrimaryDark,
    surfaceVariant = ColorSurfaceVariantDark,
    onSurfaceVariant = ColorTextMutedDark,
    surfaceTint = ColorBlue2,
    surfaceDim = ColorBackgroundDark,
    surfaceBright = ColorSurfaceContainerHighestDark,
    surfaceContainerLowest = ColorSurfaceContainerLowestDark,
    surfaceContainerLow = ColorSurfaceContainerLowDark,
    surfaceContainer = ColorSurfaceContainerDark,
    surfaceContainerHigh = ColorSurfaceContainerHighDark,
    surfaceContainerHighest = ColorSurfaceContainerHighestDark,

    // --- Inverse: light-on-dark flipped, used by snackbars ---
    inverseSurface = ColorTextPrimaryDark,
    inverseOnSurface = ColorSurfaceDark,

    // --- Borders and dividers ---
    outline = ColorOutlineDark,
    outlineVariant = ColorOutlineVariantDark,

    // --- Error / destructive ---
    error = ColorError,
    onError = ColorOnError,
    errorContainer = ColorErrorContainer,
    onErrorContainer = ColorOnErrorContainer,

    // --- The dim layer painted behind a dialog or bottom sheet ---
    // Black, which is Material's own default. Worth knowing: because the ground is *also* black,
    // the 32% scrim a ModalBottomSheet paints is invisible over an empty background — it only
    // separates the sheet from the lighter surfaces above it. That is accepted, not overlooked.
    scrim = ColorBackgroundDark,
)

/**
 * Wraps the whole app in the fixed dark scheme.
 *
 * It takes no `darkTheme` or `dynamicColor` parameter — there is nothing to choose. Every screen
 * gets the same palette, on every device, whatever the system theme says.
 *
 * @param content the screen to paint inside the theme.
 * @author Phong-Kaster
 */
@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content,
    )
}
