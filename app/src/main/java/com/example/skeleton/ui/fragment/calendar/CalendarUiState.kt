package com.example.skeleton.ui.fragment.calendar

import com.example.skeleton.domain.model.CalendarMonth
import com.example.skeleton.domain.model.Note
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
 * @param loadedDay the day [loadedDayNotes] were read for. **Not the same thing as
 *   [selectedDate]**, and the gap between them is the point — see [notesForSelectedDay].
 * @param loadedDayNotes whatever the store last handed over for [loadedDay], most recently
 *   touched first. Read [notesForSelectedDay] instead of this: these two fields are the raw
 *   answer, and only the pair of them together says which day it is an answer about.
 * @author Phong-Kaster
 */
data class CalendarUiState(
    val today: LocalDate = LocalDate.now(),
    val displayedMonth: YearMonth = YearMonth.from(today),
    val selectedDate: LocalDate? = null,
    val datesWithNotes: Set<LocalDate> = emptySet(),
    val loadedDay: LocalDate? = null,
    val loadedDayNotes: List<Note> = emptyList(),
) {

    /**
     * The notes to draw under the grid: the ones belonging to [selectedDate], newest first.
     *
     * **Derived, and derived for a reason that is not tidiness.** Picking a day and reading that
     * day's notes are two separate events — the tap changes [selectedDate] at once, while the
     * store's answer for the new day arrives a moment later through a database query. Held as one
     * plain field, the state in between is *internally inconsistent*: the heading names the day
     * just tapped while the rows underneath are still the previous day's, which is the app
     * showing notes filed under a date they are not on.
     *
     * Comparing [loadedDay] with [selectedDate] makes that state unrepresentable rather than
     * merely unlikely. Whichever of the two writes lands first, the notes are shown only when
     * they are an answer about the day being displayed.
     *
     * The cost is honest and small: for the frame or two before the store answers, a day that
     * *does* have notes draws its empty message. Briefly saying "nothing yet" about a day that
     * has something is a smaller lie than confidently showing another day's notes, and it
     * corrects itself the moment the query returns.
     *
     * Empty when nothing is picked at all, which the section below the grid draws differently
     * again — [selectedDate] being null is what tells those two apart.
     */
    val notesForSelectedDay: List<Note> =
        if (loadedDay == selectedDate) loadedDayNotes else emptyList()

    /**
     * The squares of the grid, left to right and top to bottom; `null` is padding.
     *
     * Derived here rather than stored, so it can never disagree with [displayedMonth]. Rebuilding
     * it costs at most 42 list entries, which is nothing next to the class of bug where the month
     * label says October and the grid is still showing September.
     */
    val days: List<LocalDate?> = CalendarMonth(yearMonth = displayedMonth).buildGrid()
}
