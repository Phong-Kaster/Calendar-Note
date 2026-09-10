package com.example.skeleton.ui.fragment.note

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Note
import com.example.skeleton.domain.repository.NoteRepository
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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

/**
 * Checks the part of "create a note from Home" that a plain JVM test can actually reach.
 *
 * The screen's job splits in three, and only the middle one is testable here:
 *
 * 1. The **caller** decides which day to open the editor on. Today, in this task, from a
 *    `LocalDate.now()` in `HomeFragment` and another in `SettingFragment`; `NoteFragment` has a
 *    third as its fallback when the argument is missing, and `NoteUiState` a fourth as its
 *    default. Every one of them needs a running Fragment or a real clock to observe, so **none is
 *    covered by any test in this project** — "the button creates a note for today" stays a
 *    human-inspection item, and it is the weakest link in this screen's evidence.
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
    fun `a successful save raises the trigger the screen leaves on`() = runTest {
        val viewModel = NoteViewModel(noteRepository = FakeNoteRepository())
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        assertEquals(0, viewModel.uiState.value.savedTrigger)
        viewModel.save()

        assertEquals(1, viewModel.uiState.value.savedTrigger)
        assertFalse(viewModel.uiState.value.saveFailed)
    }

    @Test
    fun `a refused save reports the failure and does not leave the screen`() = runTest {
        // The screen must stay where it is. Navigating back on a refusal would drop what the user
        // wrote and tell them nothing about why.
        val repository = FakeNoteRepository(outcome = Outcome.Error(message = "refused"))
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        viewModel.save()

        assertEquals(0, viewModel.uiState.value.savedTrigger)
        assertTrue(viewModel.uiState.value.saveFailed)
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
    fun `save works again after a refusal`() = runTest {
        // The other half of the guard above: a refusal leaves the user sitting on the screen with
        // their text, so a guard that stayed shut would leave them with no way to keep it.
        val repository = FakeNoteRepository(outcome = Outcome.Error(message = "refused"))
        val viewModel = NoteViewModel(noteRepository = repository)
        viewModel.openNote(noteId = Note.UNSAVED_ID, date = A_DAY)

        viewModel.save()
        viewModel.save()

        assertEquals(2, repository.saveCount)
    }

    @Test
    fun `the failure clears once it has been shown`() = runTest {
        val repository = FakeNoteRepository(outcome = Outcome.Error(message = "refused"))
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

    @Test
    fun `a note that has since been deleted opens as a fresh one for the day asked for`() = runTest {
        // `getNote` answers null for a row that is gone. Falling through to a draft is the
        // survivable outcome; the alternative is a screen showing someone else's note or nothing
        // at all.
        val repository = FakeNoteRepository(stored = null)
        val viewModel = NoteViewModel(noteRepository = repository)

        viewModel.openNote(noteId = 7L, date = A_DAY)

        assertEquals(A_DAY, viewModel.uiState.value.date)
        assertEquals("", viewModel.uiState.value.title)

        viewModel.save()

        assertEquals(Note.UNSAVED_ID, repository.savedNote!!.id)
    }

    private companion object {

        /** Any ordinary day. Fixed, so nothing here depends on when the suite runs. */
        private val A_DAY: LocalDate = LocalDate.of(2026, 3, 14)
    }
}

/**
 * A notes store that remembers the last note handed to it and answers with whatever it was built
 * with.
 *
 * @param stored the note [getNote] returns, or null to behave like a store that has never heard of
 *   the id being asked for.
 * @param outcome what [save] reports back. Defaults to success; hand it an
 *   [Outcome.Error] to test the refusal path without needing a real calendar rule.
 * @author Phong-Kaster
 */
private class FakeNoteRepository(
    private val stored: Note? = null,
    private val outcome: Outcome<Unit> = Outcome.Success(Unit),
) : NoteRepository {

    /** The last note [save] was given, exactly as it was given. */
    var savedNote: Note? = null
        private set

    /** How many times [save] was called. A note written twice is a note the user now has two of. */
    var saveCount: Int = 0
        private set

    override val notesFlow: Flow<List<Note>> = flowOf(listOfNotNull(stored))

    override suspend fun getNote(id: Long): Note? = stored?.takeIf { note -> note.id == id }

    override suspend fun save(note: Note): Outcome<Unit> {
        savedNote = note
        saveCount++
        return outcome
    }
}
