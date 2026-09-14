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
 * deliberately thin: it takes these instructions and passes them to the system, and it holds no
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

    /**
     * Arms every alarm in the list again, from nothing.
     *
     * **Why this exists: a restart wipes the lot.** Everything the system knows about this app's
     * alarms lives in `AlarmManager`'s memory, and Android throws all of it away when the phone is
     * switched off. The rows are still in the database and the switches on screen still read ON, so
     * nothing looks wrong at all — the alarms simply never go off again, and the user finds out by
     * oversleeping. Somebody has to arm them all a second time after the phone comes back, and that
     * somebody is `BootReceiver` calling this.
     *
     * **It is [schedule] in a loop, and that is the entire point.** Which alarms deserve arming,
     * which need cancelling instead, and *when* each one next goes off — all of that is already
     * decided, and already tested, inside [schedule]. Re-deriving any of it here would mean two
     * answers to the same question, and the second one is the one that goes stale. In particular
     * this must **not** skip an alarm whose time has already gone by today: "already gone today"
     * means "arm it for tomorrow", which is exactly what [schedule] does on its own.
     *
     * Cancelling a switched-off alarm here is a harmless no-op — a reboot has already dropped
     * everything there was to cancel — so no special case is needed for it.
     *
     * **One bad alarm must not cost the user the other twenty.** This runs on the one occasion in
     * the app's life where nothing is armed at all, so letting an exception from one alarm escape
     * would abandon every alarm after it in the list — silently, until each one's owner happens to
     * edit it. Each alarm is armed inside its own `runCatching`, matching the interface's own promise
     * that nothing here throws.
     *
     * This is the real implementation, not a convenience default a caller is expected to replace:
     * every implementation goes through this one loop, so there is exactly one place that decides
     * which alarms deserve arming.
     *
     * @param alarms every alarm in the store, switched-on and switched-off alike. An empty list is
     *   fine and does nothing.
     */
    fun rearmAll(alarms: List<Alarm>) {
        alarms.forEach { alarm -> runCatching { schedule(alarm = alarm) } }
    }
}
