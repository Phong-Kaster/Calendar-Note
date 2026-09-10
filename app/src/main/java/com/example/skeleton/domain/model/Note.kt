package com.example.skeleton.domain.model

import java.time.LocalDate

/**
 * One note the user wrote.
 *
 * Think of it as a page in a paper notebook: it belongs to one day ([date]), it has a heading
 * ([title]) that the user is allowed to leave empty, and it has the writing itself ([content]).
 *
 * There are two timestamps and the difference between them matters. [createdAt] is set once and
 * never moves again. [updatedAt] moves every time the note is saved, and it is the key every list
 * in this app sorts by — "most recently touched first". Sorting by [createdAt] instead looks
 * exactly right on a fresh database and quietly stops being right the first time somebody edits an
 * old note.
 *
 * @param id row id. `0` means "not saved yet" — Room hands out the real one on insert.
 * @param date the day this note belongs to, in the device's own calendar. Never a future day.
 * @param title the heading. May be blank; see [displayTitle].
 * @param content the body of the note. May also be blank.
 * @param createdAt when the note was first saved, in epoch milliseconds.
 * @param updatedAt when the note was last saved, in epoch milliseconds.
 * @author Phong-Kaster
 */
data class Note(
    val id: Long = 0L,
    val date: LocalDate,
    val title: String,
    val content: String,
    val createdAt: Long,
    val updatedAt: Long,
) {

    /**
     * The one line a list row writes at the top of the note.
     *
     * The title when there is one. When there is not, the first line of the body that actually has
     * words on it — so a note the user began with a blank line still shows its own text instead of
     * showing nothing at all.
     *
     * Blank when the note has neither a title nor a body. Deciding what to draw in that case is the
     * caller's job, because a placeholder like "Untitled note" is user-facing copy and this is a
     * domain model with no access to string resources.
     *
     * It is computed here, and not inside the row composable, so that an ordinary JVM test can
     * check it without rendering anything.
     *
     * @author Phong-Kaster
     */
    val displayTitle: String =
        if (title.isNotBlank()) title.trim()
        else content.lineSequence().firstOrNull(predicate = { line -> line.isNotBlank() })?.trim().orEmpty()
}
