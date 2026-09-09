package com.example.skeleton.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.skeleton.domain.model.CalendarMonth
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.fragment.calendar.component.CalendarDayCell
import com.example.skeleton.ui.fragment.calendar.component.CalendarMonthGrid
import com.example.skeleton.ui.fragment.calendar.component.CalendarMonthHeader
import com.example.skeleton.ui.fragment.calendar.component.CalendarNoteList
import java.time.LocalDate
import java.time.YearMonth

/**
 * Screenshot coverage for the Calendar screen's components.
 *
 * Every case here exists because it actually broke. The unit suite passed on all of them: it can
 * assert that a date is in `datesWithNotes`, but not that the marker for it is visible — which is
 * how a dot drawn in `primary` on top of a `primary`-filled circle shipped, invisible on exactly
 * the day a user looks at first. These are the criteria that were previously unexpressible.
 *
 * @author Phong-Kaster
 */

private val REFERENCE_DAY: LocalDate = LocalDate.of(2026, 9, 9)
private val REFERENCE_MONTH: YearMonth = YearMonth.of(2026, 9)

/**
 * The regression that motivated this whole suite: today's cell is filled with `primary`, so a
 * has-notes dot must contrast against that fill, not repeat it.
 */
@PreviewTest
@Preview(name = "Day cell - today with notes", widthDp = 80, heightDp = 80)
@Composable
private fun DayCellTodayWithNotes() {
    ScreenshotScaffold {
        CalendarDayCell(date = REFERENCE_DAY, isToday = true, hasNotes = true)
    }
}

@PreviewTest
@Preview(name = "Day cell - today", widthDp = 80, heightDp = 80)
@Composable
private fun DayCellToday() {
    ScreenshotScaffold {
        CalendarDayCell(date = REFERENCE_DAY, isToday = true)
    }
}

@PreviewTest
@Preview(name = "Day cell - plain with notes", widthDp = 80, heightDp = 80)
@Composable
private fun DayCellPlainWithNotes() {
    ScreenshotScaffold {
        CalendarDayCell(date = REFERENCE_DAY, isToday = false, hasNotes = true)
    }
}

/** Selection used to be invisible: `selectedDate` never reached the grid. */
@PreviewTest
@Preview(name = "Day cell - selected with notes", widthDp = 80, heightDp = 80)
@Composable
private fun DayCellSelectedWithNotes() {
    ScreenshotScaffold {
        CalendarDayCell(date = REFERENCE_DAY, isToday = false, isSelected = true, hasNotes = true)
    }
}

/** A day can be today *and* selected; the fill and the ring must both survive. */
@PreviewTest
@Preview(name = "Day cell - today and selected", widthDp = 80, heightDp = 80)
@Composable
private fun DayCellTodayAndSelected() {
    ScreenshotScaffold {
        CalendarDayCell(date = REFERENCE_DAY, isToday = true, isSelected = true, hasNotes = true)
    }
}

/**
 * Pins the weekday header against the grid it labels. The grid is Sunday-first, and a header that
 * drifts out of step would keep looking authoritative while pointing at the wrong column — a
 * failure no unit test can see and a reader is unlikely to double-check by hand.
 */
@PreviewTest
@Preview(name = "Month grid", widthDp = 360, heightDp = 400)
@Composable
private fun MonthGrid() {
    ScreenshotScaffold {
        CalendarMonthGrid(
            days = CalendarMonth(REFERENCE_MONTH).buildGrid(),
            today = REFERENCE_DAY,
            selectedDate = LocalDate.of(2026, 9, 17),
            datesWithNotes = setOf(REFERENCE_DAY, LocalDate.of(2026, 9, 22)),
        )
    }
}

@PreviewTest
@Preview(name = "Month header", widthDp = 360, heightDp = 80)
@Composable
private fun MonthHeader() {
    ScreenshotScaffold {
        CalendarMonthHeader(label = "September 2026")
    }
}

@PreviewTest
@Preview(name = "Note list", widthDp = 360, heightDp = 200)
@Composable
private fun NoteList() {
    ScreenshotScaffold {
        CalendarNoteList(
            notes = listOf(
                Note(id = 1, epochDay = REFERENCE_DAY.toEpochDay(), title = "Dentist appointment", createdAt = 0),
                Note(id = 2, epochDay = REFERENCE_DAY.toEpochDay(), title = "Buy birthday gift", createdAt = 0),
            ),
        )
    }
}

/** A selected date with no notes used to render an unexplained blank gap. */
@PreviewTest
@Preview(name = "Note list - empty day", widthDp = 360, heightDp = 120)
@Composable
private fun NoteListEmptyDay() {
    ScreenshotScaffold {
        CalendarNoteList(notes = emptyList(), hasSelection = true)
    }
}
