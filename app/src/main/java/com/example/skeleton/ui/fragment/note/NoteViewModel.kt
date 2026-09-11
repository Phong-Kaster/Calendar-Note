package com.example.skeleton.ui.fragment.note

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.FutureDateRefusedException
import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
import com.example.skeleton.ui.fragment.note.model.NoteProblem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

/**
 * Drives the Note screen: holds what the user is typing, hands it to the store on save, and takes
 * it back out on delete.
 *
 * This ViewModel has no `init` block, and that is deliberate. Every other screen in this app knows
 * what to load the moment it exists; this one does not — it has to be *told* which note to open,
 * because that arrives as a navigation argument. So the Fragment calls [openNote] once, and until
 * it does there is no note here at all.
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
     *
     * **`null` means there is nothing to write, and that is load-bearing.** It is null in three
     * situations: before [openNote] has run; when opening failed, because the note was deleted
     * elsewhere or the store would not answer; and after [delete] has succeeded. Both [save] and
     * [delete] stop on it. Without the second case, a screen that failed to open would still be
     * holding a plausible-looking blank note, and one tap on Save would file it as a brand-new one
     * beside the original the user was trying to edit. Without the third, Save would still be
     * holding the note that was just deleted, and would put it back.
     */
    private var openedNote: Note? = null

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
     * Guards [delete] the same way [saving] guards a save, and for the same reason.
     *
     * The second tap of a double tap would reach a row that is already gone. Room's `@Delete`
     * matches nothing and reports no problem, so the screen would announce the deletion twice and
     * try to leave twice — one intent, two messages, and a user wondering what the second one was
     * about.
     */
    private var deleting = false

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
            if (noteId == Note.UNSAVED_ID) {
                openedNote = Note.draft(date = date)
                _uiState.value = _uiState.value.copy(date = date)
                return@launch
            }

            val outcome = noteRepository.getNote(id = noteId)

            // Not a successful read at all: the store could not be asked. `Loading` lands here too
            // and that is correct — `getNote` never returns it, so seeing one would mean something
            // has changed underneath and the honest answer is still "I do not know".
            if (outcome !is Outcome.Success) {
                Log.w(TAG, "openNote($noteId) could not read the store")
                _uiState.value = _uiState.value.copy(problem = NoteProblem.Unreadable)
                return@launch
            }

            // A successful read of nothing: the note is genuinely not there any more. The screen
            // says so and leaves. It deliberately does **not** fall through to a fresh draft —
            // that used to be the behaviour, and it turned "the note you tapped was deleted" into
            // "here is a blank page whose save quietly duplicates the original".
            val stored = outcome.data
            if (stored == null) {
                Log.w(TAG, "openNote($noteId) found no such note")
                _uiState.value = _uiState.value.copy(problem = NoteProblem.Gone)
                return@launch
            }

            openedNote = stored
            _uiState.value = _uiState.value.copy(
                date = stored.date,
                title = stored.title,
                content = stored.content,
                // Only now, with a real row behind the screen, is there something to delete.
                deletable = true,
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
        // Nothing was ever opened, or opening failed. There is no note to write, and writing the
        // typed text as a new one would be the duplicate this screen exists not to make.
        val note = openedNote ?: return
        if (saving) return
        saving = true

        viewModelScope.launch {
            val state = _uiState.value
            val outcome = noteRepository.save(
                note = note.copy(
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

            // Nothing was stored, so the user is still here with their text and the button has to
            // work again. True of both branches below — a refusal and a failure alike.
            saving = false

            // **Which kind of "no" this was decides what the user is told, and the store says so
            // by type.** A refused note is dated a day that has not arrived; nothing about
            // tapping Save again changes that, and this screen offers no date control to change
            // it with — so "something went wrong, please try again" would be an instruction that
            // cannot work. A write that merely failed *is* worth another tap.
            //
            // The store's own `message` is developer-facing and is deliberately not shown: the
            // screen picks its own wording, and matching on that text to decide which wording to
            // pick would break the next time somebody rephrased a log line.
            val refusal = (outcome as? Outcome.Error)?.throwable as? FutureDateRefusedException
            if (refusal != null) {
                Log.w(TAG, "save refused: ${refusal.date} is after ${refusal.today}")
                // The day comes from the refusal rather than from `state.date`, so the message
                // names the day the store actually compared — the two agree today, and if they
                // ever stop, the store is the one that decided.
                _uiState.value = _uiState.value.copy(saveRefusedDate = refusal.date)
                return@launch
            }

            Log.w(TAG, "save failed: ${(outcome as? Outcome.Error)?.message}")
            _uiState.value = _uiState.value.copy(saveFailed = true)
        }
    }

    /**
     * The user asked to delete the note. **This does not delete anything.**
     *
     * It raises [NoteUiState.confirmingDelete] and stops. Deleting is the only irreversible thing
     * this app does, so the store is unreachable from one tap by construction: the single caller of
     * [delete] is the confirming control inside the sheet this flag opens.
     */
    fun askToDelete() {
        // Guarded for the same reason [delete] is, and it was missing here at first. A brand-new
        // note's `openedNote` is a draft carrying [Note.UNSAVED_ID]; ask to delete one and the
        // store refuses it, so the user gets "the note could not be deleted" about a note that
        // never existed — the wrong sentence, and a confirmation sheet asking a question with no
        // meaningful answer. Today only the hidden top-bar action keeps that off the screen,
        // which is exactly the "the only caller today" reasoning [delete] declines to rely on.
        if (!_uiState.value.deletable) return

        _uiState.value = _uiState.value.copy(confirmingDelete = true)
    }

    /** The user backed out of the confirmation. Nothing was deleted. */
    fun dismissDelete() {
        _uiState.value = _uiState.value.copy(confirmingDelete = false)
    }

    /**
     * Removes the note the screen is showing. Called only after the user has confirmed.
     *
     * Home needs no nudge afterwards: the store's list is a live stream, so the row leaves the
     * screen behind this one on its own.
     */
    fun delete() {
        // **The confirmation is not optional, and this line is what makes that a fact about this
        // class rather than a habit of the layout that calls it.** The sheet is the only caller,
        // but "the only caller today" is not a guarantee — the next screen to reuse this ViewModel
        // could wire a delete straight to a row, and the loss would be silent and permanent. With
        // this guard, a delete that skipped the confirmation does nothing at all, and a test can
        // say so.
        if (!_uiState.value.confirmingDelete) return

        // Same reasoning as `save`: nothing opened means nothing to remove.
        val note = openedNote ?: return
        if (deleting) return
        deleting = true

        viewModelScope.launch {
            val outcome = noteRepository.delete(note = note)

            if (outcome is Outcome.Success) {
                // **The note goes here, not just from the database.** The screen is leaving, but
                // leaving is animated — `toNote`'s `popExitAnim` runs for `config_longAnimTime`,
                // and a legacy View animation leaves the exiting view in the hierarchy taking
                // touches for the whole of it. Save is still on screen and still clickable in
                // that window, and `save()` would have found a perfectly valid note here: a real
                // id, a real `createdAt`, and `saving` still false. `upsert` is
                // `onConflict = REPLACE`, so that tap would put the row **back** — one frame
                // after the user was told it was deleted, at the top of Home because
                // `updatedAt` is fresh. Dropping the note is what makes `save()` return at its
                // own first line instead. `deleting` stays true for the same reason it does in
                // `save`; the two guards are now symmetric.
                openedNote = null
                _uiState.value = _uiState.value.copy(
                    confirmingDelete = false,
                    // Nothing left to delete either, so the action goes with it.
                    deletable = false,
                    deletedTrigger = _uiState.value.deletedTrigger + 1,
                )
                return@launch
            }

            // The note is still there and the user is still looking at it, so the action has to
            // work again.
            deleting = false

            Log.w(TAG, "delete failed: ${(outcome as? Outcome.Error)?.message}")
            _uiState.value = _uiState.value.copy(
                // The confirmation closes either way: leaving it up under a failure message reads
                // as though tapping it again might work.
                confirmingDelete = false,
                problem = NoteProblem.DeleteFailed,
            )
        }
    }

    /** Clears [NoteUiState.saveFailed] once the Fragment has shown the message. */
    fun consumeSaveFailed() {
        _uiState.value = _uiState.value.copy(saveFailed = false)
    }

    /** Clears [NoteUiState.saveRefusedDate] once the Fragment has shown the message. */
    fun consumeSaveRefused() {
        _uiState.value = _uiState.value.copy(saveRefusedDate = null)
    }

    /** Clears [NoteUiState.problem] once the Fragment has shown the matching message. */
    fun consumeProblem() {
        _uiState.value = _uiState.value.copy(problem = null)
    }
}
