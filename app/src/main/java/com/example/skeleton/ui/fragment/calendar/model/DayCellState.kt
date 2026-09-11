package com.example.skeleton.ui.fragment.calendar.model

import java.time.LocalDate
import java.time.YearMonth

/**
 * What one square of the month grid is: a day already gone, today, a day still to come, or not a
 * day of this month at all.
 *
 * **This lives outside `@Composable` on purpose.** A rule written inside a composable can only be
 * checked by rendering it, and rendering is expensive here — so the question "is this square
 * tappable?" is answered by a plain function that an ordinary JVM test can ask a thousand times a
 * second. What the screen then *does* with the answer is the composable's job.
 *
 * @author Phong-Kaster
 */
enum class DayCellState {

    /** A day before today. The user can select it and write a note on it. */
    Past,

    /** Today. Selectable, and marked so the user can find it at a glance. */
    Today,

    /**
     * A day after today.
     *
     * Rendered visibly disabled and not tappable (DoD criterion 9). This is presentation only —
     * the reason a note cannot be *stored* on a future day lives in the notes store, below every
     * screen, so hiding the affordance is the polite half of a rule that is enforced elsewhere.
     */
    Future,

    /**
     * Padding: a square that belongs to no day of the displayed month.
     *
     * Every month has some — the columns before the 1st and after the last. Drawn as an empty
     * square, never tappable.
     */
    OutsideMonth;

    /**
     * Can the user tap this square?
     *
     * Today and the past, yes. Tomorrow and the padding, no. Asked as a property rather than
     * compared against two constants at each call site, so a fourth state added later cannot
     * quietly become tappable by default.
     */
    val isSelectable: Boolean
        get() = this == Past || this == Today

    companion object {

        /**
         * Works out what a square is, from the day it holds and the day it is.
         *
         * @param date the day in that square, or `null` when the square is padding.
         * @param today the real-world today, handed in rather than read from the clock so a test
         *   can decide when "now" is.
         * @param displayedMonth the month the grid is currently showing.
         * @return the state of that square.
         */
        fun of(
            date: LocalDate?,
            today: LocalDate,
            displayedMonth: YearMonth,
        ): DayCellState {
            // Padding square — there is no day here to be past or future.
            if (date == null) return OutsideMonth

            // A date from another month. The grid does not produce these today, because it pads
            // with nulls rather than with the neighbouring months' days. It is refused anyway:
            // showing the tail of August inside September's page is an ordinary thing to want
            // later, and this is the guard that stops such a day from arriving tappable and
            // indistinguishable from a day of the month on screen.
            if (YearMonth.from(date) != displayedMonth) return OutsideMonth

            return when {
                date.isEqual(today) -> Today
                date.isAfter(today) -> Future
                else -> Past
            }
        }
    }
}
