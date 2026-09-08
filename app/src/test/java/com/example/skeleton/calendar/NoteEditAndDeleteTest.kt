package com.example.skeleton.calendar

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Verifies edit and delete on
 * [NoteRepository][com.example.skeleton.domain.repository.NoteRepository] using
 * [FakeNoteRepository]: editing keeps the note on its original date, deleting removes it, and
 * deleting an unknown id is a no-op.
 *
 * @author Phong-Kaster
 */
class NoteEditAndDeleteTest {

    @Test
    fun `editing a note changes its title but keeps its date`() = runBlocking {
        val repository = FakeNoteRepository()
        val date = LocalDate.of(2026, 9, 8)

        repository.addNote(date, "Dentist appointment")
        val noteId = repository.observeByDate(date).first().first().id

        repository.update(noteId, "Dentist appointment - rescheduled")

        val notesOnDate = repository.observeByDate(date).first()

        assertEquals(1, notesOnDate.size)
        assertEquals("Dentist appointment - rescheduled", notesOnDate.first().title)
    }

    @Test
    fun `deleting a note removes it from that date's list`() = runBlocking {
        val repository = FakeNoteRepository()
        val date = LocalDate.of(2026, 9, 8)

        repository.addNote(date, "Buy birthday gift")
        val noteId = repository.observeByDate(date).first().first().id

        repository.delete(noteId)

        val notesOnDate = repository.observeByDate(date).first()

        assertTrue(notesOnDate.isEmpty())
    }

    @Test
    fun `deleting an unknown id is a no-op`() = runBlocking {
        val repository = FakeNoteRepository()
        val date = LocalDate.of(2026, 9, 8)

        repository.addNote(date, "Dentist appointment")

        repository.delete(id = 999_999L)

        val notesOnDate = repository.observeByDate(date).first()

        assertEquals(1, notesOnDate.size)
        assertEquals("Dentist appointment", notesOnDate.first().title)
    }
}
