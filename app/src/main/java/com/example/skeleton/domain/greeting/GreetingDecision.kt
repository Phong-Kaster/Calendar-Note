package com.example.skeleton.domain.greeting

import java.time.Clock
import java.time.LocalDate
import java.time.format.DateTimeParseException

/**
 * Has the user already been greeted today — and if not, which day should be written down as the day
 * they were greeted?
 *
 * **Why this is a plain function and not a method on something Android-shaped.** The promise of the
 * feature is "one greeting a day, and exactly one". Nothing in this project can watch a real
 * notification arrive: unit tests run against a stub `android.jar` where every framework call
 * quietly answers `0` / `false` / `null`, and there is no emulator and no Robolectric. A test aimed
 * at `NotificationManager` would go green while the app greeted the user five times before
 * breakfast. So the one decision that carries the promise lives here, in `domain/`, with no Android
 * in sight, and is checked by an ordinary JVM test. `GreetingNotifier` is then a thin arm that only
 * has to ask this function and do as it is told. This is the same shape as
 * `domain/scheduler/NextFireTime.kt`.
 *
 * **Why the answer is a date and not a `Boolean`.** The caller needs two things — *whether* to greet,
 * and *which day to remember* so that it does not greet again in ten minutes. If this returned a
 * `Boolean`, the caller would have to read the clock a **second** time to work out what to store, and
 * a foreground at 23:59:59.999 could then be decided against one day and recorded against the next —
 * a greeting the user never sees again until the day after tomorrow. One reading of the clock, one
 * answer, no way for the two to disagree. `null` means "already greeted today, do nothing".
 *
 * **The comparison is by calendar day, never by elapsed hours.** Opening the app at 23:58 and again
 * at 00:02 is four minutes apart and is *two different days*, so both greet. Opening it at 00:02 and
 * again at 23:58 is nearly twenty-four hours apart and is *one* day, so only the first greets. A
 * rolling "has it been 24 hours" timer gets both of these backwards, and would slowly walk the
 * greeting later and later through the day.
 *
 * @param lastGreetedDate the day the user was last greeted, as it was read back out of storage, or
 *   `null` when they have never been greeted (a fresh install) or when what was stored could not be
 *   read — see [parseGreetedDate].
 * @param clock where "today" comes from. A parameter and never `LocalDate.now()` read inside,
 *   because every promise above is only checkable if a test can decide what day it is.
 * @return today's date when a greeting is due — **and that is the date the caller must store** — or
 *   `null` when the user has already been greeted today.
 * @author Phong-Kaster
 */
fun greetingDueOn(lastGreetedDate: LocalDate?, clock: Clock): LocalDate? {
    val today = LocalDate.now(clock)

    // `==` on two `LocalDate`s is a day-for-day comparison: same year, same month, same day number.
    // No time of day is involved at all, which is exactly what "once per calendar day" means.
    if (lastGreetedDate == today) return null

    // Everything else greets: never greeted (`null`), greeted on an earlier day, and also greeted on
    // a *later* day — which sounds impossible but is what a user who set their phone's clock back
    // leaves behind. Greeting once too often is a friendly message; never greeting again until the
    // calendar catches up would be a feature that silently died.
    return today
}

/**
 * Turns what was found in storage back into a date.
 *
 * The day is kept on disk as text — `"2026-03-14"` — so it survives the app being killed. Text can
 * be missing (nobody has ever been greeted), empty, or nonsense (a half-written file, or a value
 * left behind by an older version of the app that stored something else under the same name). All
 * three mean the same thing here: **we do not know when the user was last greeted**, so the answer is
 * `null` and [greetingDueOn] will greet them. Greeting somebody twice is a small annoyance; throwing
 * an exception out of a storage read is a crash on app start.
 *
 * @param stored exactly what came back out of storage, `null` when nothing was ever written.
 * @return the date that text describes, or `null` when it describes nothing usable.
 * @author Phong-Kaster
 */
fun parseGreetedDate(stored: String?): LocalDate? {
    if (stored.isNullOrBlank()) return null

    return try {
        // ISO-8601 — `LocalDate.parse` reads exactly the format `LocalDate.toString()` writes, which
        // is the whole reason these two functions are a pair and sit next to each other.
        LocalDate.parse(stored)
    } catch (e: DateTimeParseException) {
        // Deliberately silent and deliberately not logged: `android.util.Log` is an Android type and
        // this file is Android-free so that it stays testable. The recovery is complete — the caller
        // greets, and writes a good value over the bad one.
        null
    }
}

/**
 * Turns a date into the text that goes into storage.
 *
 * `LocalDate.toString()` is ISO-8601 by contract — `"2026-03-14"`, always ten characters, always
 * that order — so it sorts correctly as text, reads correctly to a human looking at the preferences
 * file, and is read back by [parseGreetedDate] without a formatter having to be kept in step.
 *
 * It exists as a named function rather than a bare `date.toString()` at the call site so that the
 * choice of format lives in **one** place. The day the two sides disagree about the format is the day
 * the user is greeted on every single app launch, forever.
 *
 * @param date the day to remember.
 * @return that day as ISO-8601 text.
 * @author Phong-Kaster
 */
fun formatGreetedDate(date: LocalDate): String = date.toString()
