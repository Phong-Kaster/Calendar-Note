package com.example.skeleton.data.repository

import com.example.skeleton.common.Outcome
import com.example.skeleton.data.database.local.dao.NoteDao
import com.example.skeleton.data.database.local.entity.NoteEntity
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mapper.toEntity
import com.example.skeleton.data.repository.impl.NoteRepositoryImpl
import com.example.skeleton.domain.model.Note
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Holds [NoteRepositoryImpl] to the promises its interface makes: notes come out most recently
 * touched first whatever order they went in, a note dated after today never goes in at all, a note
 * that is deleted goes away without taking its neighbours with it, and **nothing that goes wrong
 * inside ever leaves as an exception**.
 *
 * That last one earns a second fake, [BrokenNoteDao], because a fail-soft contract is only a claim
 * until something actually fails. It also earns the distinction the reading tests are about: a read
 * has *three* answers here — the note, no note, or no answer — and collapsing the last two into one
 * `null` is what let a failed read open a blank editor that duplicated the note it was meant to
 * edit.
 *
 * The DAO underneath is a fake — a list in memory, no Room, no Android, no device. That is the
 * point of the fake rather than a convenience: the real DAO sorts in SQL, and a SQL `ORDER BY` can
 * only be checked by running SQLite, which needs an emulator this project's test setup does not
 * have. So the fake deliberately hands back a **jumbled** list. If the repository were relying on
 * the database to sort for it, these tests would fail, which is exactly what they exist to notice.
 *
 * Neither rule is this file's invention: `knowledge/DOMAIN.md` states both, including the traps
 * they exist to prevent. Ordering by `createdAt` looks perfect on a fresh database and stops being
 * right the first time somebody edits an old note — the second test below is that trap, written
 * down. And the future-date boundary is stated inclusively on purpose, because an off-by-one in
 * one direction lets tomorrow through and in the other makes it impossible to write a note at all;
 * both directions are tested.
 *
 * The clock is a constructor parameter and every test that cares passes [CLOCK], a fixed one. A
 * test that read the real clock could not assert on a timestamp at all, and "is today allowed?"
 * would depend on what day the suite happened to run.
 *
 * @author Phong-Kaster
 */
class NoteRepositoryImplTest {

    @Test
    fun `notes come out newest first even when the dao hands them over jumbled`() = runTest {
        val dao = FakeNoteDao(
            rows = listOf(
                entity(id = 1L, updatedAt = 3_000L),
                entity(id = 2L, updatedAt = 1_000L),
                entity(id = 3L, updatedAt = 5_000L),
                entity(id = 4L, updatedAt = 2_000L),
            ),
        )

        val notes = NoteRepositoryImpl(noteDao = dao).notesFlow.first()

        assertEquals(listOf(3L, 1L, 4L, 2L), notes.map { note -> note.id })
    }

    @Test
    fun `notes are ordered by the most recent edit, not by creation`() = runTest {
        // The old note was written first and edited a moment ago; the new one was written later
        // and never touched again. "Most recently touched" puts the old one on top.
        val dao = FakeNoteDao(
            rows = listOf(
                entity(id = 10L, createdAt = 1_000L, updatedAt = 9_000L),
                entity(id = 20L, createdAt = 5_000L, updatedAt = 5_000L),
            ),
        )

        val notes = NoteRepositoryImpl(noteDao = dao).notesFlow.first()

        assertEquals(listOf(10L, 20L), notes.map { note -> note.id })
    }

    @Test
    fun `notes saved in the same millisecond keep a stable order`() = runTest {
        val dao = FakeNoteDao(
            rows = listOf(
                entity(id = 7L, updatedAt = 4_000L),
                entity(id = 9L, updatedAt = 4_000L),
                entity(id = 8L, updatedAt = 4_000L),
            ),
        )

        val notes = NoteRepositoryImpl(noteDao = dao).notesFlow.first()

        assertEquals(listOf(9L, 8L, 7L), notes.map { note -> note.id })
    }

    @Test
    fun `an empty table produces an empty list, not a failure`() = runTest {
        val notes = NoteRepositoryImpl(noteDao = FakeNoteDao(rows = emptyList())).notesFlow.first()

        assertEquals(emptyList<Long>(), notes.map { note -> note.id })
    }

    @Test
    fun `a stored day survives the trip out of the database`() = runTest {
        // The table keeps a day as an epoch day (a plain number). If that conversion were wrong in
        // either direction the note would silently show the wrong date — the kind of bug that
        // looks like a formatting problem for a week.
        val dao = FakeNoteDao(
            rows = listOf(entity(id = 1L, date = LocalDate.of(2026, 3, 14).toEpochDay())),
        )

        val note = NoteRepositoryImpl(noteDao = dao).notesFlow.first().single()

        assertEquals(LocalDate.of(2026, 3, 14), note.date)
        assertEquals("Groceries", note.title)
        assertEquals("Coffee, oat milk", note.content)
    }

    @Test
    fun `a note survives the round trip back into the database and out again`() = runTest {
        // The way back in has no caller yet — the first one arrives when notes become writable —
        // so nothing else would notice a swapped `createdAt`/`updatedAt` or a day converted with
        // the wrong sign until a user saw a note filed under the wrong date.
        val note = Note(
            id = 42L,
            date = LocalDate.of(2026, 3, 14),
            title = "Groceries",
            content = "Coffee, oat milk",
            createdAt = 1_000L,
            updatedAt = 2_000L,
        )

        assertEquals(note, note.toEntity().toDomain())
    }

    // ---------- Saving: the store owns the clock ----------

    @Test
    fun `a brand-new note is stamped with the current time on both timestamps`() = runTest {
        val dao = FakeNoteDao(rows = emptyList())
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)

        val outcome = repository.save(note = Note.draft(date = TODAY, title = "Groceries"))

        assertTrue(outcome is Outcome.Success)
        val stored = repository.notesFlow.first().single()
        assertEquals(NOW_MILLIS, stored.createdAt)
        assertEquals(NOW_MILLIS, stored.updatedAt)
    }

    @Test
    fun `editing a note moves updatedAt and leaves createdAt where it was`() = runTest {
        // This is the half of the ordering rule that a fresh database cannot show. If `save`
        // stamped `createdAt` too, every note in the app would report having been written the
        // moment it was last touched — and nothing on any screen would look wrong.
        val dao = FakeNoteDao(rows = listOf(entity(id = 5L, createdAt = 1_000L, updatedAt = 1_000L)))
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)

        val existing = repository.storedNote(id = 5L)
        val outcome = repository.save(note = existing.copy(title = "Groceries, again"))

        assertTrue(outcome is Outcome.Success)
        val stored = repository.storedNote(id = 5L)
        assertEquals(1_000L, stored.createdAt)
        assertEquals(NOW_MILLIS, stored.updatedAt)
        assertEquals("Groceries, again", stored.title)
    }

    @Test
    fun `editing an old note moves it to the head of the list`() = runTest {
        // The list half of the ordering rule, and the one assertion that catches an accidental
        // `ORDER BY createdAt`: note 10 is the *older* of the two by creation and becomes the newer
        // by edit. A store that sorted by creation would leave it exactly where it was and every
        // other test in this file would still pass.
        //
        // Written through `save` rather than by seeding a row with a high `updatedAt`, because the
        // re-ordering the user sees is the product of the write path and the read path agreeing —
        // seeding the answer would test the comparator twice and the write path not at all.
        // Note 10 is dated three days back rather than today, so the last assertion below has
        // something to say: an edit that re-filed the note onto the day it was edited would move a
        // past note into today, and a fixture already dated today could not tell the difference.
        val dao = FakeNoteDao(
            rows = listOf(
                entity(
                    id = 10L,
                    date = TODAY.minusDays(3L).toEpochDay(),
                    createdAt = 1_000L,
                    updatedAt = 1_000L,
                ),
                entity(id = 20L, createdAt = 5_000L, updatedAt = 5_000L),
            ),
        )
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)
        assertEquals(listOf(20L, 10L), repository.notesFlow.first().map { note -> note.id })

        val outcome = repository.save(
            note = repository.storedNote(id = 10L).copy(content = "Coffee, oat milk, and bread"),
        )

        assertTrue(outcome is Outcome.Success)
        val notes = repository.notesFlow.first()
        assertEquals(listOf(10L, 20L), notes.map { note -> note.id })
        // The edit changed a note; it did not add one. An `id` dropped on the way in would land as
        // a third row here rather than replacing the first.
        assertEquals(2, notes.size)
        assertEquals("Coffee, oat milk, and bread", notes.first().content)
        assertEquals(1_000L, notes.first().createdAt)
        // Editing a note does not move it to another day.
        assertEquals(TODAY.minusDays(3L), notes.first().date)
    }

    @Test
    fun `a title edited to blank stays blank, and the heading falls back to the body`() = runTest {
        // A title the user deliberately cleared must not come back. The tempting mistake is a
        // `title.ifBlank { existing.title }` somewhere on the way down — it reads like kindness and
        // it makes a field impossible to empty.
        val dao = FakeNoteDao(rows = listOf(entity(id = 5L)))
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)

        val outcome = repository.save(note = repository.storedNote(id = 5L).copy(title = ""))

        assertTrue(outcome is Outcome.Success)
        val stored = repository.storedNote(id = 5L)
        assertEquals("", stored.title)
        // What the row draws instead — the first written line of the body, not the old title.
        assertEquals("Coffee, oat milk", stored.displayTitle)
    }

    @Test
    fun `the day a note was given is the day it is stored under`() = runTest {
        val dao = FakeNoteDao(rows = emptyList())
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)

        repository.save(note = Note.draft(date = TODAY.minusDays(3L)))

        assertEquals(TODAY.minusDays(3L), repository.notesFlow.first().single().date)
    }

    // ---------- Saving: the store owns the calendar (knowledge/DOMAIN.md, rule 1) ----------

    @Test
    fun `a note dated tomorrow is refused, and nothing is written`() = runTest {
        val dao = FakeNoteDao(rows = emptyList())
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)

        val outcome = repository.save(note = Note.draft(date = TODAY.plusDays(1L)))

        assertTrue(outcome is Outcome.Error)
        // The refusal has to mean the row never appeared. A `save` that reported an error *and*
        // wrote the row would pass an assertion on the return value alone.
        assertEquals(emptyList<Note>(), repository.notesFlow.first())
    }

    @Test
    fun `a note dated far in the future is refused too`() = runTest {
        val repository = NoteRepositoryImpl(noteDao = FakeNoteDao(rows = emptyList()), clock = CLOCK)

        val outcome = repository.save(note = Note.draft(date = TODAY.plusYears(5L)))

        assertTrue(outcome is Outcome.Error)
    }

    @Test
    fun `a note dated today is accepted`() = runTest {
        // The boundary is inclusive. Getting this one wrong makes the app unable to write the only
        // note the create button ever offers.
        val repository = NoteRepositoryImpl(noteDao = FakeNoteDao(rows = emptyList()), clock = CLOCK)

        val outcome = repository.save(note = Note.draft(date = TODAY))

        assertTrue(outcome is Outcome.Success)
    }

    @Test
    fun `a note dated yesterday is accepted`() = runTest {
        val repository = NoteRepositoryImpl(noteDao = FakeNoteDao(rows = emptyList()), clock = CLOCK)

        val outcome = repository.save(note = Note.draft(date = TODAY.minusDays(1L)))

        assertTrue(outcome is Outcome.Success)
    }

    @Test
    fun `an existing note cannot be edited into the future either`() = runTest {
        // The rule covers every write path, not just creation. Moving an old note's date forward
        // is the same violation as creating one there.
        val dao = FakeNoteDao(rows = listOf(entity(id = 5L)))
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)

        val existing = repository.storedNote(id = 5L)
        val outcome = repository.save(note = existing.copy(date = TODAY.plusDays(1L)))

        assertTrue(outcome is Outcome.Error)
        assertEquals(TODAY, repository.storedNote(id = 5L).date)
    }

    // ---------- Reading one note: three answers, not two ----------

    @Test
    fun `asking for a note that is not there is a successful read of nothing, not an error`() = runTest {
        // The distinction this asserts is the whole reason `getNote` returns an `Outcome`. "I
        // looked and it is not there" is an ordinary answer a screen can act on — the note was
        // deleted. Reporting it as an error would make it indistinguishable from a database that
        // would not answer, and the screen above can only behave correctly if it can tell.
        val repository = NoteRepositoryImpl(noteDao = FakeNoteDao(rows = emptyList()), clock = CLOCK)

        val outcome = repository.getNote(id = 404L)

        assertTrue(outcome is Outcome.Success)
        assertNull((outcome as Outcome.Success).data)
    }

    @Test
    fun `asking for a note that is there answers with it`() = runTest {
        val repository = NoteRepositoryImpl(noteDao = FakeNoteDao(rows = listOf(entity(id = 5L))), clock = CLOCK)

        val outcome = repository.getNote(id = 5L)

        assertTrue(outcome is Outcome.Success)
        assertEquals(5L, (outcome as Outcome.Success).data?.id)
    }

    @Test
    fun `a read the database refuses is an error, not an empty answer`() = runTest {
        // The other half of the same distinction, and the one that used to be invisible: before
        // this, a thrown read came back as `null` — the same value as "no such note" — and the Note
        // screen opened a blank editor whose save wrote a *second* note beside the original.
        val repository = NoteRepositoryImpl(noteDao = BrokenNoteDao(), clock = CLOCK)

        val outcome = repository.getNote(id = 5L)

        assertTrue(outcome is Outcome.Error)
    }

    // ---------- Deleting ----------

    @Test
    fun `deleting a note takes it out of the emitted list`() = runTest {
        val dao = FakeNoteDao(rows = listOf(entity(id = 5L), entity(id = 6L)))
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)

        val outcome = repository.delete(note = repository.storedNote(id = 5L))

        assertTrue(outcome is Outcome.Success)
        // Both halves matter. The row is gone from the list a screen collects, and the *other* note
        // is untouched — a delete that took the wrong row, or every row, would satisfy the first
        // assertion on its own.
        assertEquals(listOf(6L), repository.notesFlow.first().map { note -> note.id })
    }

    @Test
    fun `a deleted note cannot be read back`() = runTest {
        val dao = FakeNoteDao(rows = listOf(entity(id = 5L)))
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)

        repository.delete(note = repository.storedNote(id = 5L))

        val outcome = repository.getNote(id = 5L)
        assertTrue(outcome is Outcome.Success)
        assertNull((outcome as Outcome.Success).data)
    }

    @Test
    fun `deleting a note that was never saved is refused, and nothing else goes with it`() = runTest {
        // A draft has `UNSAVED_ID`, which matches no row. Room's `@Delete` would remove nothing and
        // report no problem, so the screen above would announce a deletion that never happened —
        // about a note the user can still see. The refusal is what makes that impossible.
        val dao = FakeNoteDao(rows = listOf(entity(id = 5L)))
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)

        val outcome = repository.delete(note = Note.draft(date = TODAY, title = "Groceries"))

        assertTrue(outcome is Outcome.Error)
        assertEquals(listOf(5L), repository.notesFlow.first().map { note -> note.id })
    }

    @Test
    fun `deleting a note that is no longer there is reported, not announced as a success`() = runTest {
        // The general case of the refusal below, and the one that gets ordinary once a second
        // surface can delete: the id is a perfectly real one, it just does not match a row any
        // more. Room's `@Delete` matches nothing and reports nothing wrong, so without the row
        // count the screen would show "Note deleted" for a note that had already gone — or, worse
        // once the Calendar screens land, for one that is still sitting in another list.
        val dao = FakeNoteDao(rows = listOf(entity(id = 5L)))
        val repository = NoteRepositoryImpl(noteDao = dao, clock = CLOCK)
        val note = repository.storedNote(id = 5L)

        assertTrue(repository.delete(note = note) is Outcome.Success)
        val second = repository.delete(note = note)

        assertTrue(second is Outcome.Error)
    }

    @Test
    fun `a delete the database refuses is reported rather than thrown`() = runTest {
        val repository = NoteRepositoryImpl(noteDao = BrokenNoteDao(), clock = CLOCK)

        val outcome = repository.delete(
            note = Note(
                id = 5L,
                date = TODAY,
                title = "Groceries",
                content = "Coffee, oat milk",
                createdAt = 1_000L,
                updatedAt = 1_000L,
            ),
        )

        // A repository does not throw at the screen above it — the screen has to be able to tell
        // the user something, and an exception crossing this boundary lands in a ViewModel that is
        // catching nothing.
        assertTrue(outcome is Outcome.Error)
    }

    @Test
    fun `a database that will not be read shows an empty list instead of crashing the screen`() = runTest {
        // `notesFlow` is collected by a ViewModel that catches nothing, so an exception escaping
        // here would take the app down. "No notes" is the wrong answer and a survivable one.
        val notes = NoteRepositoryImpl(noteDao = BrokenNoteDao(), clock = CLOCK).notesFlow.first()

        assertEquals(emptyList<Note>(), notes)
    }

    private fun entity(
        id: Long,
        date: Long = TODAY.toEpochDay(),
        createdAt: Long = 1_000L,
        updatedAt: Long = 1_000L,
    ): NoteEntity = NoteEntity(
        id = id,
        date = date,
        title = "Groceries",
        content = "Coffee, oat milk",
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

    private companion object {

        /** The day every test in this file pretends it is. */
        private val TODAY: LocalDate = LocalDate.of(2026, 3, 14)

        /**
         * A clock stopped mid-morning on [TODAY].
         *
         * Mid-morning rather than midnight so that no assertion here can pass or fail on which
         * side of a day boundary the instant landed.
         */
        private val CLOCK: Clock = Clock.fixed(
            TODAY.atStartOfDay(ZoneOffset.UTC).plusHours(9L).toInstant(),
            ZoneOffset.UTC,
        )

        /** What [CLOCK] reads in epoch milliseconds — the stamp every save here should produce. */
        private val NOW_MILLIS: Long = CLOCK.millis()
    }
}

/**
 * A notes table that is really just a list held in memory.
 *
 * [observeAll] returns the rows **in the order they were handed to the constructor**, on purpose.
 * A fake that sorted would be agreeing with the code under test instead of checking it.
 *
 * One thing it does *not* emulate: Room's `autoGenerate`. [upsert] keeps whatever id it is given,
 * so two brand-new notes — both carrying [com.example.skeleton.domain.model.Note.UNSAVED_ID] —
 * would land on the same row here where the real table would hand out two. No test below saves
 * two new notes; a test that needs to will need a fake that counts.
 *
 * @param rows the rows the fake table starts with.
 * @author Phong-Kaster
 */
private class FakeNoteDao(rows: List<NoteEntity>) : NoteDao {

    private val storedRows = MutableStateFlow(rows)

    override fun observeAll() = storedRows

    override fun observeByDate(epochDay: Long) = MutableStateFlow(
        storedRows.value.filter { row -> row.date == epochDay },
    )

    override suspend fun getById(id: Long): NoteEntity? =
        storedRows.value.firstOrNull { row -> row.id == id }

    override suspend fun upsert(note: NoteEntity): Long {
        storedRows.value = storedRows.value.filterNot { row -> row.id == note.id } + note
        return note.id
    }

    override suspend fun delete(note: NoteEntity): Int {
        val before = storedRows.value
        storedRows.value = before.filterNot { row -> row.id == note.id }
        // The count Room would report, computed the same way Room computes it — how many rows the
        // primary key actually matched. Returning a constant `1` here would make the repository's
        // "a delete that removed nothing is an error" rule untestable, which is the fake agreeing
        // with the code instead of checking it.
        return before.size - storedRows.value.size
    }
}

/**
 * A notes table where every single thing fails.
 *
 * It exists because the repository's fail-soft promise — *never throw at the screen above you* — is
 * only a promise until something actually throws. There is no other way to reach those catch blocks
 * on this toolchain: the real failure is a corrupt database file or a disk that will not read, and
 * nothing here can produce either.
 *
 * [observeAll] throws on **collection** rather than when it is called, because
 * `NoteRepositoryImpl` builds its `notesFlow` in a property initialiser — a fake that threw from
 * the function itself would blow up in the constructor and never reach the code being tested.
 *
 * @author Phong-Kaster
 */
private class BrokenNoteDao : NoteDao {

    override fun observeAll(): Flow<List<NoteEntity>> = flow { throw IOException("disk is gone") }

    override fun observeByDate(epochDay: Long): Flow<List<NoteEntity>> =
        flow { throw IOException("disk is gone") }

    override suspend fun getById(id: Long): NoteEntity? = throw IOException("disk is gone")

    override suspend fun upsert(note: NoteEntity): Long = throw IOException("disk is gone")

    override suspend fun delete(note: NoteEntity): Int = throw IOException("disk is gone")
}

/**
 * The note behind a read the test expects to succeed.
 *
 * `NoteRepository.getNote` answers with an [Outcome] rather than a nullable note, because "it is
 * gone" and "I could not look" are different answers that call for different behaviour on screen.
 * Most tests in this file are about *saving* something they first read back, and unwrapping that
 * in-line five times would bury each assertion under ceremony.
 *
 * It asserts on the way through on purpose: a read that failed, or that found nothing, fails the
 * test here — at the read — rather than three lines later as a mystifying null.
 *
 * @param id the row id to read.
 * @author Phong-Kaster
 */
private suspend fun NoteRepositoryImpl.storedNote(id: Long): Note {
    val outcome = getNote(id = id)
    assertTrue("reading note $id should have succeeded", outcome is Outcome.Success)

    val note = (outcome as Outcome.Success).data
    assertNotNull("note $id should be in the store", note)
    return note!!
}
