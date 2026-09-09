package com.example.skeleton.ui.fragment.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Holds the currently displayed month, the selected date, and that date's notes.
 *
 * @author Phong-Kaster
 */
class CalendarViewModel(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val TAG = "CalendarViewModel"

    // Today starts selected, so the screen opens on a usable state instead of one where the
    // add-note row is disabled with nothing on screen saying why.
    private val _uiState = MutableStateFlow(
        CalendarUiState().let { initial -> initial.copy(selectedDate = initial.today) }
    )
    val uiState = _uiState.asStateFlow()

    init {
        collectNotesForSelectedDate()
        collectDatesWithNotesForVisibleMonth()
    }

    /** Moves the displayed month back by one. */
    fun previousMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.previous())
    }

    /** Moves the displayed month forward by one. */
    fun nextMonth() {
        _uiState.value = _uiState.value.copy(currentMonth = _uiState.value.currentMonth.next())
    }

    /** Selects [date] so its notes show below the grid. */
    fun selectDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(selectedDate = date)
    }

    /** Adds a note titled [title] to the currently selected date; no-op when nothing is selected. */
    fun addNote(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val date = _uiState.value.selectedDate ?: return@launch
            noteRepository.addNote(date, title)
        }
    }

    /** Opens the edit dialog for [note]. */
    fun startEditingNote(note: Note) {
        _uiState.value = _uiState.value.copy(editingNote = note)
    }

    /** Closes the edit dialog without saving. */
    fun cancelEditingNote() {
        _uiState.value = _uiState.value.copy(editingNote = null)
    }

    /**
     * Saves [title] as the new title of [CalendarUiState.editingNote], then closes the edit
     * dialog; no-op when nothing is being edited.
     */
    fun updateNoteTitle(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val note = _uiState.value.editingNote ?: return@launch
            noteRepository.update(note.id, title)
            _uiState.value = _uiState.value.copy(editingNote = null)
        }
    }

    /** Asks for confirmation before deleting the note identified by [id]. */
    fun requestDeleteNote(id: Long) {
        _uiState.value = _uiState.value.copy(pendingDeleteNoteId = id)
    }

    /** Dismisses the delete confirmation without deleting anything. */
    fun cancelDeleteNote() {
        _uiState.value = _uiState.value.copy(pendingDeleteNoteId = null)
    }

    /**
     * Deletes the note awaiting confirmation and closes the dialog; no-op when nothing is
     * pending. The pending id is cleared first so a second confirm tap cannot re-issue the
     * delete against an id that is already gone.
     */
    fun confirmDeleteNote() {
        val id = _uiState.value.pendingDeleteNoteId ?: return
        _uiState.value = _uiState.value.copy(pendingDeleteNoteId = null)
        viewModelScope.launch(Dispatchers.IO) {
            noteRepository.delete(id)
        }
    }

    /** Re-collects notes for [CalendarUiState.selectedDate] whenever the selection changes. */
    private fun collectNotesForSelectedDate() {
        _uiState.map { it.selectedDate }
            .distinctUntilChanged()
            .flatMapLatest { date ->
                if (date == null) flowOf(emptyList()) else noteRepository.observeByDate(date)
            }
            .onEach { notes -> _uiState.value = _uiState.value.copy(notesOfSelectedDate = notes) }
            .launchIn(viewModelScope)
    }

    /** Re-collects the "has notes" marker set whenever the visible month changes. */
    private fun collectDatesWithNotesForVisibleMonth() {
        _uiState.map { it.currentMonth }
            .distinctUntilChanged()
            .flatMapLatest { month ->
                val firstDayOfMonth = month.yearMonth.atDay(1)
                val lastDayOfMonth = month.yearMonth.atEndOfMonth()
                noteRepository.observeDatesWithNotesBetween(firstDayOfMonth, lastDayOfMonth)
            }
            .onEach { dates -> _uiState.value = _uiState.value.copy(datesWithNotes = dates) }
            .launchIn(viewModelScope)
    }
}
