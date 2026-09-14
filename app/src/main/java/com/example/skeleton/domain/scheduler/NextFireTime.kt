package com.example.skeleton.domain.scheduler

import java.time.Clock
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
 * @param hourOfDay the hour the alarm is set for, 0–23.
 * @param minute the minute past that hour, 0–59.
 * @param clock where "now" comes from. A parameter and never `Clock.systemDefaultZone()` read
 *   inside, because every promise above is only checkable if a test can decide what time it is.
 * @return the next date and time the alarm should go off, in the clock's own time zone.
 * @author Phong-Kaster
 */
fun nextFireTime(hourOfDay: Int, minute: Int, clock: Clock): LocalDateTime {
    val now = LocalDateTime.now(clock)
    val todayAtThatTime = now.toLocalDate().atTime(hourOfDay, minute)

    // `isAfter`, not `!isBefore`. The difference is only the single instant where the two are
    // equal — and that single instant is the one the receiver re-arms from. See the story above.
    if (todayAtThatTime.isAfter(now)) return todayAtThatTime

    // `plusDays(1)` and not `plusHours(24)`. They are the same number of hours on almost every day
    // of the year and a different number on the two days a clock changes — and what the user asked
    // for is "at seven every morning", not "every twenty-four hours". `plusDays` keeps the
    // wall-clock time; `plusHours(24)` would drift the alarm by an hour each spring and autumn.
    return todayAtThatTime.plusDays(1L)
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
 * @param clock where "now" and the time zone both come from.
 * @return the next fire instant as epoch milliseconds.
 * @author Phong-Kaster
 */
fun nextFireTimeMillis(hourOfDay: Int, minute: Int, clock: Clock): Long =
    nextFireTime(hourOfDay = hourOfDay, minute = minute, clock = clock)
        .atZone(clock.zone)
        .toInstant()
        .toEpochMilli()
