package com.example.skeleton.domain.repository

import com.example.skeleton.domain.model.Note
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

/**
 * I keep track of the user's calendar notes, one date at a time.
 *
 * @author Phong-Kaster
 */
interface NoteRepository {

    /**
     * All notes for [date], oldest first.
     *
     * @param date The date to scope notes to.
     */
    fun observeByDate(date: LocalDate): Flow<List<Note>>

    /**
     * Every date between [start] and [end] (inclusive) that has at least one note.
     *
     * Drives the "has notes" marker for a visible month grid.
     *
     * @param start First date of the range, inclusive.
     * @param end Last date of the range, inclusive.
     */
    fun observeDatesWithNotesBetween(start: LocalDate, end: LocalDate): Flow<Set<LocalDate>>

    /**
     * Adds a new note titled [title] on [date].
     *
     * A blank or whitespace-only title is silently ignored (no exception, no error state) —
     * I just do nothing in that case, like a kid who won't write down an empty sticky note.
     *
     * @param date The date this note belongs to.
     * @param title What the user wrote.
     */
    suspend fun addNote(date: LocalDate, title: String)
}
