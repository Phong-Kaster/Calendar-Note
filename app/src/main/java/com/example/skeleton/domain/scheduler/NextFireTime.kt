package com.example.skeleton.domain.scheduler

import com.example.skeleton.domain.enums.AlarmRepeatMode
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDateTime

/**
 * When does an alarm set for `hourOfDay:minute` next go off?
 *
 * **Why this is a plain function and not a method on something Android-shaped.** The whole point of
 * an alarm is that it goes off at the right moment, and on this toolchain `AlarmManager` is a stub
 * that quietly returns nothing — a test aimed at it passes without proving anything. So the one
 * piece of arithmetic that decides *when* lives here, in `domain/`, with no Android in sight, and is
 * checked by an ordinary JVM test. `AlarmManagerAlarmScheduler` is then a thin arm that only has to
 * hand this number to the system.
 *
 * **The rule: the answer is always strictly in the future.** If today's occurrence has already gone
 * by — or is *exactly* this instant — the answer is tomorrow's.
 *
 * That "exactly now counts as gone" half is the whole reason this function has a test of its own.
 * A `setExactAndAllowWhileIdle` alarm fires **once**; nothing repeats it. The only thing that makes
 * the alarm daily is `AlarmReceiver` arming the next one the moment it fires — and at that moment
 * "now" *is* the alarm's time, to the millisecond. Answer "today" there and the app arms an alarm
 * for the instant that has just passed, the system fires it immediately, the receiver notifies
 * again and arms again: the user gets the same reminder twice (or in a bad case, over and over).
 * Answer "tomorrow" and the next occurrence is exactly twenty-four hours later at the same
 * wall-clock time, which is precisely what "repeats daily" means.
 *
 * The cost of the choice, stated plainly: an alarm saved at *exactly* its own time — say the user
 * taps save at 09:00:00.000 on an alarm set for 09:00 — waits until tomorrow instead of going off
 * that same instant. That is the right way round. An alarm the user has already missed by a
 * millisecond is not one they want shouting at them while their finger is still on the button.
 *
 * **[AlarmRepeatMode.CUSTOM] is a different question, answered below the shared rule.** [repeatMode]
 * defaults to [AlarmRepeatMode.DAILY] and [repeatDays] to empty, so every call site written before
 * this parameter existed keeps behaving exactly as it did — [ONE_TIME][AlarmRepeatMode.ONE_TIME] and
 * [DAILY][AlarmRepeatMode.DAILY] both answer "the next occurrence of this time of day", because the
 * *first* occurrence of a one-time alarm is found the same way a daily one's is; the difference
 * between the two is entirely in whether `AlarmReceiver` asks this function again after it fires.
 *
 * Under [AlarmRepeatMode.CUSTOM], the answer must additionally land on a day whose [DayOfWeek] is in
 * [repeatDays]. If [repeatDays] is empty there is no such day, and the answer is `null` — an alarm
 * the user switched to Custom before ticking anything is saved and shown as armed, but nothing is
 * scheduled for it until at least one weekday is picked; see [AlarmRepeatMode.CUSTOM]'s own KDoc.
 *
 * @param hourOfDay the hour the alarm is set for, 0–23.
 * @param minute the minute past that hour, 0–59.
 * @param repeatMode how often the alarm repeats. Defaults to [AlarmRepeatMode.DAILY].
 * @param repeatDays which weekdays qualify under [AlarmRepeatMode.CUSTOM]. Ignored otherwise.
 * @param clock where "now" comes from. A parameter and never `Clock.systemDefaultZone()` read
 *   inside, because every promise above is only checkable if a test can decide what time it is.
 * @return the next date and time the alarm should go off, in the clock's own time zone; `null` when
 *   [repeatMode] is [AlarmRepeatMode.CUSTOM] and [repeatDays] is empty.
 * @author Phong-Kaster
 */
fun nextFireTime(
    hourOfDay: Int,
    minute: Int,
    repeatMode: AlarmRepeatMode = AlarmRepeatMode.DAILY,
    repeatDays: Set<DayOfWeek> = emptySet(),
    clock: Clock,
): LocalDateTime? {
    val now = LocalDateTime.now(clock)
    val todayAtThatTime = now.toLocalDate().atTime(hourOfDay, minute)

    if (repeatMode != AlarmRepeatMode.CUSTOM) {
        // `isAfter`, not `!isBefore`. The difference is only the single instant where the two are
        // equal — and that single instant is the one the receiver re-arms from. See the story above.
        if (todayAtThatTime.isAfter(now)) return todayAtThatTime

        // `plusDays(1)` and not `plusHours(24)`. They are the same number of hours on almost every
        // day of the year and a different number on the two days a clock changes — and what the
        // user asked for is "at seven every morning", not "every twenty-four hours". `plusDays`
        // keeps the wall-clock time; `plusHours(24)` would drift the alarm by an hour each spring
        // and autumn.
        return todayAtThatTime.plusDays(1L)
    }

    if (repeatDays.isEmpty()) return null

    // Walk forward one day at a time, today included, up to and including a full week out. Eight
    // candidates and not seven: today's weekday can be in `repeatDays` while today's time of day has
    // already gone, and the correct answer then is the *same* weekday next week — offset 7, one past
    // a bare week of offsets 0..6.
    for (offset in 0..7L) {
        val candidate = now.toLocalDate().plusDays(offset).atTime(hourOfDay, minute)
        if (candidate.dayOfWeek !in repeatDays) continue
        if (candidate.isAfter(now)) return candidate
    }

    // Unreachable: with `repeatDays` non-empty, some weekday within the next seven days always
    // matches, and the loop above always finds an `isAfter(now)` candidate at or before offset 7.
    // Kept as a typed `null` rather than an `error()` so a future change to the loop fails safe —
    // "nothing scheduled" rather than a crash inside a repository write.
    return null
}

/**
 * The same answer as [nextFireTime], counted in epoch milliseconds.
 *
 * `AlarmManager` speaks only in epoch milliseconds (`RTC_WAKEUP`), so somebody has to do this
 * conversion. It is here rather than in the data layer so that the zone used for it is the *same*
 * [Clock]'s zone as the one used to decide "today or tomorrow" — two different zones in those two
 * steps would produce an alarm that is right to the minute and wrong by hours, and nothing would
 * look wrong in the code.
 *
 * On the two days a year a clock changes, `atZone` does the sane thing on its own: a wall-clock time
 * that does not exist that day (the spring-forward gap) slides forward past the gap, and one that
 * happens twice (the autumn overlap) takes the first of the two. Neither needs handling here.
 *
 * @param hourOfDay the hour the alarm is set for, 0–23.
 * @param minute the minute past that hour, 0–59.
 * @param repeatMode how often the alarm repeats. Defaults to [AlarmRepeatMode.DAILY].
 * @param repeatDays which weekdays qualify under [AlarmRepeatMode.CUSTOM]. Ignored otherwise.
 * @param clock where "now" and the time zone both come from.
 * @return the next fire instant as epoch milliseconds, or `null` exactly when [nextFireTime] does.
 * @author Phong-Kaster
 */
fun nextFireTimeMillis(
    hourOfDay: Int,
    minute: Int,
    repeatMode: AlarmRepeatMode = AlarmRepeatMode.DAILY,
    repeatDays: Set<DayOfWeek> = emptySet(),
    clock: Clock,
): Long? =
    nextFireTime(
        hourOfDay = hourOfDay,
        minute = minute,
        repeatMode = repeatMode,
        repeatDays = repeatDays,
        clock = clock,
    )
        ?.atZone(clock.zone)
        ?.toInstant()
        ?.toEpochMilli()
