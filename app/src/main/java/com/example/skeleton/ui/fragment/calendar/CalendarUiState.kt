package com.example.skeleton.ui.fragment.calendar

import com.example.skeleton.domain.model.CalendarMonth
import java.time.LocalDate
import java.time.YearMonth

/**
 * Everything the Calendar screen draws.
 *
 * @param today the real-world today. Held as state rather than read from the clock inside the
 *   layout, so that what the screen calls "today" and what the ViewModel checks a tap against are
 *   the same day, always — two separate `LocalDate.now()` calls either side of midnight are not.
 * @param displayedMonth the month currently on screen. Defaults to the month containing [today] —
 *   derived from that field rather than from a second `YearMonth.now()`, so the two cannot be
 *   read either side of midnight and disagree — and moves when the user taps the arrows.
 * @param selectedDate the day whose notes the screen is about to show (T-007), or null when
 *   nothing is picked. **Kept at or before [today] by the ViewModel, not by construction** —
 *   `selectDate` refuses a later day, and `refreshToday` drops a selection the clock has
 *   overtaken. Both are enforcement, not impossibility: nothing about this type prevents a
 *   future date being written here, so a new writer has to hold the rule up itself. The
 *   failure it is worth holding up for is a square drawn with a full-strength selection ring
 *   while being dimmed and refusing taps — a control that looks chosen and does nothing.
 * @param datesWithNotes every day that already has at least one note on it, so the grid can mark
 *   it. A `Set` because the only question ever asked of it is "is this day in here?".
 * @author Phong-Kaster
 */
data class CalendarUiState(
    val today: LocalDate = LocalDate.now(),
    val displayedMonth: YearMonth = YearMonth.from(today),
    val selectedDate: LocalDate? = null,
    val datesWithNotes: Set<LocalDate> = emptySet(),
) {

    /**
     * The squares of the grid, left to right and top to bottom; `null` is padding.
     *
     * Derived here rather than stored, so it can never disagree with [displayedMonth]. Rebuilding
     * it costs at most 42 list entries, which is nothing next to the class of bug where the month
     * label says October and the grid is still showing September.
     */
    val days: List<LocalDate?> = CalendarMonth(yearMonth = displayedMonth).buildGrid()
}
