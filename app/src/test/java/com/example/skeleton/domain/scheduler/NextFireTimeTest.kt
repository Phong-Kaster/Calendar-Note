package com.example.skeleton.domain.scheduler

import com.example.skeleton.domain.enums.AlarmRepeatMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Holds [nextFireTime] to the one piece of arithmetic the whole alarms feature stands on.
 *
 * **Why this function is tested and `AlarmManager` is not.** Nothing in this project can observe a
 * real alarm going off — no emulator, no Robolectric, and a stub `android.jar` where every framework
 * call answers `0` / `false` / `null` without complaining. A test pointed at `AlarmManager` would go
 * green while the app never made a sound. So the decision that actually matters — *which instant* —
 * was pulled out into a plain Kotlin function in `domain/`, and this file is the only thing in the
 * feature that can prove an alarm is set for the right moment.
 *
 * Every clock below is [Clock.fixed]. A test that read the real clock could not assert on a result
 * at all, and one written at three in the afternoon would quietly flip its answer at midnight.
 *
 * The cases are chosen, not sampled. Each is the smallest situation that can catch one specific slip:
 * an off-by-one at the exact-now boundary either fires an alarm twice or skips a whole day, and the
 * month rollover catches the kind of hand-rolled `day + 1` that works for twenty-eight days a month.
 *
 * @author Phong-Kaster
 */
class NextFireTimeTest {

    // ---------- Today or tomorrow ----------

    @Test
    fun `a time still ahead today goes off today`() {
        val nextFire = nextFireTime(hourOfDay = 21, minute = 30, clock = clockAt(hour = 9, minute = 0))

        assertEquals(LocalDateTime.of(2026, 3, 14, 21, 30), nextFire)
    }

    @Test
    fun `a time already gone today goes off tomorrow`() {
        // The half that keeps a morning alarm from being armed for a moment in the past. An alarm
        // set in the past is not "late" to AlarmManager — it fires immediately, so the user taps
        // save at nine in the morning and is told to get up, right then.
        val nextFire = nextFireTime(hourOfDay = 7, minute = 0, clock = clockAt(hour = 9, minute = 0))

        assertEquals(LocalDateTime.of(2026, 3, 15, 7, 0), nextFire)
    }

    @Test
    fun `a time that is exactly now counts as gone, and goes off tomorrow`() {
        // --- The boundary, and why it falls on this side (simple story) ---
        //
        // This single instant is the one `AlarmReceiver` re-arms from. An exact alarm fires **once**;
        // the only thing that makes the feature daily is the receiver asking for the next occurrence
        // the moment the current one goes off — and at that moment, "now" IS the alarm's time.
        //
        // Answer "today" here and the app arms an alarm for an instant that has just passed, the
        // system fires it straight away, the receiver shows the reminder a second time and arms it a
        // third — the user's phone buzzes over and over for one alarm.
        //
        // Answer "tomorrow" and the next occurrence is exactly twenty-four hours later, which is
        // what "repeats daily" means. The price is the opposite edge: an alarm saved at precisely
        // its own time waits a day instead of going off that instant. That is the right way round —
        // an alarm the user has already missed by a millisecond should not shout at them while their
        // finger is still on the save button.
        val nextFire = nextFireTime(hourOfDay = 9, minute = 0, clock = clockAt(hour = 9, minute = 0))

        assertEquals(LocalDateTime.of(2026, 3, 15, 9, 0), nextFire)
    }

    @Test
    fun `midnight one minute away is one minute away, on tomorrow's date`() {
        // The nastiest ordinary case: the answer is barely in the future and yet lands on a
        // different day, different month in some years, different year in one. A comparison done on
        // the time of day alone — "00:00 is before 23:59, so tomorrow" — happens to get the right
        // answer here for the wrong reason, so the assertion is on the whole date and time.
        val nextFire = nextFireTime(hourOfDay = 0, minute = 0, clock = clockAt(hour = 23, minute = 59))

        assertEquals(LocalDateTime.of(2026, 3, 15, 0, 0), nextFire)
    }

    // ---------- Rolling over the end of a month ----------

    @Test
    fun `tomorrow at the end of January is the first of February`() {
        // A hand-rolled "add one to the day number" gets 32 January here and either throws or wraps
        // to something nonsensical. `plusDays` knows how long months are; this is what says so.
        val januaryEvening = Clock.fixed(
            LocalDate.of(2026, 1, 31).atTime(23, 50).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC,
        )

        val nextFire = nextFireTime(hourOfDay = 0, minute = 10, clock = januaryEvening)

        assertEquals(LocalDateTime.of(2026, 2, 1, 0, 10), nextFire)
    }

    @Test
    fun `tomorrow on the twenty-eighth of February in a leap year is the twenty-ninth`() {
        // The other end of the same arithmetic, and the one a hardcoded month length gets wrong in
        // three years out of four: 2024 has a 29 February, so the alarm is not due on 1 March.
        val leapFebruary = Clock.fixed(
            LocalDate.of(2024, 2, 28).atTime(23, 50).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC,
        )

        val nextFire = nextFireTime(hourOfDay = 6, minute = 0, clock = leapFebruary)

        assertEquals(LocalDateTime.of(2024, 2, 29, 6, 0), nextFire)
    }

    // ---------- The daily repeat ----------

    @Test
    fun `the occurrence after a firing is exactly one day later at the same time`() {
        // **This is the test that says the alarm repeats at all.** Nothing in the platform repeats
        // it; `AlarmReceiver` re-arms it from the instant it fires, so this asks the function the
        // exact question the receiver asks: the clock stopped at 07:00 on the dot, on an alarm set
        // for 07:00. The answer has to be 07:00 tomorrow — not 07:00 today (which fires instantly,
        // over and over) and not some time that has drifted off the user's chosen minute.
        val theMomentItFired = clockAt(hour = 7, minute = 0)

        val nextFire = nextFireTime(hourOfDay = 7, minute = 0, clock = theMomentItFired)

        assertEquals(LocalDateTime.of(2026, 3, 15, 7, 0), nextFire)
        assertEquals(
            Duration.ofHours(24L),
            Duration.between(LocalDateTime.of(2026, 3, 14, 7, 0), nextFire),
        )
    }

    @Test
    fun `a firing that arrived a little late still lands on tomorrow, not the day after`() {
        // Alarms are allowed to arrive late — a phone asleep in a drawer delivers one minutes after
        // the minute. The re-arm has to cope: at 07:04 on an alarm set for 07:00, today's occurrence
        // is gone, so the answer is tomorrow at 07:00. A re-arm that measured "twenty-four hours
        // from delivery" instead would walk the alarm a few minutes later every single day.
        val deliveredLate = clockAt(hour = 7, minute = 4)

        val nextFire = nextFireTime(hourOfDay = 7, minute = 0, clock = deliveredLate)

        assertEquals(LocalDateTime.of(2026, 3, 15, 7, 0), nextFire)
    }

    // ---------- Custom weekdays ----------

    @Test
    fun `a custom alarm fires today when today's weekday is picked and the time is still ahead`() {
        // TODAY (2026-03-14) is a Saturday — see the companion object's own comment.
        val nextFire = nextFireTime(
            hourOfDay = 21,
            minute = 30,
            repeatMode = AlarmRepeatMode.CUSTOM,
            repeatDays = setOf(DayOfWeek.SATURDAY),
            clock = clockAt(hour = 9, minute = 0),
        )

        assertEquals(LocalDateTime.of(2026, 3, 14, 21, 30), nextFire)
    }

    @Test
    fun `a custom alarm skips to the next picked weekday when today is not one of them`() {
        // Saturday, none of Monday/Wednesday/Friday — the nearest of the three is Monday, two days
        // ahead, not the first one alphabetically or by enum order.
        val nextFire = nextFireTime(
            hourOfDay = 7,
            minute = 0,
            repeatMode = AlarmRepeatMode.CUSTOM,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
            clock = clockAt(hour = 9, minute = 0),
        )

        assertEquals(LocalDateTime.of(2026, 3, 16, 7, 0), nextFire)
    }

    @Test
    fun `a custom alarm whose only picked day is today, already gone, waits a full week`() {
        // The case the "offset 0..7" range in the implementation exists for: today (Saturday)
        // qualifies as a weekday, but 07:00 is already behind the 09:00 clock, so the answer is not
        // "no valid day" — it is the same weekday, a full week from today.
        val nextFire = nextFireTime(
            hourOfDay = 7,
            minute = 0,
            repeatMode = AlarmRepeatMode.CUSTOM,
            repeatDays = setOf(DayOfWeek.SATURDAY),
            clock = clockAt(hour = 9, minute = 0),
        )

        assertEquals(LocalDateTime.of(2026, 3, 21, 7, 0), nextFire)
    }

    @Test
    fun `a custom alarm with no weekday picked has no next occurrence`() {
        // Reachable from the editor: switching to Custom before ticking anything. There is nothing
        // to arm, and the answer says so rather than falling back to "every day" or crashing.
        val nextFire = nextFireTime(
            hourOfDay = 8,
            minute = 0,
            repeatMode = AlarmRepeatMode.CUSTOM,
            repeatDays = emptySet(),
            clock = clockAt(hour = 9, minute = 0),
        )

        assertNull(nextFire)
    }

    @Test
    fun `the millisecond form of a custom alarm with no weekday picked is also null`() {
        val millis = nextFireTimeMillis(
            hourOfDay = 8,
            minute = 0,
            repeatMode = AlarmRepeatMode.CUSTOM,
            repeatDays = emptySet(),
            clock = clockAt(hour = 9, minute = 0),
        )

        assertNull(millis)
    }

    // ---------- The epoch-millisecond form AlarmManager needs ----------

    @Test
    fun `the millisecond form is the same instant, read in the clock's own zone`() {
        // `AlarmManager` speaks only in epoch milliseconds, so somebody has to convert — and doing
        // it in a different time zone from the one that chose "today or tomorrow" would produce an
        // alarm that is right to the minute and hours wrong, with nothing in the code looking odd.
        val clock = clockAt(hour = 9, minute = 0)

        val millis = nextFireTimeMillis(hourOfDay = 21, minute = 30, clock = clock)

        assertEquals(
            LocalDateTime.of(2026, 3, 14, 21, 30).toInstant(ZoneOffset.UTC).toEpochMilli(),
            millis,
        )
    }

    @Test
    fun `the millisecond form is always in the future`() {
        // The one sentence the whole feature depends on, stated as a number: an instant that is not
        // after "now" is an alarm AlarmManager fires immediately.
        val clock = clockAt(hour = 9, minute = 0)

        val millis = nextFireTimeMillis(hourOfDay = 9, minute = 0, clock = clock)

        assertEquals(clock.millis() + Duration.ofHours(24L).toMillis(), millis)
    }

    private companion object {

        /** The day every test in this file pretends it is — a Saturday, in an ordinary year. */
        private val TODAY: LocalDate = LocalDate.of(2026, 3, 14)

        /**
         * A clock stopped at a chosen time on [TODAY], in UTC.
         *
         * UTC rather than a real zone so that no assertion here can pass or fail on a daylight-saving
         * change that happens to fall near the test's date.
         */
        private fun clockAt(hour: Int, minute: Int): Clock = Clock.fixed(
            TODAY.atTime(hour, minute).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC,
        )
    }
}
