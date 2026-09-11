package com.example.skeleton.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * Holds [CalendarMonth] to the arithmetic a calendar page depends on.
 *
 * Every failure this file is written against is silent. A grid with one blank too many draws a
 * whole month shifted by a column — every date under the wrong weekday, and nothing anywhere
 * complains. A month whose last week is short stretches its squares across the full width. These
 * are the mistakes that look fine in a screenshot until you count.
 *
 * The months below are chosen, not sampled. Each one is the smallest case that can catch a
 * specific slip: a Sunday start catches the `% 7`, a leap February catches a hardcoded 28, and a
 * month that ends exactly on a Saturday catches a trailing-blank formula that adds a spare week.
 *
 * @author Phong-Kaster
 */
class CalendarMonthTest {

    // ---------- Leading blanks: where the 1st lands ----------

    @Test
    fun `a month starting on a Tuesday begins with two blank squares`() {
        // 1 September 2026 is a Tuesday. Sunday-first, that is columns 0 and 1 empty.
        val grid = CalendarMonth(yearMonth = YearMonth.of(2026, 9)).buildGrid()

        assertEquals(null, grid[0])
        assertEquals(null, grid[1])
        assertEquals(LocalDate.of(2026, 9, 1), grid[2])
    }

    @Test
    fun `a month starting on a Sunday begins with no blank squares at all`() {
        // 1 February 2026 is a Sunday, and in java.time Sunday is 7 — so without the `% 7` this
        // month would open with a completely empty first week and every date one row too low.
        val grid = CalendarMonth(yearMonth = YearMonth.of(2026, 2)).buildGrid()

        assertEquals(LocalDate.of(2026, 2, 1), grid[0])
    }

    @Test
    fun `a month starting on a Saturday begins with six blank squares`() {
        // The far end of the same arithmetic: Saturday is 6, the widest legal offset. 1 August
        // 2026 is a Saturday.
        val grid = CalendarMonth(yearMonth = YearMonth.of(2026, 8)).buildGrid()

        assertEquals(6, grid.indexOf(LocalDate.of(2026, 8, 1)))
    }

    // ---------- Day count ----------

    @Test
    fun `february in a leap year holds twenty-nine days`() {
        val month = CalendarMonth(yearMonth = YearMonth.of(2024, 2))

        assertEquals(29, month.buildGrid().count { date -> date != null })
        assertTrue(month.buildGrid().contains(LocalDate.of(2024, 2, 29)))
    }

    @Test
    fun `february in an ordinary year holds twenty-eight days`() {
        val month = CalendarMonth(yearMonth = YearMonth.of(2026, 2))

        assertEquals(28, month.buildGrid().count { date -> date != null })
        // The last day by name, not just the count: 2026-02-29 cannot be written down at all, so
        // "it is not in the grid" has to be asked this way round.
        assertEquals(LocalDate.of(2026, 2, 28), month.buildGrid().filterNotNull().last())
    }

    @Test
    fun `every day of the month appears once, in order, with nothing missing`() {
        // Counting the days is not enough — a grid could hold 30 entries with the 14th twice and
        // the 15th nowhere. This checks the actual sequence.
        val days = CalendarMonth(yearMonth = YearMonth.of(2026, 9)).buildGrid().filterNotNull()

        assertEquals((1..30).map { day -> LocalDate.of(2026, 9, day) }, days)
    }

    // ---------- Whole weeks ----------

    @Test
    fun `the grid is always a whole number of weeks`() {
        // The grid chunks by seven and trusts every chunk to be full. A month that came back one
        // square short would render a final row of stretched cells.
        val months = listOf(
            YearMonth.of(2026, 2),  // 28 days starting on a Sunday: exactly four weeks
            YearMonth.of(2026, 8),  // six leading blanks, the widest offset
            YearMonth.of(2026, 9),
            YearMonth.of(2024, 2),  // leap February
            YearMonth.of(2026, 12),
        )

        months.forEach { yearMonth ->
            val size = CalendarMonth(yearMonth = yearMonth).buildGrid().size
            assertEquals("$yearMonth is not a whole number of weeks", 0, size % 7)
        }
    }

    @Test
    fun `a month that fits exactly is not given a spare empty week`() {
        // February 2026 starts on a Sunday and has 28 days, so it fills four weeks to the square.
        // Without the outer `% 7` the trailing-blank formula would add a fifth, entirely empty.
        assertEquals(28, CalendarMonth(yearMonth = YearMonth.of(2026, 2)).buildGrid().size)
    }

    @Test
    fun `the last square of the grid finishes the final week`() {
        // September 2026: two blanks plus 30 days is 32, so the grid runs to 35 and the last
        // three squares are padding.
        val grid = CalendarMonth(yearMonth = YearMonth.of(2026, 9)).buildGrid()

        assertEquals(35, grid.size)
        assertEquals(LocalDate.of(2026, 9, 30), grid[31])
        assertEquals(null, grid[32])
        assertEquals(null, grid[34])
    }

    // ---------- The week start, which the header also reads ----------

    @Test
    fun `the columns are named in the order the grid fills them`() {
        // The header row draws `weekdaysInGridOrder()` and the squares are placed by
        // `buildGrid()`. This is the test that says the two agree.
        val order = CalendarMonth.weekdaysInGridOrder()

        // Shape checks, not behaviour checks — say so rather than let them pad the count. Seven
        // entries, starting at WEEK_START, all distinct are re-statements of how
        // `weekdaysInGridOrder()` is written, and no mutation of the grid arithmetic can make
        // them red. They are kept only to fail loudly if the function is ever rewritten as a
        // hand-typed list, which is the one change that could silently drop or repeat a day.
        assertEquals(7, order.size)
        assertEquals(CalendarMonth.WEEK_START, order.first())
        assertEquals(7, order.toSet().size)

        // *This* is the assertion the test is named for. 1 September 2026 is a Tuesday, and it
        // lands at index 2 — where the label list also says Tuesday. Change the week start on
        // one side only and this is what fails.
        val grid = CalendarMonth(yearMonth = YearMonth.of(2026, 9)).buildGrid()
        val indexOfFirstDay = grid.indexOf(LocalDate.of(2026, 9, 1))

        assertEquals(DayOfWeek.TUESDAY, order[indexOfFirstDay])
    }

    @Test
    fun `every square sits under the label for its own weekday`() {
        // The whole month, not one day. An offset that is right for the 1st is right for all of
        // them, but only if the arithmetic is an offset rather than a special case.
        val order = CalendarMonth.weekdaysInGridOrder()
        val grid = CalendarMonth(yearMonth = YearMonth.of(2026, 2)).buildGrid()

        grid.forEachIndexed { index, date ->
            if (date == null) return@forEachIndexed
            assertEquals("$date", order[index % 7], date.dayOfWeek)
        }
    }
}
