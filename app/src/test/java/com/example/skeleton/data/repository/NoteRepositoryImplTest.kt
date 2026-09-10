package com.example.skeleton.data.repository

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
import org.junit.Test
import java.time.LocalDate

/**
 * Holds [NoteRepositoryImpl] to the promise its interface makes: the notes come out most recently
 * touched first, whatever order they went in.
 *
 * The DAO underneath is a fake — a list in memory, no Room, no Android, no device. That is the
 * point of the fake rather than a convenience: the real DAO sorts in SQL, and a SQL `ORDER BY` can
 * only be checked by running SQLite, which needs an emulator this project's test setup does not
 * have. So the fake deliberately hands back a **jumbled** list. If the repository were relying on
 * the database to sort for it, these tests would fail, which is exactly what they exist to notice.
 *
 * The ordering rule itself is not this file's invention: `knowledge/DOMAIN.md` states it, including
 * the trap it is there to prevent — ordering by `createdAt` looks perfect on a fresh database and
 * stops being right the first time somebody edits an old note. The second test below is that trap,
 * written down.
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

    private fun entity(
        id: Long,
        date: Long = LocalDate.of(2026, 3, 14).toEpochDay(),
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
}

/**
 * A notes table that is really just a list held in memory.
 *
 * [observeAll] returns the rows **in the order they were handed to the constructor**, on purpose.
 * A fake that sorted would be agreeing with the code under test instead of checking it.
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
