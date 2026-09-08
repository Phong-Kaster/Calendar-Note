package com.example.skeleton.calendar

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Verifies the date-scoping contract of
 * [NoteRepository][com.example.skeleton.domain.repository.NoteRepository] using
 * [FakeNoteRepository]: a note added to one date is only returned for that date, and a
 * month's "has notes" set contains exactly the dates that actually have notes.
 *
 * @author Phong-Kaster
 */
class NoteDateScopingTest {

    @Test
    fun `a note added to one date is returned for that date and not for another`() = runBlocking {
        val repository = FakeNoteRepository()
        val noteDate = LocalDate.of(2026, 9, 8)
        val otherDate = LocalDate.of(2026, 9, 9)

        repository.addNote(noteDate, "Dentist appointment")

        val notesOnNoteDate = repository.observeByDate(noteDate).first()
        val notesOnOtherDate = repository.observeByDate(otherDate).first()

        assertEquals(1, notesOnNoteDate.size)
        assertEquals("Dentist appointment", notesOnNoteDate.first().title)
        assertTrue(notesOnOtherDate.isEmpty())
    }

    @Test
    fun `datesWithNotes for a month contains exactly the dates with notes, excluding adjacent months`() = runBlocking {
        val repository = FakeNoteRepository()
        val firstOfMonth = LocalDate.of(2026, 9, 1)
        val lastOfMonth = LocalDate.of(2026, 9, 30)
        val insideMonth = LocalDate.of(2026, 9, 15)
        val previousMonth = LocalDate.of(2026, 8, 31)
        val nextMonth = LocalDate.of(2026, 10, 1)

        repository.addNote(insideMonth, "Inside the month")
        repository.addNote(previousMonth, "Previous month, should be excluded")
        repository.addNote(nextMonth, "Next month, should be excluded")

        val datesWithNotes = repository.observeDatesWithNotesBetween(firstOfMonth, lastOfMonth).first()

        assertEquals(setOf(insideMonth), datesWithNotes)
    }

    @Test
    fun `an empty date returns an empty list`() = runBlocking {
        val repository = FakeNoteRepository()
        val emptyDate = LocalDate.of(2026, 9, 8)

        val notes = repository.observeByDate(emptyDate).first()

        assertTrue(notes.isEmpty())
    }
}
