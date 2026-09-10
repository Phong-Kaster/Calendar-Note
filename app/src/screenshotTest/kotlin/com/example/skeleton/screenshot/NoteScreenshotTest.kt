package com.example.skeleton.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.skeleton.ui.fragment.note.component.NoteDeleteConfirmContent

/*
 * --- What this file is for (simple story) ---
 *
 * DoD criterion 7 ends in a sentence no command can check: "the confirming control is visually
 * distinct from the cancelling one". The DoD marks it **(I)** — human inspection — because there is
 * no compiler on earth that can look at two buttons and say whether a tired person would tell them
 * apart.
 *
 * A committed picture is the closest thing to evidence that clause can have. A human agrees **once**
 * that the two controls read differently; from then on `validateDebugScreenshotTest` fails the day
 * somebody makes them look the same — by dropping the fill, by tinting Cancel red, by giving both
 * the same weight. That is the whole deal, and it is worth being precise about it: the image does
 * not *prove* the criterion, it *pins* a state a human approved.
 *
 * --- What this does NOT prove ---
 *
 * Not DoD criterion 2. A screenshot cannot tell `Color.White` from `colorScheme.onBackground`; both
 * render the same pixels. See the same note in `ThemeScreenshotTest.kt` and `HomeScreenshotTest.kt`.
 *
 * **And not that the confirmation is unconditional.** That the store is unreachable from a single
 * tap is a fact about `NoteViewModel`, not about how the sheet looks, and it is carried by two unit
 * tests in `NoteViewModelTest` — *asking to delete opens the confirmation and deletes nothing* and
 * *a delete that skipped the confirmation does nothing*. A picture could never say it.
 *
 * **This renders the sheet's content, not the sheet.** `NoteDeleteConfirmContent` is what
 * `NoteDeleteConfirmSheet` puts inside `CoreBottomSheet`, and it is rendered here directly — so if
 * the sheet stopped passing it through, or `NoteFragment` stopped showing the sheet at all, this
 * case would still pass. The reason is not laziness: a `ModalBottomSheet` renders into its own
 * window, and what layoutlib does with that in a host-side screenshot is not something this project
 * has established. Pinning the part that certainly renders, and saying plainly which part is not
 * pinned, beats a case that fails for reasons nobody can explain. The same gap exists one floor up
 * in `HomeScreenshotTest` (`HomeNoteList`, not `HomeLayout`) and is written down there too.
 *
 * **Nor the sheet's corners, drag behaviour or inset handling** — all three belong to
 * `CoreBottomSheet`, which is not in the picture. What *is* faithful is the colour underneath:
 * `ground = ScreenshotGround.Surface` puts these controls on the same `colorScheme.surface` the
 * real sheet paints, rather than on the screen background they never touch. That distinction was
 * missed on the first recording of this image and is the sort of thing that makes a reference
 * defend the wrong contrast while looking perfectly correct.
 *
 * @author Phong-Kaster
 */

/**
 * The question that stands between a tap and a note being gone for good.
 *
 * Two things are on trial in this image, and they are the two halves of "visually distinct":
 * **colour role** — the destructive control is filled with `error`, the way out is not filled at
 * all — and **emphasis** — a solid block with bold text against plain text at normal weight.
 * Either one alone is too easy to lose. Two buttons that differ only in colour are a coin toss on
 * a dark screen, and two that differ only in wording are how a person taps "Delete" while reading
 * "Cancel".
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Delete confirmation", widthDp = 360, heightDp = 240)
@Composable
private fun DeleteConfirmation() {
    ScreenshotScaffold(
        // `Surface`, not the default `Background`. This content lives inside `CoreBottomSheet`,
        // whose `containerColor` is `colorScheme.surface` — so the default would be testing these
        // two labels against a colour they never actually sit on. The two are close on this
        // palette, so the picture would have looked right and defended the wrong thing.
        ground = ScreenshotGround.Surface,
        content = {
            NoteDeleteConfirmContent()
        },
    )
}
