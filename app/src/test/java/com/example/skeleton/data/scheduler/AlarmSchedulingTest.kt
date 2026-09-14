package com.example.skeleton.data.scheduler

import com.example.skeleton.domain.enums.AlarmRepeatMode
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.scheduler.AlarmScheduler
import com.example.skeleton.domain.scheduler.nextFireTimeMillis
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Holds the *decisions* `AlarmManagerAlarmScheduler` makes to account, and hands the rest of the
 * test suite the fake scheduler it needs.
 *
 * **What is deliberately not tested here, and why.** `AlarmManager`, `PendingIntent` and `Intent`
 * are stubs on this toolchain: every call to them answers `0` / `false` / `null` and says nothing
 * about it. A test that armed an alarm and then asserted on the `AlarmManager` it was armed with
 * would pass on an implementation that does nothing whatsoever — which is worse than no test,
 * because it looks like coverage. There is no emulator and no Robolectric here, so
 * `AlarmManagerAlarmScheduler`'s Android half simply cannot be checked, and pretending otherwise is
 * the mistake this comment exists to prevent.
 *
 * So the decisions in front of those calls were pulled out into [scheduleDecision] and
 * [requestCodeFor], which are ordinary Kotlin, and those are what this file checks. They are where
 * the real mistakes live: arming an alarm that has no row id, arming one the user switched off,
 * arming the wrong instant, or changing the number an alarm is filed under.
 *
 * [FakeAlarmScheduler] lives here too rather than in the repository's test file, because it belongs
 * to the scheduling seam and because there must be exactly **one** of it — two fakes that drifted
 * apart would let the repository's tests and this file disagree about what "cancelled" means.
 *
 * @author Phong-Kaster
 */
class AlarmSchedulingTest {

    // ---------- Which alarms get armed at all ----------

    @Test
    fun `an alarm that has never been saved is ignored, not armed`() {
        // **The most important guard in the file.** An unsaved alarm's id is 0, and the number an
        // alarm is filed under inside the system is that same id — so arming one would file it under
        // request code 0, an address every unsaved alarm in the app would share. Two of them would
        // silently overwrite each other, and neither could ever be cancelled by row id afterwards:
        // a reminder going off daily for a row nobody can find.
        val decision = scheduleDecision(
            alarm = Alarm.draft(message = "Leave for the dentist"),
            canScheduleExactAlarms = true,
            clock = CLOCK,
        )

        assertEquals(ScheduleDecision.Ignore, decision)
    }

    @Test
    fun `a switched-off alarm is cancelled, not merely left alone`() {
        // "Cancel" and "do nothing" look identical on a fresh install and could not be more
        // different in real use: an alarm that has just been switched off almost always has one
        // already armed, and leaving it alone means the user's phone still goes off tomorrow morning
        // for an alarm whose switch reads OFF on screen.
        val decision = scheduleDecision(
            alarm = alarm(id = 5L, enabled = false),
            canScheduleExactAlarms = true,
            clock = CLOCK,
        )

        assertEquals(ScheduleDecision.Cancel, decision)
    }

    @Test
    fun `a saved, switched-on alarm is armed for its next occurrence`() {
        val decision = scheduleDecision(
            alarm = alarm(id = 5L, hourOfDay = 21, minute = 30),
            canScheduleExactAlarms = true,
            clock = CLOCK,
        )

        assertEquals(
            ScheduleDecision.Arm(
                // Not null: the default `repeatMode` here is `DAILY`, which always has a next
                // occurrence — only `CUSTOM` with no weekdays picked can answer `null`.
                triggerAtMillis = nextFireTimeMillis(hourOfDay = 21, minute = 30, clock = CLOCK)!!,
                exact = true,
            ),
            decision,
        )
    }

    @Test
    fun `the armed instant is in the future, even for a time that has already gone today`() {
        // The decision does not do this arithmetic itself — it asks `nextFireTime` — but the wiring
        // between the two is exactly the sort of thing that gets swapped for `System.currentTimeMillis()`
        // during a refactor, and an alarm armed for a moment in the past fires the instant it is set.
        val decision = scheduleDecision(
            alarm = alarm(id = 5L, hourOfDay = 7, minute = 0),
            canScheduleExactAlarms = true,
            clock = CLOCK,
        )

        assertTrue((decision as ScheduleDecision.Arm).triggerAtMillis > CLOCK.millis())
    }

    // ---------- Exact or not ----------

    @Test
    fun `an alarm is still armed when the phone refuses exact alarms`() {
        // From Android 12 the user can take the exact-alarm permission away at any time. The wrong
        // answer here is to give up (the alarm silently never goes off) or to throw (the save that
        // triggered it reports failure over a row that was written perfectly well). The right answer
        // is a less precise alarm — late, never early — and a notice elsewhere explaining why.
        val decision = scheduleDecision(
            alarm = alarm(id = 5L, hourOfDay = 21, minute = 30),
            canScheduleExactAlarms = false,
            clock = CLOCK,
        )

        assertEquals(
            ScheduleDecision.Arm(
                // Not null — see the same guard in the exact-alarm case above.
                triggerAtMillis = nextFireTimeMillis(hourOfDay = 21, minute = 30, clock = CLOCK)!!,
                exact = false,
            ),
            decision,
        )
    }

    @Test
    fun `refusing exact alarms changes only the precision, never the time`() {
        val exact = scheduleDecision(alarm = alarm(id = 5L), canScheduleExactAlarms = true, clock = CLOCK)
        val inexact = scheduleDecision(alarm = alarm(id = 5L), canScheduleExactAlarms = false, clock = CLOCK)

        assertEquals(
            (exact as ScheduleDecision.Arm).triggerAtMillis,
            (inexact as ScheduleDecision.Arm).triggerAtMillis,
        )
    }

    // ---------- Custom weekdays ----------

    @Test
    fun `a switched-on custom alarm with no weekday picked is cancelled, not armed`() {
        // The third path to Cancel, alongside "unsaved" and "switched off": a Custom alarm nobody
        // has ticked a day for has nothing for the system to arm, and whatever was pending for this
        // id before must not keep ticking.
        val decision = scheduleDecision(
            alarm = alarm(id = 5L, repeatMode = AlarmRepeatMode.CUSTOM, repeatDays = emptySet()),
            canScheduleExactAlarms = true,
            clock = CLOCK,
        )

        assertEquals(ScheduleDecision.Cancel, decision)
    }

    @Test
    fun `a switched-on custom alarm with a weekday picked is armed for its next occurrence`() {
        val repeatDays = setOf(DayOfWeek.MONDAY)

        val decision = scheduleDecision(
            alarm = alarm(id = 5L, hourOfDay = 7, minute = 0, repeatMode = AlarmRepeatMode.CUSTOM, repeatDays = repeatDays),
            canScheduleExactAlarms = true,
            clock = CLOCK,
        )

        assertEquals(
            ScheduleDecision.Arm(
                triggerAtMillis = nextFireTimeMillis(
                    hourOfDay = 7,
                    minute = 0,
                    repeatMode = AlarmRepeatMode.CUSTOM,
                    repeatDays = repeatDays,
                    clock = CLOCK,
                )!!,
                exact = true,
            ),
            decision,
        )
    }

    // ---------- The number an alarm is filed under ----------

    @Test
    fun `an alarm is filed under its own row id`() {
        // **Pinned on purpose, because changing it is invisible and catastrophic.** Arming an alarm
        // and cancelling it are two calls made days apart, and the only thing that makes the second
        // find what the first left behind is that both compute this same number from the same id.
        // Change the formula and every alarm already armed on every phone in the world becomes
        // uncancellable — it keeps going off for a row the user deleted weeks ago.
        assertEquals(5, requestCodeFor(alarmId = 5L))
        assertEquals(1, requestCodeFor(alarmId = 1L))
        assertEquals(9_000, requestCodeFor(alarmId = 9_000L))
    }

    private fun alarm(
        id: Long,
        hourOfDay: Int = 8,
        minute: Int = 0,
        enabled: Boolean = true,
        repeatMode: AlarmRepeatMode = AlarmRepeatMode.DAILY,
        repeatDays: Set<DayOfWeek> = emptySet(),
    ): Alarm = Alarm(
        id = id,
        message = "Take the bread out of the freezer",
        hourOfDay = hourOfDay,
        minute = minute,
        enabled = enabled,
        repeatMode = repeatMode,
        repeatDays = repeatDays,
        createdAt = 1_000L,
    )

    private companion object {

        /**
         * A clock stopped mid-morning on a fixed day.
         *
         * Mid-morning so that both halves of `nextFireTime` are exercised by the alarms above: 21:30
         * is still ahead of it, 07:00 is already behind it.
         */
        private val CLOCK: Clock = Clock.fixed(
            LocalDate.of(2026, 3, 14).atTime(9, 0).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC,
        )
    }
}

/**
 * A scheduler that arms nothing and simply writes down what it was asked to do.
 *
 * This is the seam that makes the alarms feature testable at all. The real scheduler ends in
 * `AlarmManager` calls that a JVM test cannot see; `AlarmRepositoryImpl` depends on the
 * [AlarmScheduler] interface instead, so a test can hand it this and then assert the thing that
 * genuinely matters — *that a deleted alarm's schedule was cancelled*, that an edited alarm did not
 * leave a second one armed behind it.
 *
 * [calls] is the part that earns its keep. Counting how many times each method ran is not enough:
 * "cancel the old one, then arm the new one" and "arm the new one, then cancel it again" produce
 * identical counts and opposite behaviour — the second leaves the user with no alarm at all. The
 * ordered list is the only way to tell them apart.
 *
 * It is `internal` rather than `private` so that `AlarmRepositoryImplTest`, in another package, can
 * use this one fake instead of growing a second one that slowly disagrees with it.
 *
 * @author Phong-Kaster
 */
internal class FakeAlarmScheduler : AlarmScheduler {

    /** Every alarm this was asked to arm, in order, whole. */
    val scheduled = mutableListOf<Alarm>()

    /** Every alarm id this was asked to cancel, in order. */
    val cancelled = mutableListOf<Long>()

    /**
     * Both of the above interleaved, as readable strings like `cancel(5)` and `schedule(5)`.
     *
     * An assertion on this list checks the **order** the two happened in, which no pair of separate
     * lists can show.
     */
    val calls = mutableListOf<String>()

    override fun schedule(alarm: Alarm) {
        scheduled += alarm
        calls += "schedule(${alarm.id})"
    }

    override fun cancel(alarmId: Long) {
        cancelled += alarmId
        calls += "cancel($alarmId)"
    }
}

/**
 * A scheduler where arming and cancelling both fail.
 *
 * It exists for one promise that is only a promise until something actually throws: **a phone that
 * will not arm an alarm must not turn a perfectly good save into a failure on screen.** The database
 * is the source of truth and the schedule is a best-effort mirror of it, so the write outcome cannot
 * depend on the mirror. Nothing else in this project can produce that failure — the real one is a
 * device revoking the exact-alarm permission mid-write.
 *
 * @author Phong-Kaster
 */
internal class BrokenAlarmScheduler : AlarmScheduler {

    override fun schedule(alarm: Alarm): Unit = throw IllegalStateException("alarms are not allowed")

    override fun cancel(alarmId: Long): Unit = throw IllegalStateException("alarms are not allowed")
}
