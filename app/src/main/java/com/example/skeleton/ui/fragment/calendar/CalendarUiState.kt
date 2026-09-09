package com.example.skeleton.ui.fragment.calendar

import com.example.skeleton.domain.model.CalendarMonth
import com.example.skeleton.domain.model.Note
import java.time.LocalDate

/**
 * UI state for the Calendar screen.
 *
 * @param currentMonth The month currently displayed in the grid.
 * @param today Real-world today, used to highlight the matching cell.
 * @param selectedDate The date whose notes are shown. Starts as [today] rather than null: the
 * add-note row is disabled without a selection, so an unselected first frame meant the screen
 * opened in a state where nothing could be done and nothing explained why.
 * @param notesOfSelectedDate Notes for [selectedDate], oldest first; empty when nothing selected.
 * @param datesWithNotes Every date in [currentMonth] (and its padding cells) that has at least
 * one note, used to render the "has notes" marker on each day cell.
 * @param editingNote The note currently being edited in [com.example.skeleton.ui.fragment.calendar.component.CalendarEditNoteDialog];
 * null when no edit dialog should be shown.
 * @param pendingDeleteNoteId The note awaiting delete confirmation; null when no confirm dialog
 * should be shown. Deletion is irreversible, so it is a two-step action.
 * @author Phong-Kaster
 */
data class CalendarUiState(
    val currentMonth: CalendarMonth = CalendarMonth.current(),
    val today: LocalDate = LocalDate.now(),
    val selectedDate: LocalDate? = null,
    val notesOfSelectedDate: List<Note> = emptyList(),
    val datesWithNotes: Set<LocalDate> = emptySet(),
    val editingNote: Note? = null,
    val pendingDeleteNoteId: Long? = null,
)
