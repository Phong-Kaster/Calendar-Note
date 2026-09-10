package com.example.skeleton.ui.theme

import androidx.compose.ui.graphics.Color

/*
 * --- The app's colour palette (simple story) ---
 *
 * Calendar Note is a dark-only app with a blue accent. Every colour it paints is declared here,
 * once, and then handed to Material through the single dark scheme in `Theme.kt`. Composables read
 * colours from `MaterialTheme.colorScheme.*` — they never write `Color(0xFF...)` themselves.
 *
 * Why that matters: a hardcoded colour only looks right by accident, because of whatever background
 * happens to sit underneath it today. Change the background and it breaks, silently, with the build
 * still green.
 *
 * The names below say what a colour *is for*, not what it looks like. "ColorTextMutedDark" survives
 * a redesign; "ColorGrey" does not. Two exceptions predate this scheme and are kept because other
 * files already reference them: `ColorBlue` and `ColorBlue2` describe only their appearance. Read
 * their KDoc for the role each actually fills.
 *
 * A note on the contrast figures quoted throughout: they are WCAG 2.x ratios against the ground or
 * the stated fill, and they are written down because on a pure-black app *every* mid-grey looks
 * plausible in isolation. Two tokens here were first set at values that measured under their floor
 * and would have failed invisibly.
 *
 * @author Phong-Kaster
 */

// ---------- Brand blues ----------

/** The darker brand blue. Used for filled containers and the secondary role. */
val ColorBlue = Color(0xFF2F70BC)

/** The app's primary blue. Light enough to stay legible on a black background. */
val ColorBlue2 = Color(0xFF35A0F5)

/** Very dark navy for text and icons drawn *on top of* [ColorBlue2]. White on that blue is unreadable. */
val ColorOnBlue = Color(0xFF03121F)

/**
 * A deep blue fill for tonal containers (selected day cells, chips, the FAB).
 *
 * This — not [ColorBlue] — is `primaryContainer`. Material draws `FloatingActionButton` and
 * `Card(containerColor = primaryContainer)` labels with `onPrimaryContainer`, and `#CFE6FF` on
 * `ColorBlue` is only 3.94:1: fine for an icon, under AA for text. On this darker fill the same
 * on-colour reaches 9.12:1.
 */
val ColorBlueContainer = Color(0xFF123A5F)

/** A muted slate-blue fill for the quieter `secondaryContainer` role. 11.3:1 under [ColorOnBlueContainer]. */
val ColorBlueContainerMuted = Color(0xFF1E2A38)

/** Pale blue text and icons drawn on [ColorBlue], [ColorBlueContainer] and other blue fills. */
val ColorOnBlueContainer = Color(0xFFCFE6FF)

// ---------- Ground and surfaces ----------

/*
 * The ground is true black: `CoreLayout` paints it behind every screen, and the app is dark-only,
 * so there is no light ground to reconcile with. Surfaces are progressively lighter greys that sit
 * *on* that black, which is how a card reads as raised without drawing a shadow.
 */

/** The screen's ground colour — what the user sees behind everything else. */
val ColorBackgroundDark = Color(0xFF000000)

/** Default surface for cards, sheets and menus resting on the ground. */
val ColorSurfaceDark = Color(0xFF12171E)

/** A slightly lighter surface used to separate a region from the plain surface around it. */
val ColorSurfaceVariantDark = Color(0xFF1C232C)

/** Surface tiers, lowest (nearly the ground) to highest (most raised). */
val ColorSurfaceContainerLowestDark = Color(0xFF0A0E13)
val ColorSurfaceContainerLowDark = Color(0xFF0F141A)
val ColorSurfaceContainerDark = Color(0xFF161C24)
val ColorSurfaceContainerHighDark = Color(0xFF1F2732)
val ColorSurfaceContainerHighestDark = Color(0xFF262F3A)

// ---------- Text and icons ----------

/**
 * Primary text and icon colour, and the default `color` of `customizedTextStyle` — so every `Text`
 * in the app that does not name a colour is painted from this token rather than from a literal.
 */
val ColorTextPrimaryDark = Color(0xFFFFFFFF)

/** Secondary text: labels, hints, dates, anything that must read as quieter than the main text. */
val ColorTextMutedDark = Color(0xFFA9B4C0)

// ---------- Outlines ----------

/**
 * Visible borders — a text-field outline, a calendar day-cell boundary, anything whose *edge* is
 * the thing carrying meaning.
 *
 * Light enough to clear 3:1 against the black ground (3.35:1), which is the minimum for a boundary
 * a user is meant to see. The obvious darker grey looks tidier in isolation and lands near 2.2:1,
 * where a day-cell border quietly stops being visible at all.
 */
val ColorOutlineDark = Color(0xFF55616E)

/**
 * Subtle dividers that only need to separate, not to be noticed. Do **not** use it for a border
 * that carries meaning; that is what [ColorOutlineDark] is for.
 *
 * "Subtle" still has a floor. This is Material's own dark-baseline divider weight (≈1.9:1 on
 * `surface`); the darker `#232B34` it started as measured 1.26:1, which is not a subtle divider
 * but an absent one — and `outlineVariant` is what `HorizontalDivider` and `OutlinedCard` use by
 * default, so every separator in the app would silently have been missing.
 */
val ColorOutlineVariantDark = Color(0xFF3B4652)

// ---------- Tertiary accent ----------

/** A teal accent for the occasional element that must not read as the primary action. */
val ColorTeal = Color(0xFF4FD8C4)

/** Dark teal for text and icons drawn on [ColorTeal]. */
val ColorOnTeal = Color(0xFF00201B)

/** A deep teal fill, with its pale on-colour. */
val ColorTealContainer = Color(0xFF00504A)
val ColorOnTealContainer = Color(0xFFB6F2E8)

// ---------- Error ----------

/**
 * The destructive / error red. Bright enough to be unmistakably different from the blue primary,
 * which is what makes a "Delete" control visually distinct from a "Cancel" one.
 */
val ColorError = Color(0xFFFF6B6B)

/** Very dark red for text and icons drawn on [ColorError]. */
val ColorOnError = Color(0xFF2A0000)

/** A deep red fill for error banners, with its pale on-colour. */
val ColorErrorContainer = Color(0xFF5C1A1A)
val ColorOnErrorContainer = Color(0xFFFFDAD6)
