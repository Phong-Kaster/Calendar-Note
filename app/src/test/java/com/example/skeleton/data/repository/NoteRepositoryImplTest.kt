package com.example.skeleton.data.repository

import com.example.skeleton.common.Outcome
import com.example.skeleton.data.database.local.dao.NoteDao
import com.example.skeleton.data.database.local.entity.NoteEntity
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mapper.toEntity
import com.example.skeleton.data.repository.impl.NoteRepositoryImpl
import com.example.skeleton.domain.model.Note
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Holds [NoteRepositoryImpl] to the two promises its interface makes: notes come out most recently
 * touched first whatever order they went in, and a note dated after today never goes in at all.
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

        val existing = repository.getNote(id = 5L)!!
        val outcome = repository.save(note = existing.copy(title = "Groceries, again"))

        assertTrue(outcome is Outcome.Success)
        val stored = repository.getNote(id = 5L)!!
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
            note = repository.getNote(id = 10L)!!.copy(content = "Coffee, oat milk, and bread"),
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

        val outcome = repository.save(note = repository.getNote(id = 5L)!!.copy(title = ""))

        assertTrue(outcome is Outcome.Success)
        val stored = repository.getNote(id = 5L)!!
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

        val existing = repository.getNote(id = 5L)!!
        val outcome = repository.save(note = existing.copy(date = TODAY.plusDays(1L)))

        assertTrue(outcome is Outcome.Error)
        assertEquals(TODAY, repository.getNote(id = 5L)!!.date)
    }

    // ---------- Reading one note ----------

    @Test
    fun `asking for a note that is not there is answered with null, not an error`() = runTest {
        val repository = NoteRepositoryImpl(noteDao = FakeNoteDao(rows = emptyList()), clock = CLOCK)

        assertNull(repository.getNote(id = 404L))
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

    override suspend fun delete(note: NoteEntity) {
        storedRows.value = storedRows.value.filterNot { row -> row.id == note.id }
    }
}
