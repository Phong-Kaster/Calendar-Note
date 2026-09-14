package com.example.skeleton.domain.scheduler

import com.example.skeleton.domain.model.Alarm

/**
 * The thing that makes an alarm actually go off.
 *
 * **Why this interface exists at all** (the short version): everything the Android platform offers
 * for "wake me at this time" — `AlarmManager`, `PendingIntent`, `NotificationManager` — is invisible
 * to a plain JVM test. This project has no emulator and no Robolectric, and unit tests run against a
 * stub `android.jar` where those calls return `0` / `false` / `null` without a word. A test aimed
 * straight at `AlarmManager` therefore *passes* while proving nothing at all, which is worse than
 * having no test.
 *
 * So the decision "which alarms should be armed right now" is pulled out to this side of the line,
 * where it is ordinary Kotlin. `AlarmRepositoryImpl` depends on **this interface** and never on
 * `AlarmManager`, so a test can hand it a fake scheduler that simply writes down what it was asked
 * to do — and then assert the thing that actually matters: that a deleted alarm's schedule is
 * cancelled, that an edited alarm does not leave a second one armed behind it.
 *
 * There is exactly one real implementation, `AlarmManagerAlarmScheduler` in `data/scheduler/`. It is
 * deliberately thin: it takes these two instructions and passes them to the system, and it holds no
 * decision that a test would want to check.
 *
 * **Nothing here throws.** Same contract as a repository: arming an alarm is a best-effort mirror of
 * what is in the database, and a device that refuses to arm one must not take a screen down with it.
 *
 * @author Phong-Kaster
 */
interface AlarmScheduler {

    /**
     * Arms **one** alarm for its next occurrence.
     *
     * "Next occurrence" is worked out by [nextFireTime] from the current time: today at the alarm's
     * hour and minute if that is still ahead, tomorrow at the same time otherwise.
     *
     * **One occurrence, not a repeating series.** The platform's exact-alarm call fires once and is
     * then finished, so the daily repeat comes from `AlarmReceiver` calling this again the moment an
     * alarm goes off. That is why this method takes the whole alarm and not just a time — the
     * receiver has to be able to re-arm the very same alarm without asking the database anything.
     *
     * Calling it twice for the same alarm does **not** arm two: every alarm is addressed by its own
     * row id, so a second call replaces the first. An alarm that has never been saved (still
     * carrying [Alarm.UNSAVED_ID]) has no id to be addressed by and is ignored.
     *
     * An alarm whose `enabled` is `false` is not armed — it is cancelled, so that handing over a
     * switched-off alarm can never leave one ticking.
     *
     * @param alarm the alarm to arm. Only its id, hour, minute, message and `enabled` matter here.
     */
    fun schedule(alarm: Alarm)

    /**
     * Cancels whatever is pending for this alarm id, if anything is.
     *
     * Cancelling an alarm that was never armed is fine and does nothing — this is called on every
     * delete and on every edit, and most of those have nothing to cancel.
     *
     * **This is the half that must never be skipped.** A deleted alarm whose schedule is left
     * pending still goes off, still shows its notification, and — because the receiver re-arms
     * itself — goes off again every day afterwards, for a row the user cannot see and therefore
     * cannot delete a second time.
     *
     * @param alarmId the row id of the alarm, never [Alarm.UNSAVED_ID].
     */
    fun cancel(alarmId: Long)
}
