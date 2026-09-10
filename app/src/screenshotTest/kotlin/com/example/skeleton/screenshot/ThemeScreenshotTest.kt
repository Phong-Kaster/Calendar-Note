package com.example.skeleton.screenshot

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/*
 * --- What this file is for (simple story) ---
 *
 * DoD criterion 1 says the app uses one fixed dark scheme and never follows the system light/dark
 * setting. That sentence cannot be checked by reading code: `assembleDebug` is just as happy with
 * an unreadable palette, and `DarkColorSchemeTest` can only prove a role was *assigned*, never that
 * the colour put there can be seen.
 *
 * So the cases below render the palette itself. A human looks at the generated images once and
 * agrees they are right; from then on `validateDebugScreenshotTest` fails if the rendering drifts.
 * That is what turns "the accent reads as blue" from an opinion into evidence.
 *
 * Each swatch prints its role name *in that role's own on-colour*, so an unreadable pairing shows
 * up as unreadable text rather than as a number nobody checked. That is the exact failure mode
 * T-001 found twice by computing contrast ratios by hand: `outline` at 2.19:1 and `outlineVariant`
 * at 1.26:1 both looked like perfectly ordinary greys in the source file.
 *
 * --- What these images do NOT prove: be precise about it ---
 *
 * They do **not** prove DoD criterion 2 ("colour comes from the theme, never hardcoded"). A
 * screenshot cannot tell `Color.White` apart from `colorScheme.onBackground` when both render
 * #FFFFFF. Fourteen files under `ui/` still hold hardcoded literals today and every case here
 * passes anyway — see `knowledge/ISSUES.md`. Criterion 2 needs a *static* check (a lint rule or a
 * grep over `ui/` outside `ui/theme/`), and until one exists it is checked by review, by hand.
 * Saying so here matters: a comment that claims coverage it does not have is how a criterion
 * quietly stops being checked at all.
 *
 * What they do prove: criterion 1 (the fixed dark scheme renders as declared, in both `uiMode`s),
 * that no role has drifted to an illegible value, and that the accent stays legible on the ground
 * in shipped code (`CoreBottomBar`, `CoreLayout`).
 *
 * One deliberate deviation from the house rules: single-line `Text` here uses
 * `overflow = TextOverflow.Ellipsis` instead of `Modifier.basicMarquee(...)`. A marquee animates,
 * and an animating component does not render the same pixels twice — which would make every
 * validation run a coin flip.
 *
 * @author Phong-Kaster
 */

/**
 * The blue the whole app is built around, plus every accent that must not be mistaken for it.
 *
 * `error` matters as much as `primary` here: DoD criterion 7 wants a delete control that is
 * visually distinct from the cancel next to it, and that is only true if the red and the blue do
 * not read as the same weight of "important".
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Palette - accents", widthDp = 360, heightDp = 420)
@Composable
private fun PaletteAccents() {
    ScreenshotScaffold(
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Swatch(
                    label = "primary  #35A0F5",
                    fill = MaterialTheme.colorScheme.primary,
                    onFill = MaterialTheme.colorScheme.onPrimary,
                )
                Swatch(
                    label = "primaryContainer",
                    fill = MaterialTheme.colorScheme.primaryContainer,
                    onFill = MaterialTheme.colorScheme.onPrimaryContainer,
                )
                Swatch(
                    label = "secondary",
                    fill = MaterialTheme.colorScheme.secondary,
                    onFill = MaterialTheme.colorScheme.onSecondary,
                )
                Swatch(
                    label = "secondaryContainer",
                    fill = MaterialTheme.colorScheme.secondaryContainer,
                    onFill = MaterialTheme.colorScheme.onSecondaryContainer,
                )
                Swatch(
                    label = "tertiary",
                    fill = MaterialTheme.colorScheme.tertiary,
                    onFill = MaterialTheme.colorScheme.onTertiary,
                )
                Swatch(
                    label = "tertiaryContainer",
                    fill = MaterialTheme.colorScheme.tertiaryContainer,
                    onFill = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Swatch(
                    label = "error",
                    fill = MaterialTheme.colorScheme.error,
                    onFill = MaterialTheme.colorScheme.onError,
                )
                Swatch(
                    label = "errorContainer",
                    fill = MaterialTheme.colorScheme.errorContainer,
                    onFill = MaterialTheme.colorScheme.onErrorContainer,
                )
                // Drawn the way Material actually uses it: `inversePrimary` is the accent for an
                // action label sitting ON `inverseSurface` (a Snackbar's button), never a fill with
                // its own on-colour. Pairing it with `onPrimaryContainer` — as this row first did —
                // invents a combination the app never paints, and that invented pairing measures
                // 3.94:1, under AA. A reference image must show a real pairing or it asks a human
                // to approve something that will never appear on screen.
                Swatch(
                    label = "inversePrimary on inverseSurface",
                    fill = MaterialTheme.colorScheme.inverseSurface,
                    onFill = MaterialTheme.colorScheme.inversePrimary,
                )
            }
        },
    )
}

/**
 * The greys. Every one of these sits on a pure-black ground, which is why they are worth pinning:
 * a card only reads as raised if its surface is distinguishable from the ground behind it, and on
 * black that difference is a handful of levels wide.
 *
 * One role of the 36 is deliberately absent: `scrim`. It is pure black by design and is only ever
 * painted as a translucent veil over other content, so a solid swatch of it on the black ground
 * would be a black rectangle proving nothing. `DarkColorSchemeTest` asserts its value instead.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Palette - surfaces", widthDp = 360, heightDp = 540)
@Composable
private fun PaletteSurfaces() {
    ScreenshotScaffold(
        content = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Swatch(
                    label = "background (the ground)",
                    fill = MaterialTheme.colorScheme.background,
                    onFill = MaterialTheme.colorScheme.onBackground,
                )
                Swatch(
                    label = "surfaceContainerLowest",
                    fill = MaterialTheme.colorScheme.surfaceContainerLowest,
                    onFill = MaterialTheme.colorScheme.onSurface,
                )
                Swatch(
                    label = "surfaceContainerLow",
                    fill = MaterialTheme.colorScheme.surfaceContainerLow,
                    onFill = MaterialTheme.colorScheme.onSurface,
                )
                Swatch(
                    label = "surface",
                    fill = MaterialTheme.colorScheme.surface,
                    onFill = MaterialTheme.colorScheme.onSurface,
                )
                Swatch(
                    label = "surfaceContainer",
                    fill = MaterialTheme.colorScheme.surfaceContainer,
                    onFill = MaterialTheme.colorScheme.onSurface,
                )
                Swatch(
                    label = "surfaceVariant / onSurfaceVariant",
                    fill = MaterialTheme.colorScheme.surfaceVariant,
                    onFill = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Swatch(
                    label = "surfaceContainerHigh",
                    fill = MaterialTheme.colorScheme.surfaceContainerHigh,
                    onFill = MaterialTheme.colorScheme.onSurface,
                )
                Swatch(
                    label = "surfaceContainerHighest",
                    fill = MaterialTheme.colorScheme.surfaceContainerHighest,
                    onFill = MaterialTheme.colorScheme.onSurface,
                )
                // `surfaceDim` is deliberately the ground colour (`Theme.kt`), so this row has no
                // visible edge. That is the correct picture, not a missing swatch — labelled as
                // such so nobody approving this image reads it as a bug and "fixes" the theme.
                Swatch(
                    label = "surfaceDim (= the ground, on purpose)",
                    fill = MaterialTheme.colorScheme.surfaceDim,
                    onFill = MaterialTheme.colorScheme.onSurface,
                )
                Swatch(
                    label = "surfaceBright",
                    fill = MaterialTheme.colorScheme.surfaceBright,
                    onFill = MaterialTheme.colorScheme.onSurface,
                )
                // `surfaceTint` is what Material blends into an elevated surface, so it is normally
                // seen only diluted. Shown solid here because a wrong value is invisible otherwise.
                Swatch(
                    label = "surfaceTint (blended into elevation)",
                    fill = MaterialTheme.colorScheme.surfaceTint,
                    onFill = MaterialTheme.colorScheme.onPrimary,
                )
                Swatch(
                    label = "inverseSurface / inverseOnSurface",
                    fill = MaterialTheme.colorScheme.inverseSurface,
                    onFill = MaterialTheme.colorScheme.inverseOnSurface,
                )
            }
        },
    )
}

/**
 * The two boundary roles, drawn as boundaries rather than as swatches — because that is the only
 * way their failure is visible.
 *
 * `outline` is the card's border and `outlineVariant` is what a bare `HorizontalDivider()` paints.
 * Both were set to values under their contrast floor in T-001 and both looked fine as hex. If
 * either edge is missing in this image, the palette is wrong, not the image.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Outlines and text", widthDp = 360, heightDp = 220)
@Composable
private fun OutlinesAndText() {
    ScreenshotScaffold(
        content = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(16.dp),
                    )
                    .padding(16.dp),
            ) {
                Text(
                    text = "Primary text — a note title",
                    style = customizedTextStyle(fontSize = 16, fontWeight = 600),
                )
                Text(
                    text = "Muted text — the date under it",
                    style = customizedTextStyle(
                        fontSize = 14,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
                HorizontalDivider()
                Text(
                    text = "A second row, below the divider",
                    style = customizedTextStyle(fontSize = 14),
                )
            }
        },
    )
}

/**
 * The one component that already paints the accent for real: `CoreBottomBar`'s centre action
 * button is a `primary`-filled circle with an `onPrimary` icon on it.
 *
 * This is the case that answers "is the blue legible on black" about shipped code rather than
 * about a swatch. The bar itself is transparent, so it is also a check that the ground shows
 * through as the ground and not as some component's own fill.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Bottom bar", widthDp = 360, heightDp = 110)
@Composable
private fun BottomBar() {
    ScreenshotScaffold(
        content = {
            CoreBottomBar(onCreateNote = {})
        },
    )
}

/**
 * The same bar again, rendered with the system asked for **night** mode.
 *
 * This is the one case whose whole point is that it is a duplicate. DoD criterion 1 says nothing
 * reads the system light/dark setting; [BottomBar] above renders at the default `uiMode`, this one
 * at `UI_MODE_NIGHT_YES`, and the proof is that the two reference images are the same picture. The
 * day the theme starts calling `isSystemInDarkTheme()`, exactly one of these two goes light and
 * validation fails — which is the only cheap way a machine can notice.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(
    name = "Bottom bar - system night",
    widthDp = 360,
    heightDp = 110,
    uiMode = Configuration.UI_MODE_NIGHT_YES,
)
@Composable
private fun BottomBarSystemNight() {
    ScreenshotScaffold(
        content = {
            CoreBottomBar(onCreateNote = {})
        },
    )
}

/**
 * The real screen shell, `CoreLayout`, rendering its own ground.
 *
 * Every other case here paints the ground through [ScreenshotScaffold], which is a *copy* of what
 * `CoreLayout` does rather than `CoreLayout` itself. That copy is the blind spot: if `CoreLayout`
 * ever went back to hardcoding its background, every scaffold-based image would stay green while
 * the actual app changed colour underneath the user. This case is the one that would fail.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Screen shell", widthDp = 360, heightDp = 160)
@Composable
private fun ScreenShell() {
    MyApplicationTheme {
        CoreLayout(
            content = {
                Text(
                    text = "On CoreLayout's own ground",
                    style = customizedTextStyle(fontSize = 14),
                )
            },
        )
    }
}

/**
 * One colour role, drawn as a filled bar with its own name written on it in its on-colour.
 *
 * @param label the role name, and the hex where the exact value is part of the requirement.
 * @param fill the colour being shown.
 * @param onFill the colour Material pairs with [fill] for text and icons drawn on top of it.
 * @author Phong-Kaster
 */
@Composable
private fun Swatch(
    label: String,
    fill: Color,
    onFill: Color,
) {
    Box(
        contentAlignment = Alignment.CenterStart,
        modifier = Modifier
            .fillMaxWidth()
            .height(34.dp)
            .background(color = fill, shape = RoundedCornerShape(6.dp))
            .padding(horizontal = 10.dp),
    ) {
        Text(
            text = label,
            style = customizedTextStyle(fontSize = 13, fontWeight = 500, color = onFill),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}
