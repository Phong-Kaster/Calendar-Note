package com.example.skeleton.screenshot

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.skeleton.ui.theme.MyApplicationTheme

/**
 * Which of the app's two ground colours sits behind the component in a screenshot case.
 *
 * A name rather than a `Color`, because a `Color` cannot be passed in correctly from a call site:
 * `ScreenshotScaffold` is where `MyApplicationTheme` is applied, so anything a caller writes as
 * `MaterialTheme.colorScheme.…` is read **outside** it, against Material's baseline **light**
 * scheme. Naming the role and resolving it inside is the only version of this that cannot be got
 * wrong by accident.
 *
 * @author Phong-Kaster
 */
enum class ScreenshotGround {

    /** `colorScheme.background` — what `CoreLayout` paints. Right for anything on a screen. */
    Background,

    /** `colorScheme.surface` — what `CoreBottomSheet` paints. Right for sheet contents. */
    Surface,
}

/**
 * Wraps a component in the real theme, on the real ground colour the app paints **underneath that
 * component**.
 *
 * Both halves matter. The `@Preview` functions in the main source set call components bare, so
 * they render against Studio's default white and with Material's baseline colours — which is
 * precisely why a screen full of hardcoded `Color.White` looked fine in the preview pane and
 * unreadable nowhere else. A screenshot taken through this scaffold is a screenshot of what a user
 * sees: theme colours, on the colour that is actually behind them.
 *
 * @param ground which ground the component sits on. [ScreenshotGround.Background] is right for
 *   anything rendered directly on a screen and is the default; sheet contents need
 *   [ScreenshotGround.Surface], because they sit on `CoreBottomSheet`'s `containerColor` and that
 *   is a different colour. Choosing wrong fails nothing today — the two are close enough on this
 *   palette that a human would still approve the picture. It just means the reference quietly
 *   stops defending the contrast it was recorded to defend, having tested a label against a
 *   colour the label never appears on.
 * @param content the component under the camera.
 * @author Phong-Kaster
 */
@Composable
fun ScreenshotScaffold(
    ground: ScreenshotGround = ScreenshotGround.Background,
    content: @Composable () -> Unit,
) {
    MyApplicationTheme {
        // Resolved here, inside the theme, and not as a default argument or at the call site —
        // both of those are evaluated in the caller's composition, where the scheme is still
        // Material's light baseline. That mistake reads as a no-op and re-records every
        // reference image on a white ground.
        val color = when (ground) {
            ScreenshotGround.Background -> MaterialTheme.colorScheme.background
            ScreenshotGround.Surface -> MaterialTheme.colorScheme.surface
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color)
                .padding(12.dp),
        ) {
            content()
        }
    }
}
