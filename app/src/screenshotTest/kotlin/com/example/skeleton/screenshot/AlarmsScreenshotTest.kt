package com.example.skeleton.screenshot

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.example.skeleton.ui.fragment.alarms.component.AlarmsEmptyState

/*
 * --- What this file is for (simple story) ---
 *
 * DoD criterion 10 says an empty Alarms screen says it is empty rather than looking broken. A
 * blank content area still builds, still passes lint, and still passes every unit test — the
 * failure only shows up to a person looking at the rendered screen. So the message is rendered
 * here for real, at the size it occupies, and the picture is committed: a human agrees once that
 * it is there and legible, and validateDebugScreenshotTest fails the day it stops being either.
 *
 * This renders AlarmsEmptyState directly, the same way HomeScreenshotTest renders HomeNoteList
 * rather than HomeFragment's whole screen — the component under test has no NavController
 * dependency, so nothing here needs one and nothing here draws a bottom bar that would misreport
 * AS-5's hidden centre "+" (that decision lives in CoreBottomBar/BottomBarDestination and is
 * outside what this component renders).
 *
 * --- What this does NOT prove ---
 *
 * Not DoD criterion 33 (no hardcoded colour) — a screenshot cannot tell `Color.White` apart from
 * `colorScheme.onSurfaceVariant`; see the same note in ThemeScreenshotTest.kt. Not that the
 * Alarms screen actually calls this component — that is AlarmsFragment/AlarmsLayout, which are
 * private to the main source set exactly as every other screen's Layout is.
 *
 * @author Phong-Kaster
 */

/**
 * The Alarms screen with nothing on it yet — what every user sees the first time they open this
 * tab, and every time after the last alarm is removed.
 *
 * Given a fixed height rather than wrapping content, per C-05: a message that renders correctly
 * inside a 0dp-tall box is still a blank screen to the person holding the phone.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Alarms - empty state", widthDp = 360, heightDp = 320)
@Composable
private fun AlarmsEmptyStateCase() {
    ScreenshotScaffold(
        content = {
            AlarmsEmptyState(modifier = Modifier.height(280.dp))
        },
    )
}
