package com.example.skeleton.ui.fragment.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Drives the Note screen: holds what the user is typing and hands it to the store on save.
 *
 * This ViewModel has no `init` block, and that is deliberate. Every other screen in this app knows
 * what to load the moment it exists; this one does not — it has to be *told* which note to open,
 * because that arrives as a navigation argument. So the Fragment calls [openNote] once, and until
 * it does the screen shows an empty note for today.
 *
 * @param noteRepository the notes store, injected by interface so a test can hand over a fake.
 * @author Phong-Kaster
 */
class NoteViewModel(
    private val noteRepository: NoteRepository,
) : ViewModel() {

    private val TAG = "NoteViewModel"

    private val _uiState = MutableStateFlow(NoteUiState())
    val uiState: StateFlow<NoteUiState> = _uiState.asStateFlow()

    /**
     * The note as it was when the screen opened — a fresh draft, or the stored note being edited.
     *
     * It is kept whole rather than as two loose fields because its `id` and its `createdAt` both
     * have to survive back into the store untouched at save time, and a note that loses either one
     * turns into a *second* note the next time it is saved.
     */
    private var openedNote: Note = Note.draft(date = LocalDate.now())

    /**
     * Guards [openNote] against running twice.
     *
     * Not paranoia: a Fragment is recreated on every rotation while its ViewModel survives, so a
     * second call is the normal course of events — and it would overwrite whatever the user had
     * typed with what the database still holds. Opening a note happens once per ViewModel.
     */
    private var opened = false

    /**
     * Guards [save] against a double tap inserting the same new note twice.
     *
     * It is **never** lowered again after a save succeeds, and that asymmetry is the whole point.
     * A successful save is followed by the screen leaving — but leaving is animated, and the
     * composition stays alive and accepting touches for the length of that animation. A guard that
     * reopened the moment the database returned would be shut for about five milliseconds and open
     * for the next few hundred, which is exactly the window a second tap lands in. The second tap
     * would then insert a *second* row, because a note that has just been created still carries
     * [Note.UNSAVED_ID] here — this ViewModel never learns the id the store handed out.
     */
    private var saving = false

    /**
     * Loads the note this screen was opened for.
     *
     * @param noteId the row id to edit, or [Note.UNSAVED_ID] to start a new note.
     * @param date the day a new note belongs to. Ignored when an existing note is loaded — that
     *   note already knows its own day, and taking the argument's word for it instead would move
     *   an old note onto today the first time somebody re-opened it.
     */
    fun openNote(noteId: Long, date: LocalDate) {
        if (opened) return
        opened = true

        viewModelScope.launch {
            val stored = if (noteId == Note.UNSAVED_ID) null else noteRepository.getNote(id = noteId)
            val note = stored ?: Note.draft(date = date)

            openedNote = note
            _uiState.value = _uiState.value.copy(
                date = note.date,
                title = note.title,
                content = note.content,
            )
        }
    }

    /** Mirrors the title the user is typing. */
    fun setTitle(value: String) {
        _uiState.value = _uiState.value.copy(title = value)
    }

    /** Mirrors the body the user is typing. */
    fun setContent(value: String) {
        _uiState.value = _uiState.value.copy(content = value)
    }

    /**
     * Hands the note to the store, keeping its id and its original creation time.
     *
     * The timestamps are not set here. The store owns the clock — see [NoteRepository.save] — so
     * this passes the note along as it stands and lets the one place that knows what time it is do
     * the stamping.
     */
    fun save() {
        if (saving) return
        saving = true

        viewModelScope.launch {
            val state = _uiState.value
            val outcome = noteRepository.save(
                note = openedNote.copy(
                    date = state.date,
                    title = state.title,
                    content = state.content,
                ),
            )

            if (outcome is Outcome.Success) {
                // `saving` deliberately stays true — see its own comment. The screen is leaving.
                _uiState.value = _uiState.value.copy(
                    savedTrigger = _uiState.value.savedTrigger + 1,
                )
                return@launch
            }

            // A refusal leaves the user on the screen with their text, so the button has to work
            // again.
            saving = false

            // The store's message is developer-facing — it names the date it refused, which is
            // useful in a log and meaningless in a toast. The screen shows its own wording.
            Log.w(TAG, "save was refused: ${(outcome as? Outcome.Error)?.message}")
            _uiState.value = _uiState.value.copy(saveFailed = true)
        }
    }

    /** Clears [NoteUiState.saveFailed] once the Fragment has shown the message. */
    fun consumeSaveFailed() {
        _uiState.value = _uiState.value.copy(saveFailed = false)
    }
}
