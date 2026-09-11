package com.example.skeleton.domain.model

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth

/**
 * One month, arranged the way a calendar page shows it.
 *
 * Think of a paper wall calendar. It has seven columns, the first day of the month does not
 * usually land in the first column, and the last week usually runs out of days. This class does
 * that arithmetic — nothing else. It holds no colours, no selection and no notes, because those
 * belong to the screen and this has to stay something a plain JVM test can check without rendering
 * anything.
 *
 * **Weeks start on Sunday**, and that is a decision rather than a fact: half the world starts on
 * Monday, and `java.time` will happily tell you either depending on the device's locale. A
 * locale-dependent grid draws differently on two phones and cannot be pinned by a reference
 * image, so the choice is made once, here, as [WEEK_START].
 *
 * Two things have to agree about that choice: this class's leading-blank arithmetic, and the
 * weekday labels above the columns. Both read [WEEK_START] rather than each hardcoding Sunday
 * for themselves — a header that has drifted out of step with the grid keeps looking
 * authoritative while pointing at the wrong column, and nothing but a human eye would catch it.
 *
 * @param yearMonth the month this instance represents.
 * @author Phong-Kaster
 */
data class CalendarMonth(val yearMonth: YearMonth) {

    /**
     * Every cell of the seven-column grid, read left to right and top to bottom.
     *
     * Three parts, in order:
     *
     * 1. **Leading blanks** — `null` for each column before the 1st of the month, so day 1 lands
     *    in its real column instead of always in the first one.
     * 2. **The days themselves**, one [LocalDate] each.
     * 3. **Trailing blanks** — enough `null`s to finish the last week.
     *
     * The size is therefore always a multiple of seven, which is what lets the grid chunk the list
     * into rows of seven and trust that every row is full.
     *
     * @return the grid cells; `null` means "this square is padding, not a day".
     */
    fun buildGrid(): List<LocalDate?> {
        val firstDayOfMonth = yearMonth.atDay(1)

        // How many columns the 1st sits to the right of the week's first column. In java.time
        // Monday is 1 and Sunday is 7, so the difference can come out negative — adding a whole
        // week before taking the remainder is what keeps it in 0..6 for any [WEEK_START].
        val leadingBlankCount =
            (firstDayOfMonth.dayOfWeek.value - WEEK_START.value + DAYS_IN_WEEK) % DAYS_IN_WEEK

        val cells = mutableListOf<LocalDate?>()
        repeat(times = leadingBlankCount) { cells.add(null) }
        for (dayOfMonth in 1..yearMonth.lengthOfMonth()) {
            cells.add(yearMonth.atDay(dayOfMonth))
        }

        // The outer `% DAYS_IN_WEEK` is what stops a month that already ends on a Saturday from
        // being given a whole extra blank week.
        val trailingBlankCount = (DAYS_IN_WEEK - cells.size % DAYS_IN_WEEK) % DAYS_IN_WEEK
        repeat(times = trailingBlankCount) { cells.add(null) }

        return cells
    }

    companion object {

        /** Seven columns, one per day of the week. */
        const val DAYS_IN_WEEK = 7

        /**
         * The day the grid's first column is.
         *
         * Read by [buildGrid] and by the weekday header that labels the columns, so that
         * changing it moves both together. Changing it also changes what every committed
         * reference image of the grid shows, which is the point: a human sees the new layout
         * before it ships.
         */
        val WEEK_START: DayOfWeek = DayOfWeek.SUNDAY

        /**
         * The seven days of the week in the order the grid draws them, starting at [WEEK_START].
         *
         * @return Sunday through Saturday, for the default week start.
         */
        fun weekdaysInGridOrder(): List<DayOfWeek> =
            (0 until DAYS_IN_WEEK).map { column -> WEEK_START.plus(column.toLong()) }
    }
}
