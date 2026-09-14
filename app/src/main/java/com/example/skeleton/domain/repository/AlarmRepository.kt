package com.example.skeleton.domain.repository

import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.model.BlankAlarmMessageException
import kotlinx.coroutines.flow.Flow

/**
 * The app's store of alarms.
 *
 * Everything above this line — ViewModels, screens — asks for alarms through this interface and
 * never talks to the database directly. That is what lets a plain JVM test hand a ViewModel a fake
 * store, and what keeps Room out of `domain/`.
 *
 * @author Phong-Kaster
 */
interface AlarmRepository {

    /**
     * Every alarm in the app, earliest time of day first.
     *
     * A live stream, not a one-off read: saving an alarm makes this emit again on its own, so a
     * screen collecting it stays current without asking.
     *
     * "Earliest first" means ordered by hour, then by minute, then by row id. The list is a
     * *clock face*, not a history — a user scanning their alarms is looking for "what happens in
     * the morning", so creation order would be the wrong answer even though it is the order the
     * rows were written in. The `id` behind it is not decoration: two alarms set for the same
     * minute would otherwise be free to swap places between emissions, which is the kind of bug
     * that reproduces once a week and never in a test.
     *
     * This ordering is a promise of the store itself, not of the screen that happens to display it.
     */
    val alarmsFlow: Flow<List<Alarm>>

    /**
     * One alarm by its row id.
     *
     * **There are three answers here, not two, and keeping them apart is the whole point of the
     * return type.**
     *
     * - [Outcome.Success] carrying the alarm — here it is.
     * - [Outcome.Success] carrying `null` — there is no such alarm. Not an error: asking for an
     *   alarm that has since been removed is an ordinary thing for a screen to do, and it gets an
     *   ordinary answer.
     * - [Outcome.Error] — the store could not be read, so whether the alarm exists is **unknown**.
     *
     * The notes store learned this the hard way: a single nullable return collapsed the last two
     * into one `null`, the screen above guessed "start a new one", and a failed read opened a blank
     * editor whose save wrote a *second* row beside the untouched original. See
     * `NoteRepository.getNote`. The same shape is used here so the same mistake cannot be made
     * twice.
     *
     * [Outcome.Loading] is never returned — this is a one-shot read, not a stream.
     *
     * @param id the row id.
     * @return the alarm, the absence of an alarm, or the failure to find out.
     */
    suspend fun getAlarm(id: Long): Outcome<Alarm?>

    /**
     * Writes an alarm, creating it when it has no id yet and replacing it when it does.
     *
     * **The store owns the clock, not the caller.** An alarm handed over with [Alarm.UNSAVED_AT]
     * is stamped with the current time; an alarm that already carries a `createdAt` keeps it. That
     * is why no screen in this app is allowed to read the clock for itself.
     *
     * **The store also owns the "an alarm must say something" rule.** An alarm whose message is
     * blank is refused here, below every screen, so that a caller cannot get around the rule by
     * not offering the affordance. An alarm with nothing written on it would go off saying nothing.
     *
     * Returns a value rather than throwing: [Outcome.Success] when the alarm is stored,
     * [Outcome.Error] when it is refused or the write fails. [Outcome.Loading] is never emitted —
     * this is a one-shot write, not a stream.
     *
     * **The two kinds of [Outcome.Error] are told apart by type, never by message.** A refusal
     * carries a [BlankAlarmMessageException] in `throwable`; a write that genuinely failed carries
     * whatever the database threw. A caller has to be able to tell them apart, because retrying a
     * refusal can never succeed — the message is as blank as it was a moment ago — while retrying a
     * failed write often can. Matching on the message text would work until somebody rephrased it.
     *
     * @param alarm the alarm to store.
     * @return whether the alarm was stored.
     */
    suspend fun save(alarm: Alarm): Outcome<Unit>

    /**
     * Removes an alarm for good. [alarmsFlow] emits again without it, with nobody having to ask.
     *
     * **A delete that removed nothing is an [Outcome.Error], not a success.** Room matches on the
     * primary key and is perfectly content to match no row and report nothing wrong — so without
     * this, the screen above would say "deleted" about an alarm that is still sitting in the list,
     * or about one that went a moment ago. Two ways of having no row are refused, and they are
     * separate on purpose: an alarm carrying [Alarm.UNSAVED_ID] was never stored at all (a caller
     * mistake — a draft cannot be deleted), while any *other* id that matches nothing is an ordinary
     * race with the world. Both get a refusal; neither gets a silent no-op. Same shape, same
     * reasoning, as `NoteRepository.delete`.
     *
     * **There is no separate "switch this alarm off" here, and that is deliberate.** Turning an
     * alarm on or off is an ordinary edit of [Alarm.enabled] — hand [save] the same alarm with the
     * flag flipped. A second write method would be a second place for the write rules (the
     * blank-message refusal, the creation stamp) to be applied, and the second place is the one that
     * gets missed.
     *
     * Returns a value rather than throwing, like [save]. [Outcome.Loading] is never returned.
     *
     * @param alarm the alarm to remove. Only its [Alarm.id] decides which row goes.
     * @return whether the alarm was removed.
     */
    suspend fun delete(alarm: Alarm): Outcome<Unit>
}
