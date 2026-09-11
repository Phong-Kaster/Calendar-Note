package com.example.skeleton.ui.fragment.note

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.FutureDateRefusedException
import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
import com.example.skeleton.ui.fragment.note.model.NoteProblem
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Checks the part of "create a note from Home" that a plain JVM test can actually reach.
 *
 * The screen's job splits in three, and only the middle one is testable here:
 *
 * 1. The **caller** decides which day to open the editor on. From a `LocalDate.now()` in
 *    `HomeFragment` and another in `SettingFragment`; `NoteFragment` has a third as its fallback
 *    when the argument is missing, and `NoteUiState` a fourth as its default. Each of those needs
 *    a running Fragment or a real clock to observe, so **none is covered by any test here** —
 *    "the button on Home creates a note for today" stays a human-inspection item.
 *    **The Calendar screen is the exception, and deliberately so:** its choice of day is DoD
 *    criterion 11, so it was pushed out of the Fragment into
 *    `CalendarViewModel.dateForNewNote()`, where `CalendarViewModelTest` holds it to picking the
 *    *selected* day rather than today. A decision that carries a criterion does not belong
 *    somewhere no test can reach.
 * 2. **This ViewModel carries whatever day it was given through the editing session and hands it
 *    to the store.** That is what the tests below hold it to, and it is where the interesting
 *    mistakes live: an id dropped on the way to `save`, a `createdAt` overwritten, a rotation that
 *    throws away what the user typed, a second tap that writes a second note.
 * 3. The store stamps the times and refuses future dates — covered by `NoteRepositoryImplTest`.
 *
 * `Dispatchers.setMain` is what makes any of this possible: `viewModelScope` posts to
 * `Dispatchers.Main`, which does not exist off a device. Replaced with an unconfined test
 * dispatcher, every `launch` in the ViewModel runs to completion the moment it is started, so a
 * test can call a function and assert on the state on the very next line.
 *
 * @author Phong-Kaster
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NoteViewModelTest {

    private val mainDispatcher = UnconfinedTestDispatcher()

    @Before
    fun replaceMainDispatcher() {
        Dispatchers.setMain(mainDispatcher)
    }

    @After
    fun restoreMainDispatcher() {
        Dispatchers.resetMain()
    }

    @Test
    fun `a new note opens empty, on the day it was opened for`() = runTest {
        val viewModel = NoteViewModel(noteRepository = FakeNoteRepository())

        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        assertEquals(A_DAY, viewModel.uiState.value.date)
        assertEquals("", viewModel.uiState.value.title)
        assertEquals("", viewModel.uiState.value.content)
    }

    @Test
    fun `a stored note opens on its own day, not the one it was asked for`() = runTest {
        // Taking the argument's word for the date would move an old note onto whatever day the
        // caller happened to pass — silently re-filing it the first time somebody re-opened it.
        val stored = Note(
            id = 7L,
            date = A_DAY.minusDays(30L),
            title = "Groceries",
            content = "Coffee, oat milk",
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )
        val viewModel = NoteViewModel(noteRepository = FakeNoteRepository(stored = stored))

        viewModel.openNote(noteId = 7L, date = A_DAY)

        assertEquals(A_DAY.minusDays(30L), viewModel.uiState.value.date)
        assertEquals("Groceries", viewModel.uiState.value.title)
        assertEquals("Coffee, oat milk", viewModel.uiState.value.content)
    }

    @Test
    fun `saving a new note hands the store the day the editor was opened on`() = runTest {
        val repository = FakeNoteRepository()
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        viewModel.setTitle(value = "Groceries")
        viewModel.setContent(value = "Coffee, oat milk")
        viewModel.save()

        val saved = repository.savedNote!!
        assertEquals(A_DAY, saved.date)
        assertEquals("Groceries", saved.title)
        assertEquals("Coffee, oat milk", saved.content)
        // Handed over unstamped: the store, and nothing else in this app, decides what time it is.
        assertEquals(Note.UNSAVED_ID, saved.id)
        assertEquals(Note.UNSAVED_AT, saved.createdAt)
        assertEquals(Note.UNSAVED_AT, saved.updatedAt)
    }

    @Test
    fun `saving an edited note keeps its id and its original creation time`() = runTest {
        // Lose the id and the next save writes a second note instead of changing the first. Lose
        // the `createdAt` and the store, which only replaces a zero, stamps it as brand new.
        val stored = Note(
            id = 7L,
            date = A_DAY,
            title = "Groceries",
            content = "Coffee, oat milk",
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )
        val repository = FakeNoteRepository(stored = stored)
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)

        viewModel.setTitle(value = "Groceries, again")
        viewModel.save()

        val saved = repository.savedNote!!
        assertEquals(7L, saved.id)
        assertEquals(1_000L, saved.createdAt)
        assertEquals("Groceries, again", saved.title)
    }

    @Test
    fun `a blank title does not stop a note being saved`() = runTest {
        val repository = FakeNoteRepository()
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        viewModel.setContent(value = "No title on this one.")
        viewModel.save()

        assertEquals("", repository.savedNote!!.title)
    }

    @Test
    fun `clearing the title of a stored note hands the store a blank one`() = runTest {
        // The editing session starts from the stored note and only overwrites the fields the user
        // touched, which is what keeps the id and the creation time — and it is also where a
        // cleared title could quietly come back to life. `openedNote` still holds "Groceries", so a
        // save that fell back to it on a blank value would make the title field impossible to
        // empty, with the old heading reappearing on Home as if the edit had never happened.
        val stored = Note(
            id = 7L,
            date = A_DAY,
            title = "Groceries",
            content = "Coffee, oat milk",
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )
        val repository = FakeNoteRepository(stored = stored)
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)

        viewModel.setTitle(value = "")
        viewModel.save()

        val saved = repository.savedNote!!
        assertEquals("", saved.title)
        assertEquals(7L, saved.id)
        assertEquals("Coffee, oat milk", saved.content)
    }

    @Test
    fun `a successful save raises the trigger the screen leaves on`() = runTest {
        val viewModel = NoteViewModel(noteRepository = FakeNoteRepository())
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        assertEquals(0, viewModel.uiState.value.savedTrigger)
        viewModel.save()

        assertEquals(1, viewModel.uiState.value.savedTrigger)
        assertFalse(viewModel.uiState.value.saveFailed)
    }

    @Test
    fun `a save that failed reports the failure and does not leave the screen`() = runTest {
        // The screen must stay where it is. Navigating back on a failure would drop what the user
        // wrote and tell them nothing about why.
        //
        // A plain error, with no tag on it: this is the disk-went-wrong path, and the assertion
        // that it is **not** reported as a refusal is what keeps the two apart. Tagging every
        // error as a refusal would pass the refusal tests below and would tell a user whose
        // database is failing that their note is dated wrongly.
        val repository = FakeNoteRepository(outcome = Outcome.Error(message = "the disk is gone"))
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        viewModel.save()

        assertEquals(0, viewModel.uiState.value.savedTrigger)
        assertTrue(viewModel.uiState.value.saveFailed)
        assertNull(viewModel.uiState.value.saveRefusedDate)
    }

    @Test
    fun `a refused save is reported as a refusal, naming the day, and not as a failure`() = runTest {
        // The whole point of telling the two apart. The screen says "please try again" for a
        // failure, and a retry of a *refusal* can never succeed — the note is dated the same day
        // it was a moment ago, and this editor has no date control to change it with. So the
        // refusal has to arrive as its own event, carrying the day, and it must **not** raise
        // `saveFailed`: the toast for that one is an instruction the user cannot act on.
        //
        // **The day the editor was opened on and the day the refusal names are deliberately
        // different here**, and they would never be in the running app. That is what gives this
        // test teeth: the ViewModel reads `refusal.date` — the day the *store* compared, which is
        // the authority — and an implementation reaching for `_uiState.value.date` instead would
        // pass against a fixture where the two agree. The two can only disagree if the store's
        // clock and the screen's argument do, which is exactly the backwards-clock case this whole
        // chain exists for, and the case where naming the wrong day would be worst.
        val refusedDay = A_DAY.plusDays(2L)
        val repository = FakeNoteRepository(
            outcome = Outcome.Error(
                message = "A note cannot be dated after $A_DAY.",
                throwable = FutureDateRefusedException(date = refusedDay, today = A_DAY),
            ),
        )
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        viewModel.save()

        assertEquals(refusedDay, viewModel.uiState.value.saveRefusedDate)
        assertFalse(viewModel.uiState.value.saveFailed)
        // And the user is still on the screen with their text, exactly as after a failure.
        assertEquals(0, viewModel.uiState.value.savedTrigger)
    }

    @Test
    fun `the refusal clears once it has been shown`() = runTest {
        val repository = FakeNoteRepository(
            outcome = Outcome.Error(
                message = "refused",
                throwable = FutureDateRefusedException(date = A_DAY.plusDays(1L), today = A_DAY),
            ),
        )
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY.plusDays(1L))
        viewModel.save()
        // Asserted *before* consuming, or this test proves nothing: the field starts null, so a
        // `save` that never raised it at all would leave the assertion below passing.
        assertEquals(A_DAY.plusDays(1L), viewModel.uiState.value.saveRefusedDate)

        viewModel.consumeSaveRefused()

        assertNull(viewModel.uiState.value.saveRefusedDate)
    }

    @Test
    fun `save works again after a refusal`() = runTest {
        // A refusal leaves the user on the screen with their text, so the button has to work
        // again — the same reasoning as after a failure, and a separate test because the two now
        // take different branches out of `save`. A guard left shut on the refusal path would
        // strand somebody with a note they cannot store on any day.
        val repository = FakeNoteRepository(
            outcome = Outcome.Error(
                message = "refused",
                throwable = FutureDateRefusedException(date = A_DAY.plusDays(1L), today = A_DAY),
            ),
        )
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY.plusDays(1L))

        viewModel.save()
        viewModel.save()

        assertEquals(2, repository.saveCount)
    }

    @Test
    fun `tapping save twice writes the note once`() = runTest {
        // The screen leaves on the first success, but leaving is animated and the composition
        // stays touchable while it plays. A second tap in that window used to reach the store
        // with the note still carrying UNSAVED_ID, and Room's autoGenerate would hand out a
        // second row — one intent, two identical notes on Home, and nothing to tell them apart.
        val repository = FakeNoteRepository()
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        viewModel.save()
        viewModel.save()

        assertEquals(1, repository.saveCount)
        assertEquals(1, viewModel.uiState.value.savedTrigger)
    }

    @Test
    fun `save works again after a failure`() = runTest {
        // The other half of the guard above: a failed write leaves the user sitting on the screen
        // with their text, so a guard that stayed shut would leave them with no way to keep it.
        val repository = FakeNoteRepository(outcome = Outcome.Error(message = "the disk is gone"))
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        viewModel.save()
        viewModel.save()

        assertEquals(2, repository.saveCount)
    }

    @Test
    fun `the failure clears once it has been shown`() = runTest {
        val repository = FakeNoteRepository(outcome = Outcome.Error(message = "the disk is gone"))
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)
        viewModel.save()

        viewModel.consumeSaveFailed()

        assertFalse(viewModel.uiState.value.saveFailed)
    }

    @Test
    fun `opening the same note twice does not throw away what the user typed`() = runTest {
        // This is the rotation case, and it is the normal course of events rather than an edge:
        // the Fragment is rebuilt and calls openNote again while the ViewModel — and the half
        // written note in it — survives.
        val stored = Note(
            id = 7L,
            date = A_DAY,
            title = "Groceries",
            content = "Coffee, oat milk",
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )
        val viewModel = NoteViewModel(noteRepository = FakeNoteRepository(stored = stored))
        viewModel.openNote(noteId = 7L, date = A_DAY)
        viewModel.setTitle(value = "Groceries, again")

        viewModel.openNote(noteId = 7L, date = A_DAY)

        assertEquals("Groceries, again", viewModel.uiState.value.title)
    }

    // ---------- Opening a note that is not there ----------

    @Test
    fun `a note that has since been deleted does not open as a fresh one, and cannot be saved over`() =
        runTest {
            // **This test was reversed on purpose.** It used to assert the opposite — that a
            // missing note fell through to a blank draft for the day asked for — and that
            // fall-through was the defect: the user retyped the note they thought they were
            // editing, tapped Save, and Room's `autoGenerate` filed it as a *second* row. Home
            // then showed two notes: the original with its old text and old position, plus the
            // retyped copy. Nothing anywhere said so.
            //
            // The store now says "I looked, it is not there" as its own answer, and the screen
            // reports that and leaves.
            val repository = FakeNoteRepository(stored = null)
            val viewModel = NoteViewModel(noteRepository = repository)

            viewModel.openNote(noteId = 7L, date = A_DAY)

            assertEquals(NoteProblem.Gone, viewModel.uiState.value.problem)
            // No delete action either: there is nothing behind this screen to delete.
            assertFalse(viewModel.uiState.value.deletable)

            viewModel.save()

            // The assertion that matters. A save from here is the duplicate.
            assertEquals(0, repository.saveCount)
            assertNull(repository.savedNote)
        }

    @Test
    fun `a note the store cannot read reports the failure instead of opening blank`() = runTest {
        // Told apart from the case above deliberately. "It is gone" is a fact; "I could not look"
        // is a failure, and answering it with an empty note is the screen inventing an answer it
        // does not have — one whose save writes a second note beside a first that may well still
        // exist.
        val repository = FakeNoteRepository(stored = null, readFails = true)
        val viewModel = NoteViewModel(noteRepository = repository)

        viewModel.openNote(noteId = 7L, date = A_DAY)

        assertEquals(NoteProblem.Unreadable, viewModel.uiState.value.problem)

        viewModel.save()

        assertEquals(0, repository.saveCount)
    }

    @Test
    fun `the problem clears once it has been shown`() = runTest {
        val viewModel = NoteViewModel(noteRepository = FakeNoteRepository(stored = null))
        viewModel.openNote(noteId = 7L, date = A_DAY)

        // Asserted *before* consuming, because without it this test passes just as happily
        // against an `openNote` that stopped reporting problems at all — the assertion below
        // would then be checking that null is still null.
        assertEquals(NoteProblem.Gone, viewModel.uiState.value.problem)

        viewModel.consumeProblem()

        assertNull(viewModel.uiState.value.problem)
    }

    @Test
    fun `nothing is written or removed before a note has been opened`() = runTest {
        // The ViewModel exists before `openNote` runs — the Fragment constructs it and then calls
        // in from `onCreate`. Until it does there is no note here, and both write paths have to
        // know that rather than acting on a plausible-looking empty one.
        val repository = FakeNoteRepository()
        val viewModel = NoteViewModel(noteRepository = repository)

        viewModel.save()
        // `askToDelete()` first, and that detail is the test. Calling `delete()` on its own
        // returns at the confirmation guard and never reaches the "is there a note?" guard this
        // test claims to be about — so the delete half would pass while proving nothing.
        // `askToDelete()` is itself refused here (nothing is deletable yet), which is why
        // `confirmingDelete` is asserted false rather than assumed.
        viewModel.askToDelete()
        assertFalse(viewModel.uiState.value.confirmingDelete)
        viewModel.delete()

        assertEquals(0, repository.saveCount)
        assertEquals(0, repository.deleteCount)
    }

    // ---------- Deleting, behind a confirmation ----------

    @Test
    fun `a brand-new note offers no delete action, and cannot be asked to delete one`() = runTest {
        // `deletable` is false by default, so asserting only that would be asserting the default
        // — a test that passes against an `openNote` which never touches the field. The second
        // half is what has teeth: the guard in `askToDelete()` must also hold, because the
        // hidden control is a fact about the top bar and not about this class.
        val repository = FakeNoteRepository()
        val viewModel = NoteViewModel(noteRepository = repository)

        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)
        viewModel.askToDelete()

        assertFalse(viewModel.uiState.value.deletable)
        assertFalse(viewModel.uiState.value.confirmingDelete)

        viewModel.delete()

        assertEquals(0, repository.deleteCount)
    }

    @Test
    fun `a stored note offers a delete action`() = runTest {
        val viewModel = NoteViewModel(noteRepository = FakeNoteRepository(stored = A_STORED_NOTE))

        viewModel.openNote(noteId = 7L, date = A_DAY)

        assertTrue(viewModel.uiState.value.deletable)
    }

    @Test
    fun `asking to delete opens the confirmation and deletes nothing`() = runTest {
        // DoD criterion 7: no single tap deletes a note. The tap that *looks* like the delete is
        // this one, and all it does is put a question on screen.
        val repository = FakeNoteRepository(stored = A_STORED_NOTE)
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)

        viewModel.askToDelete()

        assertTrue(viewModel.uiState.value.confirmingDelete)
        assertEquals(0, repository.deleteCount)
        assertEquals(0, viewModel.uiState.value.deletedTrigger)
    }

    @Test
    fun `a delete that skipped the confirmation does nothing`() = runTest {
        // The other half of the same criterion, and the reason it is a property of this class and
        // not of the layout: even a caller that reaches `delete()` directly cannot get past the
        // confirmation it never opened.
        val repository = FakeNoteRepository(stored = A_STORED_NOTE)
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)

        viewModel.delete()

        assertEquals(0, repository.deleteCount)
        assertEquals(0, viewModel.uiState.value.deletedTrigger)
    }

    @Test
    fun `confirming removes the note the screen was showing and leaves`() = runTest {
        val repository = FakeNoteRepository(stored = A_STORED_NOTE)
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)
        viewModel.askToDelete()

        viewModel.delete()

        // The right note, not just any note: an id dropped on the way down would delete nothing,
        // or — worse, once a second screen reuses this — somebody else's row.
        assertEquals(7L, repository.deletedNote!!.id)
        assertEquals(1, repository.deleteCount)
        assertEquals(1, viewModel.uiState.value.deletedTrigger)
        // The question comes down with the note.
        assertFalse(viewModel.uiState.value.confirmingDelete)
        // And so does the delete action — there is nothing left behind this screen.
        assertFalse(viewModel.uiState.value.deletable)
    }

    @Test
    fun `a save after the note has been deleted does not put it back`() = runTest {
        // **The worst thing this screen could do, and it was doing it.** The screen leaves on a
        // successful delete, but leaving is animated: `toNote`'s `popExitAnim` runs for
        // `config_longAnimTime` (500 ms), and a legacy View animation leaves the exiting view in
        // the hierarchy, un-transformed for hit-testing, taking touches the whole time. Save is
        // still on screen and still clickable in that window.
        //
        // Before the fix, `save()` found everything it needed: a real id, a real `createdAt`, and
        // its own guard still open, because only `deleting` had been raised. `upsert` is
        // `onConflict = REPLACE`, so the tap **re-inserted the deleted row** — one frame after
        // the user was told "Note deleted", and at the top of Home, because `updatedAt` was
        // fresh. The note the user deleted came back and looked like the newest thing they wrote.
        val repository = FakeNoteRepository(stored = A_STORED_NOTE)
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)
        viewModel.askToDelete()
        viewModel.delete()

        viewModel.setTitle(value = "Groceries, again")
        viewModel.save()

        assertEquals(0, repository.saveCount)
        assertNull(repository.savedNote)
    }

    @Test
    fun `backing out of the confirmation deletes nothing`() = runTest {
        val repository = FakeNoteRepository(stored = A_STORED_NOTE)
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)
        viewModel.askToDelete()

        viewModel.dismissDelete()

        assertFalse(viewModel.uiState.value.confirmingDelete)
        assertEquals(0, repository.deleteCount)
    }

    @Test
    fun `two taps while the first delete is still in flight delete once`() = runTest {
        // **This test had to be rewritten before it tested anything.** Its first version called
        // `delete()` twice in a row and passed — but not for the reason it claimed: the first call
        // had already finished and lowered `confirmingDelete`, so the *confirmation* guard refused
        // the second one and the in-flight guard was never reached. Disabling that guard left the
        // test green. Mutation testing is the only reason anybody found out.
        //
        // This is the shape of the real race. The confirmation comes down only when the store
        // answers, so two taps inside that window both find it still open, and only the in-flight
        // guard stands between them and a second delete. The gate holds the store mid-call so the
        // window can be entered on purpose.
        val gate = CompletableDeferred<Unit>()
        val repository = FakeNoteRepository(stored = A_STORED_NOTE, deleteGate = gate)
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)
        viewModel.askToDelete()

        viewModel.delete()
        // The store is suspended inside the first delete, and the sheet has not closed yet.
        assertTrue(viewModel.uiState.value.confirmingDelete)
        viewModel.delete()

        gate.complete(Unit)

        assertEquals(1, repository.deleteCount)
        assertEquals(1, viewModel.uiState.value.deletedTrigger)
    }

    @Test
    fun `confirming again after the sheet has closed deletes nothing more`() = runTest {
        // A different guard from the one above, and worth its own test: the screen leaves on the
        // first success, and leaving is animated, so the composition keeps taking touches for a few
        // hundred milliseconds. This second tap is refused because the confirmation is no longer
        // open — a second delete would find no row, report no problem, and announce the deletion
        // all over again.
        val repository = FakeNoteRepository(stored = A_STORED_NOTE)
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)
        viewModel.askToDelete()

        viewModel.delete()
        viewModel.delete()

        assertEquals(1, repository.deleteCount)
        assertEquals(1, viewModel.uiState.value.deletedTrigger)
    }

    @Test
    fun `a delete that fails says so and keeps the user on the screen`() = runTest {
        val repository = FakeNoteRepository(
            stored = A_STORED_NOTE,
            deleteOutcome = Outcome.Error(message = "the disk said no"),
        )
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)
        viewModel.askToDelete()

        viewModel.delete()

        assertEquals(NoteProblem.DeleteFailed, viewModel.uiState.value.problem)
        // The screen must not leave: the note is still there, and leaving would tell the user the
        // opposite of what happened.
        assertEquals(0, viewModel.uiState.value.deletedTrigger)
        // And the confirmation comes down — left up under a failure message it reads as though
        // tapping it again might work.
        assertFalse(viewModel.uiState.value.confirmingDelete)
    }

    @Test
    fun `delete works again after a failure`() = runTest {
        // The counterpart of `save works again after a refusal`. A guard that stayed shut would
        // leave the user unable to retry a delete that failed for a passing reason.
        val repository = FakeNoteRepository(
            stored = A_STORED_NOTE,
            deleteOutcome = Outcome.Error(message = "the disk said no"),
        )
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = 7L, date = A_DAY)

        viewModel.askToDelete()
        viewModel.delete()
        viewModel.askToDelete()
        viewModel.delete()

        assertEquals(2, repository.deleteCount)
    }

    private companion object {

        /** Any ordinary day. Fixed, so nothing here depends on when the suite runs. */
        private val A_DAY: LocalDate = LocalDate.of(2026, 3, 14)

        /** A note that already exists in the store, on [A_DAY], with row id 7. */
        private val A_STORED_NOTE = Note(
            id = 7L,
            date = A_DAY,
            title = "Groceries",
            content = "Coffee, oat milk",
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )
    }
}

/**
 * A notes store that remembers what it was asked to do and answers with whatever it was built with.
 *
 * The counters are as important as the recorded notes. Several tests below are about something
 * **not** happening — a save that must not write a duplicate, a delete that must not run without a
 * confirmation — and "the last note handed over is still null" is a weaker statement than "the
 * store was never called".
 *
 * @param stored the note [getNote] answers with, or null to behave like a store that has never
 *   heard of the id being asked for. Note the difference from [readFails] below: this one is a
 *   store that answers "no such note", which is not a failure.
 * @param outcome what [save] reports back. Defaults to success. A plain [Outcome.Error] is a write
 *   that went wrong; one carrying a
 *   [com.example.skeleton.domain.model.FutureDateRefusedException] is the calendar rule refusing
 *   the note. The screen has to say different things about those two, so the fake has to be able
 *   to be both — and neither needs a real clock here.
 * @param readFails true to make [getNote] report that it could not read at all. That is a
 *   different answer from [stored] being null, and the screen is required to treat it differently.
 * @param deleteOutcome what [delete] reports back.
 * @param deleteGate when given, [delete] records the call and then **waits** on it before
 *   answering. That is what lets a test stand inside the moment a real delete is in flight — the
 *   window where a second tap is still possible — instead of only before and after it. Left null,
 *   the store answers straight away like the others.
 * @author Phong-Kaster
 */
private class FakeNoteRepository(
    private val stored: Note? = null,
    private val outcome: Outcome<Unit> = Outcome.Success(Unit),
    private val readFails: Boolean = false,
    private val deleteOutcome: Outcome<Unit> = Outcome.Success(Unit),
    private val deleteGate: CompletableDeferred<Unit>? = null,
) : NoteRepository {

    /** The last note [save] was given, exactly as it was given. */
    var savedNote: Note? = null
        private set

    /** How many times [save] was called. A note written twice is a note the user now has two of. */
    var saveCount: Int = 0
        private set

    /** The last note [delete] was given. */
    var deletedNote: Note? = null
        private set

    /** How many times [delete] was called. */
    var deleteCount: Int = 0
        private set

    override val notesFlow: Flow<List<Note>> = flowOf(listOfNotNull(stored))

    // The Note screen never reads a whole day — it opens one note by id — so this is here to
    // satisfy the interface and nothing more. It still filters rather than returning everything,
    // because a fake that lies about its contract is a trap for the next test written against it.
    override fun notesForDateFlow(date: LocalDate): Flow<List<Note>> =
        flowOf(listOfNotNull(stored).filter { note -> note.date == date })

    override suspend fun getNote(id: Long): Outcome<Note?> {
        if (readFails) return Outcome.Error(message = "the store would not answer")
        return Outcome.Success(stored?.takeIf { note -> note.id == id })
    }

    override suspend fun save(note: Note): Outcome<Unit> {
        savedNote = note
        saveCount++
        return outcome
    }

    override suspend fun delete(note: Note): Outcome<Unit> {
        deletedNote = note
        // Counted *before* the wait, on purpose: the count means "the store was entered", which is
        // the thing a second tap must not be able to do twice.
        deleteCount++
        deleteGate?.await()
        return deleteOutcome
    }
}
