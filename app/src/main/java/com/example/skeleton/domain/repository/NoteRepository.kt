package com.example.skeleton.domain.repository

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
}
