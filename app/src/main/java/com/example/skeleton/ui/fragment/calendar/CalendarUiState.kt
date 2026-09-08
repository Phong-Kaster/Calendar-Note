package com.example.skeleton.ui.fragment.calendar

import com.example.skeleton.domain.model.CalendarMonth
import com.example.skeleton.domain.model.Note
import java.time.LocalDate

/**
 * UI state for the Calendar screen.
 *
 * @param currentMonth The month currently displayed in the grid.
 * @param today Real-world today, used to highlight the matching cell.
 * @param selectedDate The date the user tapped; null when nothing is selected yet.
 * @param notesOfSelectedDate Notes for [selectedDate], oldest first; empty when nothing selected.
 * @param datesWithNotes Every date in [currentMonth] (and its padding cells) that has at least
 * one note, used to render the "has notes" marker on each day cell.
 * @author Phong-Kaster
 */
data class CalendarUiState(
    val currentMonth: CalendarMonth = CalendarMonth.current(),
    val today: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate? = null,
    val notesOfSelectedDate: List<Note> = emptyList(),
    val datesWithNotes: Set<LocalDate> = emptySet(),
)
