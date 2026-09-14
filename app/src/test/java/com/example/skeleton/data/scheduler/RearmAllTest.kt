package com.example.skeleton.data.scheduler

import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.scheduler.AlarmScheduler
import com.example.skeleton.domain.scheduler.nextFireTimeMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * Holds `AlarmScheduler.rearmAll` — the thing that puts every alarm back after the phone restarts —
 * to the only three promises it makes.
 *
 * **Why the boot receiver itself is not what is tested here.** `BootReceiver` is a
 * `BroadcastReceiver`, and on this toolchain `BroadcastReceiver`, `Intent`, `PendingIntent` and
 * `AlarmManager` are all stubs from a fake `android.jar`: every call to them answers `0` / `false` /
 * `null` and says nothing about it. A test that delivered a boot broadcast and then asserted on what
 * happened would pass against a receiver that does absolutely nothing — coverage that is worse than
 * none, because it looks like proof. So the receiver's plumbing is left untested on purpose, and what
 * is checked instead is the decision it delegates to, which is ordinary Kotlin.
 *
 * **The third test is the one that earns this file.** After a restart, some of the user's alarms are
 * for times that have already gone by today — it is now nine in the morning and the seven o'clock
 * alarm is in the past. The tempting mistake is to treat those as expired and skip them, which is
 * exactly backwards: "already gone today" means "arm it for tomorrow". Skip it and that alarm is
 * silent forever, with the switch on screen still reading ON.
 *
 * [RecordingAlarmScheduler] is written fresh here rather than borrowed from `AlarmSchedulingTest`'s
 * `FakeAlarmScheduler`. That one records the *order* calls arrived in, machinery this file has no use
 * for; this one records the **instant** each alarm was armed for, which is the thing being asserted.
 *
 * @author Phong-Kaster
 */
class RearmAllTest {

    @Test
    fun `a switched-on alarm is armed again after a restart`() {
        // The plain case, and the reason the whole feature exists: a restart drops every alarm the
        // system knew about, so an alarm that survives in the database has to be handed over again or
        // it simply never goes off.
        val scheduler = RecordingAlarmScheduler(clock = CLOCK)

        scheduler.rearmAll(alarms = listOf(alarm(id = 1L, hourOfDay = 21, minute = 30)))

        assertEquals(
            nextFireTimeMillis(hourOfDay = 21, minute = 30, clock = CLOCK),
            scheduler.armed[1L],
        )
    }

    @Test
    fun `a switched-off alarm is not armed`() {
        // A reboot has already thrown away everything the system knew, so a switched-off alarm needs
        // nothing done to it at all — there is no leftover schedule to cancel. What must not happen
        // is the opposite: arming it, which would wake the user at a time whose switch reads OFF.
        val scheduler = RecordingAlarmScheduler(clock = CLOCK)

        scheduler.rearmAll(alarms = listOf(alarm(id = 2L, hourOfDay = 12, minute = 0, enabled = false)))

        assertFalse(scheduler.armed.containsKey(2L))
    }

    @Test
    fun `an alarm whose time has already gone today is armed for tomorrow, never skipped`() {
        // **The important one.** The clock is stopped at nine in the morning and this alarm is set
        // for seven, so today's occurrence is hours behind us. A naive re-arm filters out anything
        // whose today-instant is in the past — and that alarm is then gone for good, silently, while
        // its switch still reads ON.
        //
        // The right answer is tomorrow at seven, and `rearmAll` gets it for free by asking `schedule`
        // rather than re-deciding anything: `nextFireTime` already answers "tomorrow" for a time that
        // has passed.
        val scheduler = RecordingAlarmScheduler(clock = CLOCK)

        scheduler.rearmAll(alarms = listOf(alarm(id = 3L, hourOfDay = 7, minute = 0)))

        val armedAt = scheduler.armed[3L]
        assertTrue("an alarm armed in the past fires the instant it is set", (armedAt ?: 0L) > CLOCK.millis())
        assertEquals(
            LocalDateTime.of(2026, 3, 15, 7, 0).toInstant(ZoneOffset.UTC).toEpochMilli(),
            armedAt,
        )
    }

    @Test
    fun `a whole list is re-armed, and only the switched-on ones`() {
        // The three cases above arriving together, the way they do on a real phone: the loop has to
        // reach the last alarm as well as the first, and the switched-off one in the middle must not
        // stop it or sneak in.
        val scheduler = RecordingAlarmScheduler(clock = CLOCK)

        scheduler.rearmAll(
            alarms = listOf(
                alarm(id = 1L, hourOfDay = 21, minute = 30),
                alarm(id = 2L, hourOfDay = 12, minute = 0, enabled = false),
                alarm(id = 3L, hourOfDay = 7, minute = 0),
            ),
        )

        assertEquals(setOf(1L, 3L), scheduler.armed.keys)
    }

    @Test
    fun `re-arming an empty store does nothing at all`() {
        // A fresh install that has been restarted. Ordinary, and it must not be an error.
        val scheduler = RecordingAlarmScheduler(clock = CLOCK)

        scheduler.rearmAll(alarms = emptyList())

        assertTrue(scheduler.armed.isEmpty())
    }

    private fun alarm(
        id: Long,
        hourOfDay: Int,
        minute: Int,
        enabled: Boolean = true,
    ): Alarm = Alarm(
        id = id,
        message = "Take the bread out of the freezer",
        hourOfDay = hourOfDay,
        minute = minute,
        enabled = enabled,
        createdAt = 1_000L,
    )

    private companion object {

        /**
         * A clock stopped mid-morning on a fixed day, in UTC.
         *
         * The same reading as `AlarmSchedulingTest` uses, and mid-morning for the same reason: 21:30
         * is still ahead of it and 07:00 is already behind it, so one list of alarms exercises both
         * halves of "today or tomorrow". UTC so that no assertion can flip on a daylight-saving
         * change that happens to fall near the date.
         */
        private val CLOCK: Clock = Clock.fixed(
            LocalDate.of(2026, 3, 14).atTime(9, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC,
        )
    }
}

/**
 * A scheduler that arms nothing and writes down **which instant** each alarm was armed for.
 *
 * It deliberately implements only `schedule`, and never overrides `rearmAll` — so every test above is
 * exercising the real default body on [AlarmScheduler], which is the code under test here. The moment
 * somebody replaces that default with something that filters the list, these tests go red.
 *
 * `schedule` here mirrors the real scheduler's decision in the two ways this file asserts on: a
 * switched-off alarm is not armed, and an armed one lands on [nextFireTimeMillis]. It is not a second
 * copy of that decision to be maintained — the real one is `scheduleDecision`, checked by
 * `AlarmSchedulingTest`; this is just a notebook with enough sense to be worth asserting against.
 *
 * @param clock the same fixed clock the test asserts with, so "when would this have been armed for"
 *   has a knowable answer.
 * @author Phong-Kaster
 */
private class RecordingAlarmScheduler(private val clock: Clock) : AlarmScheduler {

    /** Alarm id to the instant it was armed for, in epoch milliseconds. */
    val armed = mutableMapOf<Long, Long>()

    override fun schedule(alarm: Alarm) {
        if (!alarm.enabled) return

        armed[alarm.id] = nextFireTimeMillis(
            hourOfDay = alarm.hourOfDay,
            minute = alarm.minute,
            clock = clock,
        )
    }

    override fun cancel(alarmId: Long) {
        // Nothing to cancel: this fake never armed anything with the system, and no test here asks
        // about cancelling. A reboot has already dropped every pending alarm anyway.
    }
}
