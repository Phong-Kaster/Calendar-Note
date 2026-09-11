package com.example.skeleton.ui.fragment.calendar

import com.example.skeleton.ui.fragment.calendar.model.DayCellState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

/**
 * DoD criterion 9 in the only place a machine can check it: the rule that decides whether a square
 * of the calendar is live.
 *
 * The screen half of that criterion — whether a disabled day *looks* disabled — is a picture, and
 * a person has to approve it. This half is arithmetic, and it must never depend on anybody
 * looking at anything. Which is why the rule was put in a plain function instead of inside the
 * cell that draws it.
 *
 * "Today" is a parameter throughout, never `LocalDate.now()`. A test that reads the real clock
 * passes for eleven months and then fails on a boundary nobody can reproduce.
 *
 * @author Phong-Kaster
 */
class DayCellStateTest {

    // ---------- The three live answers ----------

    @Test
    fun `the day after today is a day still to come`() {
        assertEquals(
            DayCellState.Future,
            DayCellState.of(date = TODAY.plusDays(1L), today = TODAY, displayedMonth = THIS_MONTH),
        )
    }

    @Test
    fun `today is today`() {
        assertEquals(
            DayCellState.Today,
            DayCellState.of(date = TODAY, today = TODAY, displayedMonth = THIS_MONTH),
        )
    }

    @Test
    fun `the day before today is already gone`() {
        assertEquals(
            DayCellState.Past,
            DayCellState.of(date = TODAY.minusDays(1L), today = TODAY, displayedMonth = THIS_MONTH),
        )
    }

    @Test
    fun `the boundary sits between today and tomorrow, not between yesterday and today`() {
        // The off-by-one that matters. Slide the comparison by one day in either direction and
        // either today stops being writable or tomorrow starts being — and the second of those
        // contradicts knowledge DOMAIN.md outright. Asserted as one triple so the shape of the
        // boundary is visible in a single failure.
        val around = listOf(
            TODAY.minusDays(1L) to DayCellState.Past,
            TODAY to DayCellState.Today,
            TODAY.plusDays(1L) to DayCellState.Future,
        )

        around.forEach { (date, expected) ->
            assertEquals(
                "$date",
                expected,
                DayCellState.of(date = date, today = TODAY, displayedMonth = THIS_MONTH),
            )
        }
    }

    // ---------- The edges of the month ----------

    @Test
    fun `the first and last days of the month are read by the calendar, not by the month edge`() {
        // A month edge is not a time boundary. The 1st of the displayed month is in the past
        // when today is the 10th, and the 30th is in the future — both while sitting on the
        // outermost squares of the page, where an implementation that special-cased the edges
        // would get them wrong.
        assertEquals(
            DayCellState.Past,
            DayCellState.of(
                date = LocalDate.of(2026, 9, 1),
                today = TODAY,
                displayedMonth = THIS_MONTH,
            ),
        )
        assertEquals(
            DayCellState.Future,
            DayCellState.of(
                date = LocalDate.of(2026, 9, 30),
                today = TODAY,
                displayedMonth = THIS_MONTH,
            ),
        )
    }

    @Test
    fun `today can be the last day of its month`() {
        // The month rolls over the same evening, so this is the state of the grid on the 30th of
        // September for a whole day. Nothing about it is special, which is the point.
        assertEquals(
            DayCellState.Today,
            DayCellState.of(
                date = LocalDate.of(2026, 9, 30),
                today = LocalDate.of(2026, 9, 30),
                displayedMonth = THIS_MONTH,
            ),
        )
    }

    @Test
    fun `the 29th of February in a leap year is an ordinary day`() {
        // Only reachable once every four years, which is exactly why it is written down: a date
        // that cannot be produced by hand is a date nobody notices going wrong.
        assertEquals(
            DayCellState.Past,
            DayCellState.of(
                date = LocalDate.of(2024, 2, 29),
                today = LocalDate.of(2024, 3, 1),
                displayedMonth = YearMonth.of(2024, 2),
            ),
        )
        assertEquals(
            DayCellState.Today,
            DayCellState.of(
                date = LocalDate.of(2024, 2, 29),
                today = LocalDate.of(2024, 2, 29),
                displayedMonth = YearMonth.of(2024, 2),
            ),
        )
    }

    // ---------- Squares that are not days of this month ----------

    @Test
    fun `a padding square belongs to no month`() {
        assertEquals(
            DayCellState.OutsideMonth,
            DayCellState.of(date = null, today = TODAY, displayedMonth = THIS_MONTH),
        )
    }

    @Test
    fun `a date from a neighbouring month is not a day of this page`() {
        // Checked in both directions and, crucially, for the same month a year out — a
        // comparison that looked only at the month number would let September 2027 through and
        // draw it as if it were this September.
        listOf(
            LocalDate.of(2026, 8, 31),
            LocalDate.of(2026, 10, 1),
            LocalDate.of(2027, 9, 10),
        ).forEach { date ->
            assertEquals(
                "$date",
                DayCellState.OutsideMonth,
                DayCellState.of(date = date, today = TODAY, displayedMonth = THIS_MONTH),
            )
        }
    }

    @Test
    fun `belonging to another month wins over being in the past`() {
        // Order of the guards, stated as a test. A past date from August is still not a square of
        // September's page, and answering `Past` would make it tappable.
        assertEquals(
            DayCellState.OutsideMonth,
            DayCellState.of(
                date = LocalDate.of(2026, 8, 1),
                today = TODAY,
                displayedMonth = THIS_MONTH,
            ),
        )
    }

    // ---------- What the screen actually asks ----------

    @Test
    fun `only today and the past can be tapped`() {
        assertTrue(DayCellState.Past.isSelectable)
        assertTrue(DayCellState.Today.isSelectable)
        assertFalse(DayCellState.Future.isSelectable)
        assertFalse(DayCellState.OutsideMonth.isSelectable)
    }

    @Test
    fun `every day of a month still to come is untappable`() {
        // The whole page, not one square: paging forward is how a user meets criterion 9 in
        // practice, and the answer has to hold for all thirty-one of them.
        val nextMonth = YearMonth.of(2026, 10)

        (1..nextMonth.lengthOfMonth()).forEach { dayOfMonth ->
            val state = DayCellState.of(
                date = nextMonth.atDay(dayOfMonth),
                today = TODAY,
                displayedMonth = nextMonth,
            )

            assertEquals("day $dayOfMonth", DayCellState.Future, state)
            assertFalse("day $dayOfMonth", state.isSelectable)
        }
    }

    private companion object {

        /** A fixed Thursday in the middle of a month, so there are days either side of it. */
        private val TODAY: LocalDate = LocalDate.of(2026, 9, 10)

        private val THIS_MONTH: YearMonth = YearMonth.of(2026, 9)
    }
}
