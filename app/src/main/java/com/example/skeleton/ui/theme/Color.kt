package com.example.skeleton.ui.theme

import androidx.compose.ui.graphics.Color

val ColorBlue = Color(0xFF2F70BC)
val ColorBlue2 = Color(0xFF35A0F5)

// --- Dark blue Material3 scheme ---------------------------------------------------------------
// This app is dark-only: CoreLayout paints a black ground, and every scheme role below is chosen
// to sit on it. There is deliberately no light scheme — see MyApplicationTheme.
//
// Contrast notes, because two of these are load-bearing rather than decorative:
//   BlueDarkOnPrimary is DARK on purpose. `primary` is a pale blue, so anything drawn on top of a
//   primary-filled shape (the calendar's "today" circle) needs a dark foreground, not white.
//   BlueDarkSurfaceVariant is lifted just enough to read as a card against pure black without
//   turning into a grey slab.

val BlueDarkPrimary = Color(0xFF9FCBFF)
val BlueDarkOnPrimary = Color(0xFF00325B)
val BlueDarkPrimaryContainer = Color(0xFF1E3A5F)
val BlueDarkOnPrimaryContainer = Color(0xFFD3E4FF)

val BlueDarkSecondary = Color(0xFFB8CDE6)
val BlueDarkOnSecondary = Color(0xFF22323F)
val BlueDarkTertiary = Color(0xFF7FCFFF)
val BlueDarkOnTertiary = Color(0xFF003549)

val BlueDarkBackground = Color(0xFF000000)
val BlueDarkOnBackground = Color(0xFFE3E8EF)
val BlueDarkSurface = Color(0xFF000000)
val BlueDarkOnSurface = Color(0xFFE3E8EF)
val BlueDarkSurfaceVariant = Color(0xFF1B2430)
val BlueDarkOnSurfaceVariant = Color(0xFFA8B4C4)
val BlueDarkOutline = Color(0xFF3A4757)

val BlueDarkError = Color(0xFFFFB4AB)
val BlueDarkOnError = Color(0xFF690005)
