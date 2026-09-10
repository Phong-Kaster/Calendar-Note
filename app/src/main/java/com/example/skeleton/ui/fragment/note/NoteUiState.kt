package com.example.skeleton.ui.fragment.note

import java.time.LocalDate

/**
 * Everything the Note screen needs to draw itself.
 *
 * Notice what is *not* here: the note's row id, and its `createdAt`. Neither is ever drawn, so
 * neither belongs in UI state — the ViewModel keeps them privately and hands them back to the
 * store at save time. UI state is what a screen paints, not everything a screen knows.
 *
 * There is also no `error` field. A save that fails is a passing event, not a state the screen
 * sits in, so it arrives as [saveFailed] and is cleared the moment it has been shown.
 *
 * @param date the day this note belongs to. Defaults to today, which is what a brand-new note
 *   opened from Home's centre button gets; the Calendar screen will open the same editor on a past
 *   day instead.
 * @param title the heading being edited. May be left blank — a note with no title is a normal
 *   note, and nothing here blocks saving one.
 * @param content the body being edited. May also be blank.
 * @param savedTrigger incremented once each time a save succeeds. The Fragment watches it and
 *   leaves the screen; a counter rather than a flag because it is an event, and an event that
 *   happens twice must be visible twice.
 * @param saveFailed true when the last save was refused or failed. The Fragment shows a message
 *   and calls `consumeSaveFailed()`, which puts it back to false.
 * @author Phong-Kaster
 */
data class NoteUiState(
    val date: LocalDate = LocalDate.now(),
    val title: String = "",
    val content: String = "",
    val savedTrigger: Int = 0,
    val saveFailed: Boolean = false,
)
