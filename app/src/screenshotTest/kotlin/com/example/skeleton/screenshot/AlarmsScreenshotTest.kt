package com.example.skeleton.screenshot

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.example.skeleton.ui.fragment.alarms.component.AlarmDeleteConfirmContent
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

/**
 * The question that stands between a tap on a row's bin and the alarm actually going away.
 *
 * DoD criterion 28 ends in a sentence no command can check: "the confirming button is
 * unmistakable". The two controls here have to differ in colour role *and* emphasis — the
 * destructive one filled with `error`, the way out unfilled and plain — and this picture is the
 * only evidence that clause can ever have. See the identical reasoning in
 * `NoteScreenshotTest.kt`'s `DeleteConfirmation` case, which this one deliberately mirrors: two
 * confirmation sheets in one app that read differently would be worse than either alone.
 *
 * **Renders the sheet's content, not the sheet.** Same reason as the note editor's case: a
 * `ModalBottomSheet` draws into its own window, and what layoutlib does with that here is not
 * established. `ground = ScreenshotGround.Surface` is what keeps this faithful to the colour
 * underneath — `CoreBottomSheet`'s `containerColor`, not the screen background these controls
 * never actually sit on.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Alarms - delete confirmation", widthDp = 360, heightDp = 280)
@Composable
private fun AlarmDeleteConfirmationCase() {
    ScreenshotScaffold(
        ground = ScreenshotGround.Surface,
        content = {
            AlarmDeleteConfirmContent()
        },
    )
}

/*
 * --- Why AlarmRow has no pinned case here, on purpose ---
 *
 * `AlarmRow` formats its time through `DateFormat.is24HourFormat(LocalContext.current)` and
 * `LocalConfiguration.current.locales[0]` — both **host-dependent**. A reference image is
 * exact-pixel: recorded on a 24-hour host it shows "07:30", recorded on a 12-hour host it shows
 * "7:30 AM", and `validateDebugScreenshotTest` run on the *other* kind of host would then fail for
 * a reason that has nothing to do with whether the switch or the row are actually correct. This is
 * the identical reason the Note editor carries no reference image at all (see
 * `NoteScreenshotTest.kt`'s own note and `PROJECT.md`'s "References are exact-pixel and
 * host-locked" entry). Fixing it properly means `AlarmRow` taking a pre-formatted time `String`
 * rather than formatting it internally — a real change, and one outside this task's scope since it
 * touches the component's public signature and every existing caller.
 *
 * `AlarmRow.kt` still carries plain `@Preview` cases for both switch states (not `@PreviewTest`),
 * so both remain visible in Android Studio for a human to look at without being pinned to one
 * host's clock format.
 */
