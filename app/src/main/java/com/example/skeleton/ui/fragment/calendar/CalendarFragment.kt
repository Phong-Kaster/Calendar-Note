package com.example.skeleton.ui.fragment.calendar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.Note
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreConfirmDialog
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.calendar.component.CalendarAddNoteRow
import com.example.skeleton.ui.fragment.calendar.component.CalendarEditNoteDialog
import com.example.skeleton.ui.fragment.calendar.component.CalendarMonthGrid
import com.example.skeleton.ui.fragment.calendar.component.CalendarMonthHeader
import com.example.skeleton.ui.fragment.calendar.component.CalendarNoteList
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Calendar screen: shows the current month, lets the user step to the previous/next month,
 * select a date, and add/view/edit/delete notes on that date.
 *
 * @author Phong-Kaster
 */
class CalendarFragment : CoreFragment() {
    private val viewModel: CalendarViewModel by viewModel()

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        CalendarLayout(
            uiState = uiState,
            onPrevious = { viewModel.previousMonth() },
            onNext = { viewModel.nextMonth() },
            onDayClick = { date -> viewModel.selectDate(date) },
            onAddNote = { title -> viewModel.addNote(title) },
            onEditNote = { note -> viewModel.startEditingNote(note) },
            onDeleteNote = { id -> viewModel.requestDeleteNote(id) },
        )

        CalendarEditNoteDialog(
            note = uiState.editingNote,
            onConfirm = { title -> viewModel.updateNoteTitle(title) },
            onDismiss = { viewModel.cancelEditingNote() },
        )

        CoreConfirmDialog(
            visible = uiState.pendingDeleteNoteId != null,
            title = stringResource(R.string.delete_note_confirm_title),
            onConfirm = { viewModel.confirmDeleteNote() },
            onDismiss = { viewModel.cancelDeleteNote() },
        )
    }
}

/**
 * Calendar screen UI: month header with prev/next navigation, a weekday-labelled day grid, the
 * selected date's notes, and a row to add a new note to that date.
 *
 * The column scrolls. It has to: the note list grows with the selected date's notes, and while
 * the column was fixed-height, a busy day pushed its own notes *and* the add-note row off the
 * bottom of the screen — so the more a user relied on one date, the less they could do with it.
 */
@Composable
private fun CalendarLayout(
    uiState: CalendarUiState,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onDayClick: (LocalDate) -> Unit = {},
    onAddNote: (String) -> Unit = {},
    onEditNote: (Note) -> Unit = {},
    onDeleteNote: (Long) -> Unit = {},
) {
    val monthLabel = uiState.currentMonth.yearMonth.format(
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())
    )

    CoreLayout(
        modifier = Modifier,
        topBar = { CoreTopBar(title = stringResource(R.string.calendar)) },
        bottomBar = { CoreBottomBar() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
            ) {
                CalendarMonthHeader(
                    label = monthLabel,
                    onPrevious = onPrevious,
                    onNext = onNext,
                )
                CalendarMonthGrid(
                    days = uiState.currentMonth.buildGrid(),
                    today = uiState.today,
                    selectedDate = uiState.selectedDate,
                    datesWithNotes = uiState.datesWithNotes,
                    onDayClick = onDayClick,
                )
                CalendarNoteList(
                    notes = uiState.notesOfSelectedDate,
                    hasSelection = uiState.selectedDate != null,
                    onEdit = onEditNote,
                    onDelete = onDeleteNote,
                )
                CalendarAddNoteRow(
                    enabled = uiState.selectedDate != null,
                    onAddNote = onAddNote,
                )
            }
        }
    )
}

@Preview
@Composable
private fun CalendarLayoutPreview() {
    val today = LocalDate.of(2026, 9, 9)
    CalendarLayout(
        uiState = CalendarUiState(
            today = today,
            selectedDate = today,
            notesOfSelectedDate = listOf(
                Note(id = 1, epochDay = today.toEpochDay(), title = "Dentist appointment", createdAt = 0),
            ),
            datesWithNotes = setOf(today, LocalDate.of(2026, 9, 20)),
        ),
    )
}
