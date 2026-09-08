package com.example.skeleton.domain.model

import java.time.LocalDate
import java.time.YearMonth

/**
 * Wraps [YearMonth] with the exact helpers the Calendar screen needs: moving to the
 * previous/next month and building a 7-column day grid.
 *
 * Grid start choice: weeks start on **Sunday** (index 0 = Sunday ... index 6 = Saturday).
 *
 * @param yearMonth the month this instance represents.
 * @author Phong-Kaster
 */
data class CalendarMonth(val yearMonth: YearMonth) {

    /** Returns the previous month. */
    fun previous(): CalendarMonth = CalendarMonth(yearMonth.minusMonths(1))

    /** Returns the next month. */
    fun next(): CalendarMonth = CalendarMonth(yearMonth.plusMonths(1))

    /** Total number of days in this month (28-31), leap years handled by [YearMonth]. */
    fun lengthOfMonth(): Int = yearMonth.lengthOfMonth()

    /**
     * Builds a flat list of cells for a 7-column grid: leading `null`s so day 1 lands
     * in the correct Sunday-first column, then one [LocalDate] per day of the month,
     * then trailing `null`s so the total size is a multiple of 7.
     */
    fun buildGrid(): List<LocalDate?> {
        val firstDayOfMonth = yearMonth.atDay(1)

        // DayOfWeek.SUNDAY = 7 in java.time; convert so Sunday = 0 leading blanks.
        val leadingBlankCount = firstDayOfMonth.dayOfWeek.value % DAYS_IN_WEEK

        val days = mutableListOf<LocalDate?>()
        repeat(leadingBlankCount) { days.add(null) }
        for (day in 1..lengthOfMonth()) {
            days.add(yearMonth.atDay(day))
        }

        val trailingBlankCount = (DAYS_IN_WEEK - days.size % DAYS_IN_WEEK) % DAYS_IN_WEEK
        repeat(trailingBlankCount) { days.add(null) }

        return days
    }

    companion object {
        private const val DAYS_IN_WEEK = 7

        /** Returns [CalendarMonth] for today. */
        fun current(): CalendarMonth = CalendarMonth(YearMonth.now())
    }
}
