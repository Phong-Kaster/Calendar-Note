package com.example.skeleton.domain.model

/**
 * One alarm the user set.
 *
 * Think of it as a sticky note with a time written on it: it says *what* to be reminded of
 * ([message]) and *when* in the day to say it ([hourOfDay] and [minute]).
 *
 * **The time is two plain numbers, not a `LocalTime`.** That is on purpose and it matches the way
 * [Note] keeps its day as an epoch day: the store keeps integers, so sorting and comparing a
 * time-of-day is integer work, and the one place that would ever need a real time type is the
 * mapper. A single time object here would push a type converter into the database for no gain.
 *
 * [enabled] is carried even though nothing reads it yet. It is here now because the column that
 * backs it is here now — adding a column to a shipped table costs a second Room migration, and a
 * migration in this app (no `fallbackToDestructiveMigration` call) is the one thing that turns a
 * mistake into a launch crash for every existing install. Cheaper to carry a field than to add a
 * column later.
 *
 * @param id row id. `0` means "not saved yet" — Room hands out the real one on insert. See
 *   [UNSAVED_ID].
 * @param message what the alarm is about. Never blank on a *stored* alarm: the store refuses a
 *   blank one, because an alarm with nothing written on it says nothing when it goes off.
 * @param hourOfDay the hour the alarm is set for, 0–23.
 * @param minute the minute past that hour, 0–59.
 * @param enabled whether the alarm is armed. True for a brand-new alarm — a user who just wrote one
 *   meant it.
 * @param createdAt when the alarm was first saved, in epoch milliseconds. [UNSAVED_AT] until the
 *   store stamps it.
 * @author Phong-Kaster
 */
data class Alarm(
    val id: Long = 0L,
    val message: String,
    val hourOfDay: Int,
    val minute: Int,
    val enabled: Boolean = true,
    val createdAt: Long,
) {

    companion object {

        /**
         * The [id] of an alarm that has never been stored.
         *
         * `0` and not `-1`, because `0` is the value Room reads as "give this row a fresh id"
         * (`@PrimaryKey(autoGenerate = true)`). Navigation uses `-1` for the same idea — see
         * `AlarmEditorFragment` — and translates to this one at the edge of the screen.
         */
        const val UNSAVED_ID = 0L

        /**
         * The [createdAt] of an alarm that has never been stored.
         *
         * The store recognises it and stamps the real clock reading in its place, which is what
         * makes "created" mean created: an alarm that already carries a stamp keeps it forever.
         */
        const val UNSAVED_AT = 0L

        /**
         * The hour a brand-new alarm starts on: eight in the morning.
         *
         * A fixed, ordinary hour rather than "whatever time it is right now". Two reasons. It keeps
         * this factory free of a clock — no screen in this app reads the clock, and a `draft()`
         * that did would be the first. And "now" is a poor default for an alarm: an alarm set for
         * the current minute is one the user has already missed, so it would need thinking about
         * before it were useful, while 08:00 is simply a time somebody might pick.
         */
        const val DEFAULT_HOUR_OF_DAY = 8

        /** The minute a brand-new alarm starts on — on the hour. See [DEFAULT_HOUR_OF_DAY]. */
        const val DEFAULT_MINUTE = 0

        /**
         * An alarm the user has begun writing and has not saved yet.
         *
         * [createdAt] is left [UNSAVED_AT] on purpose: **no screen stamps an alarm.** The store
         * does, so that two alarms written from two different screens cannot end up stamped by two
         * different clocks.
         *
         * @param message what the alarm should say; may be blank while the user is still typing.
         *   The *store* is what refuses a blank one, at save time — a draft is allowed to be empty,
         *   or the editor could not open at all.
         * @param hourOfDay the hour to start on; defaults to [DEFAULT_HOUR_OF_DAY].
         * @param minute the minute to start on; defaults to [DEFAULT_MINUTE].
         * @author Phong-Kaster
         */
        fun draft(
            message: String = "",
            hourOfDay: Int = DEFAULT_HOUR_OF_DAY,
            minute: Int = DEFAULT_MINUTE,
        ): Alarm = Alarm(
            id = UNSAVED_ID,
            message = message,
            hourOfDay = hourOfDay,
            minute = minute,
            enabled = true,
            createdAt = UNSAVED_AT,
        )
    }
}
