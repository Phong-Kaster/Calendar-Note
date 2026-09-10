package com.example.skeleton.ui.fragment.note

import com.example.skeleton.ui.fragment.note.model.NoteProblem
import java.time.LocalDate

/**
 * Everything the Note screen needs to draw itself.
 *
 * Notice what is *not* here: the note's row id, and its `createdAt`. Neither is ever drawn, so
 * neither belongs in UI state — the ViewModel keeps them privately and hands them back to the
 * store at save time. UI state is what a screen paints, not everything a screen knows.
 *
 * There is also no `error` field. A save that fails is a passing event, not a state the screen
 * sits in, so it arrives as [saveFailed] and is cleared the moment it has been shown; [problem]
 * works the same way and carries a reason rather than a message.
 *
 * @param date the day this note belongs to. Defaults to today, which is what a brand-new note
 *   opened from Home's centre button gets; the Calendar screen will open the same editor on a past
 *   day instead.
 * @param title the heading being edited. May be left blank — a note with no title is a normal
 *   note, and nothing here blocks saving one.
 * @param content the body being edited. May also be blank.
 * @param deletable true exactly while there is a **stored** note behind this screen — so, from the
 *   moment one is loaded until it has been deleted. It is what hides the delete action on a note
 *   that does not exist yet: there is nothing to delete, and a control that is present but does
 *   nothing is worse than one that is not there. Hidden rather than disabled, because a greyed-out
 *   control on a brand-new note invites the user to work out what they did wrong. It also gates
 *   `NoteViewModel.askToDelete()`, so the guarantee does not depend on the top bar remembering to
 *   read this field.
 * @param confirmingDelete true while the confirmation is on screen. Deleting is the one
 *   irreversible thing this app does, so this flag sits between the delete action and the store —
 *   see `NoteViewModel.askToDelete()`, which cannot itself delete anything.
 * @param savedTrigger incremented once each time a save succeeds. The Fragment watches it and
 *   leaves the screen; a counter rather than a flag because it is an event, and an event that
 *   happens twice must be visible twice.
 * @param deletedTrigger incremented once each time a delete succeeds. Separate from
 *   [savedTrigger] because the two exits are not the same event: one leaves a note behind and the
 *   other does not, and the user is told a different thing about each.
 * @param saveFailed true when the last save was refused or failed. The Fragment shows a message
 *   and calls `consumeSaveFailed()`, which puts it back to false.
 * @param problem non-null when the screen hit one of the situations in [NoteProblem] — the note is
 *   gone, unreadable, or would not delete. The Fragment shows the matching message and calls
 *   `consumeProblem()`.
 * @author Phong-Kaster
 */
data class NoteUiState(
    val date: LocalDate = LocalDate.now(),
    val title: String = "",
    val content: String = "",

    // --- UI control ---
    val deletable: Boolean = false,
    val confirmingDelete: Boolean = false,

    // --- One-shot events the Fragment consumes ---
    val savedTrigger: Int = 0,
    val deletedTrigger: Int = 0,
    val saveFailed: Boolean = false,
    val problem: NoteProblem? = null,
)
