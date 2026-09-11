package com.example.skeleton.domain.repository

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.FutureDateRefusedException
import com.example.skeleton.domain.model.Note
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

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
     * The notes written on one day, most recently touched first.
     *
     * A live stream like [notesFlow], and filtered by the store rather than by the screen. That
     * placement is the point: a screen that collected every note and filtered the list itself
     * would be carrying the whole notebook across the app to read one page of it, and — worse —
     * would own a rule the store is supposed to own. Ask for a day, get that day.
     *
     * Ordered by `updatedAt` descending, exactly like [notesFlow]. `knowledge/DOMAIN.md` says a
     * per-day list follows the same ordering as any other list of notes unless a criterion says
     * otherwise, and none does.
     *
     * A day with nothing on it emits an **empty list**, not an error and not nothing at all. The
     * screen above has to be able to tell "this day is empty" from "the answer has not arrived",
     * because those two look identical if the second one is drawn as a blank space.
     *
     * @param date the day to read.
     */
    fun notesForDateFlow(date: LocalDate): Flow<List<Note>>

    /**
     * One note by its row id.
     *
     * **There are three answers here, not two, and keeping them apart is the whole point of the
     * return type.**
     *
     * - [Outcome.Success] carrying the note — here it is.
     * - [Outcome.Success] carrying `null` — there is no such note. Not an error: asking for a note
     *   that has since been deleted is an ordinary thing for a screen to do, and it gets an
     *   ordinary answer.
     * - [Outcome.Error] — the store could not be read, so whether the note exists is **unknown**.
     *
     * A single nullable return used to collapse the last two into one `null`, and the screen above
     * had no choice but to guess. It guessed "start a new note", so a failed read opened a blank
     * editor whose save wrote a *second* note beside the untouched original. That is why the two
     * cases are separate values now: they call for different behaviour, and only the caller can
     * decide what.
     *
     * [Outcome.Loading] is never returned — this is a one-shot read, not a stream.
     *
     * @param id the row id.
     * @return the note, the absence of a note, or the failure to find out.
     */
    suspend fun getNote(id: Long): Outcome<Note?>

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
     * **The two kinds of [Outcome.Error] are told apart by type, never by message.** A refusal
     * carries a [FutureDateRefusedException] in `throwable`, naming the day that was refused and
     * the day it was compared against; a write that genuinely failed carries whatever the database
     * threw. A caller has to be able to tell them apart, because retrying a refusal can never
     * succeed — the note is dated the same day it was a moment ago — while retrying a failed write
     * often can. Matching on the message text would work until somebody rephrased it.
     *
     * @param note the note to store.
     * @return whether the note was stored.
     */
    suspend fun save(note: Note): Outcome<Unit>

    /**
     * Removes a note for good. [notesFlow] emits again without it, with nobody having to ask.
     *
     * **A delete that removed nothing is an [Outcome.Error], not a success.** Room matches on the
     * primary key and is perfectly content to match no row and report nothing wrong — so without
     * this, the screen above would say "deleted" about a note that is still sitting there, or
     * about one that went a moment ago on another surface. Two ways of having no row are refused,
     * and they are separate on purpose: a note carrying [Note.UNSAVED_ID] was never stored at all
     * (a caller mistake — a draft cannot be deleted), while any *other* id that matches nothing is
     * an ordinary race with the world. Both get a refusal; neither gets a silent no-op.
     *
     * Returns a value rather than throwing, like [save]. [Outcome.Loading] is never returned.
     *
     * @param note the note to remove. Only its [Note.id] decides which row goes.
     * @return whether the note was removed.
     */
    suspend fun delete(note: Note): Outcome<Unit>
}
