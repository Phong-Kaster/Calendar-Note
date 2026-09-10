package com.example.skeleton.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

/**
 * Pins [Note.displayTitle] — the one line a list row writes at the top of a note.
 *
 * Why this is a test and not a glance at the code: the rule has three cases and only one of them
 * is the obvious one. A note **with** a title shows it; that case never breaks. The two that break
 * are a note with no title (must fall back to the body) and a note with nothing at all (must not
 * pretend it has content). Both of those are reachable the moment a user taps "new note" and saves
 * without typing a heading, which is the normal way to use a note app.
 *
 * The property lives on the model rather than inside the row composable precisely so it can be
 * checked here, with no screen and no Android runtime.
 *
 * @author Phong-Kaster
 */
class NoteTest {

    @Test
    fun `title wins when there is one`() {
        val note = note(title = "Groceries", content = "Coffee, oat milk")

        assertEquals("Groceries", note.displayTitle)
    }

    @Test
    fun `blank title falls back to the first line of the body`() {
        val note = note(title = "", content = "Call the dentist\nAsk about Thursday")

        assertEquals("Call the dentist", note.displayTitle)
    }

    @Test
    fun `a title of only spaces counts as blank`() {
        val note = note(title = "   ", content = "Call the dentist")

        assertEquals("Call the dentist", note.displayTitle)
    }

    @Test
    fun `a body that starts with empty lines falls back to its first written line`() {
        val note = note(title = "", content = "\n\n  Call the dentist  \nAsk about Thursday")

        assertEquals("Call the dentist", note.displayTitle)
    }

    @Test
    fun `a note with neither title nor body has no display title`() {
        val note = note(title = "", content = "")

        assertEquals("", note.displayTitle)
    }

    /**
     * A note with the fields under test spelled out and everything else fixed, so each test reads
     * as the one thing it is checking.
     */
    private fun note(
        title: String,
        content: String,
    ): Note = Note(
        id = 1L,
        date = LocalDate.of(2026, 3, 14),
        title = title,
        content = content,
        createdAt = 1_773_000_000_000L,
        updatedAt = 1_773_000_000_000L,
    )
}
