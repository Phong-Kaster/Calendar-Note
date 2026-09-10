package com.example.skeleton.screenshot

import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.android.tools.screenshot.PreviewTest
import com.example.skeleton.ui.fragment.home.component.HomeNoteList

/*
 * --- What this file is for (simple story) ---
 *
 * DoD criterion 4 ends with a sentence that no compiler can check: "When there are no notes, Home
 * shows an explicit empty-state message — never a blank region."
 *
 * A blank region is what you get for free. It is what the screen does when somebody deletes the
 * message, or wraps it in a condition that is never true, or centres it inside a box of zero
 * height. Every one of those still builds, still passes the unit tests, and still lints clean —
 * the user simply opens the app for the first time and sees nothing, which reads as a broken app
 * rather than as an empty one.
 *
 * So the empty state is rendered here for real, at the size it occupies on a screen, and the
 * picture is committed. A human agrees once that the message is there and legible; after that
 * `validateDebugScreenshotTest` fails the day it stops being either.
 *
 * --- What this does NOT prove ---
 *
 * Not DoD criterion 2. A screenshot cannot tell `Color.White` from `colorScheme.onBackground`;
 * both render the same pixels. See the same note in `ThemeScreenshotTest.kt`.
 *
 * Two gaps, both deliberate, both worth stating so nobody reads a green run as more than it is.
 *
 * **Populated rows are not pinned.** A note row prints its date through
 * `DateTimeFormatter.ofLocalizedDate(...)`, whose output follows the rendering host's locale
 * ("14 Mar 2026" here, "14.03.2026" on a German machine). A reference image of that fails on a
 * colleague's laptop for a reason that has nothing to do with the app being wrong — and the only
 * way to make it pass again is `updateDebugScreenshotTest`, the one command nobody is allowed to
 * reach for. The references are already host-locked enough (`knowledge/ISSUES.md`). The
 * consequence: the row's own "Untitled note" fallback, for a note with neither a title nor a body,
 * is checked by nobody. It arrives with the rows, when they become clickable.
 *
 * **This renders `HomeNoteList`, not `HomeLayout`.** So it defends the list's own empty state, not
 * Home's wiring to it: if `HomeLayout` stopped calling `HomeNoteList`, or held `showLoading` true
 * forever, Home would be blank on a device and this case would still pass. `HomeLayout` is
 * `private`, as the house rules require of every screen layout, so this source set cannot reach
 * it — the screen-level gate would cost making it visible, which is a change to a rule rather
 * than to a test.
 *
 * @author Phong-Kaster
 */

/**
 * Home with nothing in it — the very first thing a new user sees.
 *
 * Given a fixed height rather than wrapping content, because "the message fills the screen" is
 * half of what is being checked: a message that renders correctly inside a 0dp-tall box is still
 * a blank screen to the person holding the phone.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Notes - empty state", widthDp = 360, heightDp = 320)
@Composable
private fun NotesEmptyState() {
    ScreenshotScaffold(
        content = {
            HomeNoteList(
                notes = emptyList(),
                modifier = Modifier.height(280.dp),
            )
        },
    )
}
