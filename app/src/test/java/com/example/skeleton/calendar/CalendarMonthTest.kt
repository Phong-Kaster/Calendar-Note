package com.example.skeleton.calendar

import com.example.skeleton.domain.model.CalendarMonth
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.YearMonth

/**
 * Verifies [CalendarMonth] month arithmetic: previous/next navigation and month length,
 * including leap-year handling.
 *
 * @author Phong-Kaster
 */
class CalendarMonthTest {

    @Test
    fun `previous month steps back one month across year boundary`() {
        val calendarMonth = CalendarMonth(YearMonth.of(2025, 1))

        assertEquals(YearMonth.of(2024, 12), calendarMonth.previous().yearMonth)
    }

    @Test
    fun `next month steps forward one month across year boundary`() {
        val calendarMonth = CalendarMonth(YearMonth.of(2024, 12))

        assertEquals(YearMonth.of(2025, 1), calendarMonth.next().yearMonth)
    }

    @Test
    fun `leap year february has 29 days`() {
        val calendarMonth = CalendarMonth(YearMonth.of(2024, 2))

        assertEquals(29, calendarMonth.lengthOfMonth())
    }

    @Test
    fun `non-leap year february has 28 days`() {
        val calendarMonth = CalendarMonth(YearMonth.of(2023, 2))

        assertEquals(28, calendarMonth.lengthOfMonth())
    }
}
