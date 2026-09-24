package com.example.skeleton.ui.theme

import androidx.compose.ui.graphics.Color

// ---------- Existing semantic tokens (keep for compatibility) ----------

/** Semantic token for blue accent color used in headers and icons on light surfaces. */
val ColorBlue = Color(0xFF2F70BC)

/** Semantic token for primary blue accent color. */
val ColorBlue2 = Color(0xFF35A0F5)

// ---------- Dark theme surface colors (black family) ----------

/**
 * Background color for the entire app.
 * Pure black for maximum contrast in dark theme.
 *
 * @author Phong-Kaster
 */
val DarkBackground = Color(0xFF000000)

/**
 * Default surface color for cards, dialogs, and elevated content.
 * Pure black to match the background.
 *
 * @author Phong-Kaster
 */
val DarkSurface = Color(0xFF000000)

/**
 * Variant surface for secondary elevated surfaces.
 * Very dark grey for subtle differentiation.
 *
 * @author Phong-Kaster
 */
val DarkSurfaceVariant = Color(0xFF1A1A1A)

/**
 * Dimmest surface color for lowest-priority elevated content.
 * Almost pure black.
 *
 * @author Phong-Kaster
 */
val DarkSurfaceDim = Color(0xFF0A0A0A)

/**
 * Brightest surface color for highest-priority elevated content.
 * Dark grey, lighter than every surface container.
 *
 * @author Phong-Kaster
 */
val DarkSurfaceBright = Color(0xFF2C2C2C)

/**
 * Container surface — default level.
 * Dark grey for container backgrounds.
 *
 * @author Phong-Kaster
 */
val DarkSurfaceContainer = Color(0xFF121212)

/**
 * Container surface — low level.
 * Very dark grey, slightly lighter than dim.
 *
 * @author Phong-Kaster
 */
val DarkSurfaceContainerLow = Color(0xFF0F0F0F)

/**
 * Container surface — lowest level (just above the black ground).
 * Almost pure black.
 *
 * @author Phong-Kaster
 */
val DarkSurfaceContainerLowest = Color(0xFF050505)

/**
 * Container surface — high level.
 * Slightly lighter dark grey.
 *
 * @author Phong-Kaster
 */
val DarkSurfaceContainerHigh = Color(0xFF1A1A1A)

/**
 * Container surface — highest level.
 * Lightest dark grey in the container range.
 *
 * @author Phong-Kaster
 */
val DarkSurfaceContainerHighest = Color(0xFF242424)

/**
 * Surface tint applied to elevated surfaces.
 * Uses the primary blue color.
 *
 * @author Phong-Kaster
 */
val DarkSurfaceTint = Color(0xFF35A0F5)

// ---------- Dark theme text colors (white-ish) ----------

/**
 * Primary text color on backgrounds and surfaces.
 * Off-white for high contrast on black.
 *
 * @author Phong-Kaster
 */
val DarkOnBackground = Color(0xFFF2F2F2)

/**
 * Primary text color on surfaces.
 * Off-white for high contrast on black.
 *
 * @author Phong-Kaster
 */
val DarkOnSurface = Color(0xFFF2F2F2)

/**
 * Secondary text color on surfaces.
 * Light grey for muted emphasis.
 *
 * @author Phong-Kaster
 */
val DarkOnSurfaceVariant = Color(0xFFC4C7CC)

/**
 * Inverse text color for light surfaces.
 * Dark grey for text on light backgrounds.
 *
 * @author Phong-Kaster
 */
val DarkInverseSurface = Color(0xFFECECEC)

/**
 * Text color on inverse surfaces.
 * Dark grey for contrast.
 *
 * @author Phong-Kaster
 */
val DarkInverseOnSurface = Color(0xFF1A1A1A)

// ---------- Dark theme primary colors (blue) ----------

/**
 * Primary brand color.
 * Bright blue for primary actions and highlights.
 *
 * @author Phong-Kaster
 */
val DarkPrimary = Color(0xFF35A0F5)

/**
 * Text color on primary backgrounds.
 * Dark navy for high contrast text on the bright blue.
 * Ensures at least 4.5:1 contrast ratio with primary.
 *
 * @author Phong-Kaster
 */
val DarkOnPrimary = Color(0xFF00213A)

/**
 * Container color for primary-accented content.
 * Dark blue for tonal primary actions.
 *
 * @author Phong-Kaster
 */
val DarkPrimaryContainer = Color(0xFF004A77)

/**
 * Text color on primary container backgrounds.
 * Light blue for high contrast text on dark blue.
 * Ensures at least 4.5:1 contrast ratio with container.
 *
 * @author Phong-Kaster
 */
val DarkOnPrimaryContainer = Color(0xFFD0E4FF)

/**
 * Inverse primary color for alternate primary contexts.
 * Darker blue for inverse primary backgrounds.
 *
 * @author Phong-Kaster
 */
val DarkInversePrimary = Color(0xFF1F5FA0)

// ---------- Dark theme secondary colors ----------

/**
 * Secondary brand color.
 * Cyan for secondary actions and accents.
 *
 * @author Phong-Kaster
 */
val DarkSecondary = Color(0xFF4FC3F7)

/**
 * Text color on secondary backgrounds.
 * Dark teal for contrast.
 *
 * @author Phong-Kaster
 */
val DarkOnSecondary = Color(0xFF003A52)

/**
 * Container color for secondary-accented content.
 * Dark cyan for tonal secondary actions.
 *
 * @author Phong-Kaster
 */
val DarkSecondaryContainer = Color(0xFF00526A)

/**
 * Text color on secondary container backgrounds.
 * Light cyan for contrast.
 *
 * @author Phong-Kaster
 */
val DarkOnSecondaryContainer = Color(0xFFB1EBFF)

// ---------- Dark theme tertiary colors ----------

/**
 * Tertiary accent color.
 * Orange-yellow for tertiary actions and highlights.
 *
 * @author Phong-Kaster
 */
val DarkTertiary = Color(0xFFFFB74D)

/**
 * Text color on tertiary backgrounds.
 * Dark brown for contrast.
 *
 * @author Phong-Kaster
 */
val DarkOnTertiary = Color(0xFF4D2E00)

/**
 * Container color for tertiary-accented content.
 * Dark orange for tonal tertiary actions.
 *
 * @author Phong-Kaster
 */
val DarkTertiaryContainer = Color(0xFF664D00)

/**
 * Text color on tertiary container backgrounds.
 * Light yellow for contrast.
 *
 * @author Phong-Kaster
 */
val DarkOnTertiaryContainer = Color(0xFFFFDDB3)

// ---------- Dark theme error colors ----------

/**
 * Error color for destructive actions and error states.
 * Light red-pink for visibility on dark backgrounds.
 *
 * @author Phong-Kaster
 */
val DarkError = Color(0xFFFFB4AB)

/**
 * Text color on error backgrounds.
 * Dark red for high contrast.
 *
 * @author Phong-Kaster
 */
val DarkOnError = Color(0xFF690005)

/**
 * Container color for error-accented content.
 * Dark red for error state backgrounds.
 *
 * @author Phong-Kaster
 */
val DarkErrorContainer = Color(0xFF93000A)

/**
 * Text color on error container backgrounds.
 * Light pink for contrast on dark red.
 *
 * @author Phong-Kaster
 */
val DarkOnErrorContainer = Color(0xFFFFDAD6)

// ---------- Dark theme neutral colors ----------

/**
 * Outline color for borders and dividers.
 * Medium grey for clear visual separation.
 *
 * @author Phong-Kaster
 */
val DarkOutline = Color(0xFF8E9199)

/**
 * Variant outline color for subtle borders.
 * Darker grey for muted separation.
 *
 * @author Phong-Kaster
 */
val DarkOutlineVariant = Color(0xFF44474E)

/**
 * Scrim color for screen dimming overlays.
 * Pure black with optional opacity.
 *
 * @author Phong-Kaster
 */
val DarkScrim = Color(0xFF000000)
