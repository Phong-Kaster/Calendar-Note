package com.example.skeleton.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.skeleton.R
import com.example.skeleton.domain.model.CalendarMonth
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.component.BottomBarElement
import com.example.skeleton.ui.fragment.calendar.component.CalendarDayCell
import com.example.skeleton.ui.fragment.calendar.component.CalendarDayNotes
import com.example.skeleton.ui.fragment.calendar.component.CalendarMonthGrid
import com.example.skeleton.ui.fragment.calendar.model.DayCellState
import java.time.LocalDate
import java.time.YearMonth

/*
 * --- What this file is for (simple story) ---
 *
 * A calendar square can be several things at once — today, and picked, and holding notes — and
 * every one of those is drawn as a colour on top of another colour. That is a kind of mistake no
 * unit test can reach: a test can prove a date is in `datesWithNotes`, and prove nothing at all
 * about whether the dot for it can be seen.
 *
 * It is not a hypothetical. A prior attempt at this screen shipped three defects here with a
 * green unit suite behind all of them:
 *
 *   1. a has-notes dot painted in `primary` on top of a `primary`-filled today cell — invisible,
 *      on exactly the day a user looks at first;
 *   2. a selection that was computed and never passed down to the grid, so picking a day did
 *      nothing visible;
 *   3. an unexplained blank gap where a selected day's empty note list should have been — pinned
 *      by `DayNotesEmptyDay` below.
 *
 * So the cases below are the **combinations**, not the individual states. The individual states
 * were never the broken part. A person approves each picture once; after that
 * `validateDebugScreenshotTest` fails the day one of them stops being true.
 *
 * --- What these images do NOT prove ---
 *
 * Not DoD criterion 2. A screenshot cannot tell `Color.White` from `colorScheme.onBackground` —
 * both render the same pixels. Colour-comes-from-the-theme is checked by a reader on this
 * project, or it is not checked. See the same note in `ThemeScreenshotTest.kt`.
 *
 * They also do not prove that a *tap* on a disabled square is refused. They show that it is drawn
 * as switched off; that it accepts nothing is `DayCellStateTest` plus the diff, where the cell's
 * `clickable` carries `enabled = state.isSelectable` and there is no other tap handler.
 *
 * @author Phong-Kaster
 */

/** Fixed, so nothing here depends on when the suite runs. 10 September 2026 is a Thursday. */
private val REFERENCE_TODAY: LocalDate = LocalDate.of(2026, 9, 10)
private val REFERENCE_MONTH: YearMonth = YearMonth.of(2026, 9)

/**
 * Defect 1, pinned: the dot has to survive being drawn on today's filled square.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Day cell - today with notes", widthDp = 72, heightDp = 72)
@Composable
private fun DayCellTodayWithNotes() {
    ScreenshotScaffold(
        content = {
            CalendarDayCell(
                date = REFERENCE_TODAY,
                state = DayCellState.Today,
                hasNotes = true,
            )
        },
    )
}

/**
 * The worst overlap in the whole screen, and the one the app opens on: today, picked, and holding
 * notes, all at once.
 *
 * Three markers in two colours on one square. The earlier version of this component chose between
 * the fill and the ring with a single `when`, so today simply won and the ring was never drawn —
 * a day that was both looked exactly like a day that was only today. Here the ring is outside the
 * fill with a gap, and this picture is what says so.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Day cell - today, picked, with notes", widthDp = 72, heightDp = 72)
@Composable
private fun DayCellTodaySelectedWithNotes() {
    ScreenshotScaffold(
        content = {
            CalendarDayCell(
                date = REFERENCE_TODAY,
                state = DayCellState.Today,
                isSelected = true,
                hasNotes = true,
            )
        },
    )
}

/**
 * Defect 2, pinned: a picked day that is not today must still look picked.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Day cell - picked with notes", widthDp = 72, heightDp = 72)
@Composable
private fun DayCellSelectedWithNotes() {
    ScreenshotScaffold(
        content = {
            CalendarDayCell(
                date = LocalDate.of(2026, 9, 3),
                state = DayCellState.Past,
                isSelected = true,
                hasNotes = true,
            )
        },
    )
}

/**
 * The ordinary case the other three are compared against: a past day with notes on it, neither
 * today nor picked.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Day cell - plain with notes", widthDp = 72, heightDp = 72)
@Composable
private fun DayCellPlainWithNotes() {
    ScreenshotScaffold(
        content = {
            CalendarDayCell(
                date = LocalDate.of(2026, 9, 3),
                state = DayCellState.Past,
                hasNotes = true,
            )
        },
    )
}

/**
 * DoD criterion 9's perceptual half: is a day the user cannot tap *legibly* switched off?
 *
 * The two failures this picture is between are opposite. Too bright and it is indistinguishable
 * from a live day, so the user keeps tapping it. Too dim — Material's usual 38% measures 2.14:1
 * on this black ground — and the number is not quieter, it is *gone*, and the month looks like it
 * failed to render. Only a person can say which side of that line the image is on, which is why
 * it is here to be approved rather than asserted.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Day cell - a day still to come", widthDp = 72, heightDp = 72)
@Composable
private fun DayCellFuture() {
    ScreenshotScaffold(
        content = {
            CalendarDayCell(
                date = LocalDate.of(2026, 9, 30),
                state = DayCellState.Future,
            )
        },
    )
}

/**
 * The combination the component argues for and nothing was covering: a day still to come that
 * already has a note on it.
 *
 * It is reachable, narrowly. The notes store refuses to date a note after today, but if the clock
 * turns while this screen is open, the grid is still drawn against the old `today` for a moment —
 * so a note written in that window lands on a square the grid is calling `Future`. The dot is
 * dimmed to the same weight as the number so the square does not say "switched off" and "here is
 * something" at two different volumes.
 *
 * Worth a picture precisely because it is the case nobody will reproduce by hand: the dimmed dot
 * is `primary` at 60% on black, 3.16:1, and "is that still a dot or is that a smudge" is not a
 * question arithmetic answers.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Day cell - a day still to come, with notes", widthDp = 72, heightDp = 72)
@Composable
private fun DayCellFutureWithNotes() {
    ScreenshotScaffold(
        content = {
            CalendarDayCell(
                date = LocalDate.of(2026, 9, 30),
                state = DayCellState.Future,
                hasNotes = true,
            )
        },
    )
}

/**
 * What adding a third tab did to the bottom bar's label, in the language where it shows.
 *
 * Adding the Calendar tab halved every slot: `(360 − 24 padding − 72 for the action button) / 4`
 * weighted slots is about **66dp** on a 360dp screen, and less on a 320dp one. Only the selected
 * tab draws its label, so this is rendered with `enable = true` and at exactly that width.
 *
 * The preview is 90 × 94, not 66 × 70: `ScreenshotScaffold` insets its content by 12dp on every
 * side, so the numbers here are the real slot plus that inset. Both matter, and the height caught
 * it — at 70 the tab had 46dp to lay out a 24dp icon, its spacing and a 14sp line, so the label
 * was measured to nothing and the first recorded image showed an icon and no text at all. A
 * reference that silently drops the thing it was taken for is worse than no reference.
 *
 * `locale = "de"` because English hides the problem. "Setting" fits; "Einstellungen" does not, and
 * the German strings are shipped — lint rates a missing one an error in this project. What the
 * picture is for is the question arithmetic cannot settle: whether the ellipsised remainder is
 * still enough for a person to know which tab they are on.
 *
 * This is recorded as a **known defect** with an open entry in `knowledge/ISSUES.md`, not as
 * approved behaviour. The reference exists so the defect is visible and so a later fix has
 * something to be compared against — do not read a passing validation here as the issue being
 * resolved.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Bottom bar tab - longest German label", widthDp = 90, heightDp = 94, locale = "de")
@Composable
private fun BottomBarTabGermanLabel() {
    ScreenshotScaffold(
        content = {
            BottomBarElement(
                enable = true,
                drawableId = R.drawable.ic_bottom_settings,
                stringId = R.string.setting,
                onClick = {},
            )
        },
    )
}

/**
 * The whole page, which is the only case that can catch the two grid-level mistakes.
 *
 * **The weekday header against the columns.** The labels are Sunday-first and so is the grid's
 * leading-blank arithmetic; if either drifts, every date sits under the wrong weekday while
 * looking perfectly ordinary. Count from this image: 1 September 2026 is a Tuesday, so the first
 * row starts with two empty squares.
 *
 * **The line between the past and the future.** Today is the 10th, so the 11th onward are
 * dimmed — the boundary is visible as a change of weight partway through the second full week,
 * not as an edge of the month.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Month grid", widthDp = 360, heightDp = 400)
@Composable
private fun MonthGrid() {
    ScreenshotScaffold(
        content = {
            CalendarMonthGrid(
                days = CalendarMonth(yearMonth = REFERENCE_MONTH).buildGrid(),
                today = REFERENCE_TODAY,
                displayedMonth = REFERENCE_MONTH,
                selectedDate = LocalDate.of(2026, 9, 3),
                datesWithNotes = setOf(REFERENCE_TODAY, LocalDate.of(2026, 9, 3)),
            )
        },
    )
}

/**
 * **Defect 3, pinned: a picked day with nothing on it used to render an unexplained blank gap.**
 *
 * This is the one case in this file that exists for something a unit test cannot even be written
 * against. "Is there a message here, and does it read as *nothing yet* rather than as *something
 * failed*?" is a question about what a person sees. The prior attempt at this screen drew
 * nothing, and nothing is not neutral: the grid stays put, the square the user tapped lights up,
 * and then there is a gap — which reads as a list that did not load, so they tap the day again
 * and get the same gap.
 *
 * The day label is passed in as a **fixed string** rather than formatted from a `LocalDate`.
 * That is what keeps this image portable: `DateTimeFormatter.ofLocalizedDate(...)` follows the
 * app's configured locale, and a reference that carries a formatted date fails on a machine set
 * to another language for a reason that has nothing to do with the app. `CalendarDayNotes` takes
 * the label already written out precisely so this case can pin one.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Day notes - a day with nothing on it", widthDp = 360, heightDp = 180)
@Composable
private fun DayNotesEmptyDay() {
    ScreenshotScaffold(
        content = {
            CalendarDayNotes(
                dayLabel = "Thursday, 3 September 2026",
                notes = emptyList(),
                onOpenNote = {},
            )
        },
    )
}

/**
 * The longest copy the shipped locales can put in this section, on the narrowest phone.
 *
 * German writes "Donnerstag, 24. September 2026" where English writes "Thursday, 24 September
 * 2026", and "An diesem Tag wurde noch nichts geschrieben" where English writes "Nothing
 * written on this day yet". `values-de/` ships — lint rates a missing German string an error
 * here — so both are real strings on a real screen.
 *
 * **What this image actually settles, which is not what it was recorded to settle.** It was
 * added expecting the heading to overflow; it does not. At 288dp — a 320dp screen's content
 * width, the narrow end of what this app supports — the German date fits on one line with room
 * to spare, and it is the *message* that wraps, to two centred lines. So the finding that
 * prompted this case was wrong about the arithmetic, and the picture is what says so. The
 * heading's `maxLines = 2` stays as insurance for a longer locale or a large system font scale,
 * not as a fix for anything observed here — **do not read this image as evidence that a wrap
 * was needed.** `widthDp = 312` is that 288 plus `ScreenshotScaffold`'s 12dp inset each side.
 *
 * **The height is load-bearing, for a reason this project has been bitten by before.** A
 * preview too short for its content records the content *absent* rather than clipped. The first
 * attempt here was 120dp tall and came back as a heading above empty black, having silently
 * dropped the very message the case is now most useful for. 200dp fits the whole section. The
 * 120dp rendering is orphaned on disk rather than overwritten, because the filename hashes the
 * preview's parameters — see `knowledge/PROJECT.md`.
 *
 * The label is a fixed string rather than a formatted `LocalDate`, like the two cases around
 * it: a formatter-produced date would bake the rendering machine's locale into the picture. It
 * is the German *words* being measured here, not German date formatting.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Day notes - longest German heading", widthDp = 312, heightDp = 200, locale = "de")
@Composable
private fun DayNotesGermanHeading() {
    ScreenshotScaffold(
        content = {
            CalendarDayNotes(
                dayLabel = "Donnerstag, 24. September 2026",
                notes = emptyList(),
                onOpenNote = {},
            )
        },
    )
}

/**
 * The same section with notes in it, which is what the empty case above is judged against.
 *
 * Two rows rather than one, and the second has no title, because both are things a person has to
 * look at to approve: that the cards are far enough apart to read as separate notes, and that a
 * note whose heading falls back to its first line still looks like a note rather than like a
 * rendering fault.
 *
 * **Neither row prints its date**, and that is the thing to check here. Every note in this list
 * is on the day named in the heading above it, so a date on each card would repeat that heading
 * once per note. If dates ever reappear here, `showDate` stopped being passed.
 *
 * @author Phong-Kaster
 */
@PreviewTest
@Preview(name = "Day notes - a day with notes on it", widthDp = 360, heightDp = 320)
@Composable
private fun DayNotesWithNotes() {
    ScreenshotScaffold(
        content = {
            CalendarDayNotes(
                dayLabel = "Thursday, 10 September 2026",
                notes = listOf(
                    Note(
                        id = 1L,
                        date = REFERENCE_TODAY,
                        title = "Groceries",
                        content = "Coffee, oat milk, the good bread from the corner shop.",
                        createdAt = 1_773_000_000_000L,
                        updatedAt = 1_773_000_000_000L,
                    ),
                    Note(
                        id = 2L,
                        date = REFERENCE_TODAY,
                        title = "",
                        content = "No title on this one, so the first line becomes the heading.",
                        createdAt = 1_772_900_000_000L,
                        updatedAt = 1_772_900_000_000L,
                    ),
                ),
                onOpenNote = {},
            )
        },
    )
}
