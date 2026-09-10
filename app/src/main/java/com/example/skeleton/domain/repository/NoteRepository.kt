package com.example.skeleton.domain.repository

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Note
import kotlinx.coroutines.flow.Flow

/**
 * The app's store of notes.
 *
 * Everything above this line — ViewModels, screens — asks for notes through this interface and
 * never talks to the database directly. That is what lets a plain JVM test hand a ViewModel a fake
 * store, and what keeps Room out of `domain/`.
 *
 * @author Phong-Kaster
 */
interface NoteRepository {

    /**
     * Every note in the app, most recently touched first.
     *
     * A live stream, not a one-off read: saving or deleting a note makes this emit again on its
     * own, so a screen collecting it stays current without asking.
     *
     * "Most recently touched" means ordered by `updatedAt` descending — creating **or** editing a
     * note moves it back to the top. This ordering is a promise of the store itself, not of the
     * screen that happens to display it.
     */
    val notesFlow: Flow<List<Note>>

    /**
     * One note by its row id, or `null` when there is no such note.
     *
     * `null` is not an error here — asking for a note that has since been deleted is an ordinary
     * thing for a screen to do, and it gets an ordinary answer.
     *
     * @param id the row id.
     */
    suspend fun getNote(id: Long): Note?

    /**
     * Writes a note, creating it when it has no id yet and replacing it when it does.
     *
     * **The store owns the clock, not the caller.** A note handed over with
     * [Note.UNSAVED_AT] timestamps is stamped with the current time on both; a note that already
     * carries a `createdAt` keeps it and only has its `updatedAt` moved forward. That is what makes
     * "most recently touched first" mean what it says, and it is why no screen in this app is
     * allowed to read the clock for itself — two screens with two clocks produce a list order
     * nobody can explain.
     *
     * **The store also owns the calendar.** A note dated after today is refused here, below every
     * screen, so that a caller cannot get around the rule by not offering the affordance. See
     * `knowledge/DOMAIN.md`.
     *
     * Returns a value rather than throwing: [Outcome.Success] when the note is stored,
     * [Outcome.Error] when it is refused or the write fails. [Outcome.Loading] is never emitted —
     * this is a one-shot write, not a stream.
     *
     * @param note the note to store.
     * @return whether the note was stored.
     */
    suspend fun save(note: Note): Outcome<Unit>
}
