package com.example.skeleton.ui.fragment.note.model

/**
 * Something went wrong on the Note screen, and this says which thing.
 *
 * Think of it as the reason on a slip of paper the screen hands to the Fragment: the Fragment reads
 * it, shows the matching message, and throws the slip away (`NoteViewModel.consumeProblem()`). It
 * is not a state the screen sits in — that is why it is cleared the moment it has been shown, and
 * why there is no message text in here. **Wording is user-facing copy and belongs in
 * `strings.xml`**, so this carries the reason and the Fragment picks the words.
 *
 * Why an enum and not a `Boolean` per case: the three below need *different* messages, and two of
 * them mean the screen cannot go on at all. A pile of booleans would let two be true at once, which
 * is a state with no sensible message.
 *
 * @author Phong-Kaster
 */
enum class NoteProblem {

    /**
     * The note this screen was opened for is not in the store any more — most likely deleted from
     * somewhere else while a stale list row still pointed at it.
     *
     * There is nothing to edit, so the screen leaves. It must **not** fall through to a blank
     * editor: a save from there writes a brand-new note, and the user ends up with a duplicate of
     * the thing they thought they were editing.
     */
    Gone,

    /**
     * The store could not be read, so whether the note exists is unknown.
     *
     * Different from [Gone] on purpose. "It is gone" is a fact worth telling the user plainly;
     * "I could not look" is a failure, and answering it by opening an empty note would be the
     * screen inventing an answer it does not have.
     */
    Unreadable,

    /** The note is real and could not be removed. The user stays on the screen with it. */
    DeleteFailed,
}
