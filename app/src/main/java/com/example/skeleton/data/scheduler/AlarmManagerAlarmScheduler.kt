package com.example.skeleton.data.scheduler

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.skeleton.data.receiver.AlarmReceiver
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.scheduler.AlarmScheduler
import com.example.skeleton.domain.scheduler.nextFireTimeMillis
import java.time.Clock

/**
 * What [AlarmManagerAlarmScheduler.schedule] has decided to do, before it touches Android at all.
 *
 * This little type is the reason there is anything testable in this file. Every Android call below
 * is invisible to a unit test on this toolchain, but the *decision* in front of them is ordinary
 * Kotlin, and it is where the mistakes actually live: arming an alarm that has no row id, arming one
 * the user switched off, or arming the wrong instant. [scheduleDecision] makes that decision on its
 * own, and `AlarmSchedulingTest` checks it.
 *
 * @author Phong-Kaster
 */
internal sealed class ScheduleDecision {

    /**
     * Do nothing at all: this alarm has never been saved and has no id, so there is nothing to
     * address it by. See [Alarm.UNSAVED_ID].
     */
    data object Ignore : ScheduleDecision()

    /** Make sure nothing is pending for it — the alarm exists but is switched off. */
    data object Cancel : ScheduleDecision()

    /**
     * Arm one alarm.
     *
     * @param triggerAtMillis when it should go off, in epoch milliseconds.
     * @param exact whether the system will honour it to the minute. `false` means the app is not
     *   allowed to set exact alarms on this phone and the alarm will drift — late, never early.
     */
    data class Arm(val triggerAtMillis: Long, val exact: Boolean) : ScheduleDecision()
}

/**
 * Works out what should happen to an alarm, without doing any of it.
 *
 * The order of the two guards matters. An alarm with no id is refused **first**, because the thing
 * that addresses an alarm inside the system is [requestCodeFor] — and an id of zero would quietly
 * become request code zero, an address that every unsaved alarm in the app shares. Two of them would
 * overwrite each other and neither could be cancelled by row id afterwards.
 *
 * A switched-off alarm answers [ScheduleDecision.Cancel] rather than [ScheduleDecision.Ignore]. The
 * difference is the whole point: an alarm that has just been switched off usually has one already
 * armed, and "do nothing" would leave it ticking.
 *
 * @param alarm the alarm being armed.
 * @param canScheduleExactAlarms whether this phone currently lets the app set exact alarms.
 * @param clock where "now" comes from — a parameter so a test can fix it.
 * @author Phong-Kaster
 */
internal fun scheduleDecision(
    alarm: Alarm,
    canScheduleExactAlarms: Boolean,
    clock: Clock,
): ScheduleDecision {
    if (alarm.id == Alarm.UNSAVED_ID) return ScheduleDecision.Ignore

    if (!alarm.enabled) return ScheduleDecision.Cancel

    return ScheduleDecision.Arm(
        triggerAtMillis = nextFireTimeMillis(
            hourOfDay = alarm.hourOfDay,
            minute = alarm.minute,
            clock = clock,
        ),
        exact = canScheduleExactAlarms,
    )
}

/**
 * The number the system files an alarm's pending intent under: **the alarm's own row id**.
 *
 * This is load-bearing and it is a one-line function so that a test can pin it. Arming and
 * cancelling an alarm are two separate calls made minutes or days apart, and the only thing that
 * makes the second one find what the first one left behind is that both compute the same request
 * code from the same alarm id. Change this and every alarm already armed on every phone becomes
 * uncancellable — it keeps going off for a row the user has deleted.
 *
 * Row ids come from Room's `autoGenerate` and start at 1, so the narrowing to `Int` only loses
 * anything past two billion alarms, which is not a case this app has.
 *
 * @param alarmId the alarm's row id. Never [Alarm.UNSAVED_ID] — [scheduleDecision] refuses that
 *   before this is ever reached.
 * @author Phong-Kaster
 */
internal fun requestCodeFor(alarmId: Long): Int = alarmId.toInt()

/**
 * The one real [AlarmScheduler]: it hands alarms to Android's `AlarmManager`.
 *
 * Deliberately thin. Everything worth checking has been moved out of it — *when* an alarm next goes
 * off is [nextFireTimeMillis] in `domain/`, and *what to do about an alarm* is [scheduleDecision]
 * above. What is left here is the part no test on this toolchain can see anyway: building a
 * `PendingIntent` and calling the system.
 *
 * **How one alarm is addressed.** An alarm is armed as a broadcast to [AlarmReceiver], filed under
 * request code [requestCodeFor] — the alarm's row id. Arming the same id twice replaces the first;
 * cancelling looks the same id up and cancels whatever it finds. The intent's *extras* carry the
 * alarm's time and message so the receiver can both show it and arm the next one, and extras play no
 * part in whether the system considers two pending intents the same — only the action and the target
 * class do, which is why [cancel] can rebuild a matching intent while knowing nothing but the id.
 *
 * **Exact versus not.** `setExactAndAllowWhileIdle` is what makes an alarm arrive on the minute even
 * with the screen off. From Android 12 it needs a permission the user can take away at any time, so
 * the app asks first and falls back to an ordinary inexact `set` when the answer is no — an alarm
 * that is a few minutes late is worth far more than a crash, and telling the user why is the
 * permission notice's job, not this class's.
 *
 * @param context used to build the pending intents. The application context is the right one.
 * @param alarmManager the system service. A constructor parameter rather than a field looked up
 *   inside, so that the one Android dependency in this class is visible at the top of it.
 * @param clock where "now" comes from. A parameter for the same reason it is one on
 *   `AlarmRepositoryImpl`: an alarm's timing is only checkable if a test can decide what time it is.
 * @author Phong-Kaster
 */
class AlarmManagerAlarmScheduler(
    private val context: Context,
    private val alarmManager: AlarmManager =
        context.getSystemService(Context.ALARM_SERVICE) as AlarmManager,
    private val clock: Clock = Clock.systemDefaultZone(),
) : AlarmScheduler {

    override fun schedule(alarm: Alarm) {
        val decision = scheduleDecision(
            alarm = alarm,
            canScheduleExactAlarms = canScheduleExactAlarms(),
            clock = clock,
        )

        when (decision) {
            is ScheduleDecision.Ignore -> Log.w(TAG, "schedule ignored: the alarm has never been saved")

            is ScheduleDecision.Cancel -> cancel(alarmId = alarm.id)

            is ScheduleDecision.Arm -> arm(alarm = alarm, decision = decision)
        }
    }

    override fun cancel(alarmId: Long) {
        if (alarmId == Alarm.UNSAVED_ID) {
            Log.w(TAG, "cancel ignored: an unsaved alarm has no id to cancel by")
            return
        }

        // FLAG_NO_CREATE means "give me the pending intent that already exists, or nothing". Without
        // it this call would *create* a fresh pending intent and then cancel that brand-new one,
        // leaving the real armed alarm exactly where it was — a cancel that reports nothing wrong
        // and cancels nothing.
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeFor(alarmId = alarmId),
            broadcastIntent(alarmId = alarmId),
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
        )
        if (pendingIntent == null) {
            // Nothing was armed for this id. Ordinary: every edit and every delete asks, and most
            // of them have nothing to cancel.
            return
        }

        alarmManager.cancel(pendingIntent)
        // And throw away the pending intent itself, so a later FLAG_NO_CREATE lookup answers null
        // instead of handing back a stale one that is no longer armed.
        pendingIntent.cancel()
    }

    /** Hands one armed alarm to the system, exactly or inexactly. */
    private fun arm(alarm: Alarm, decision: ScheduleDecision.Arm) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCodeFor(alarmId = alarm.id),
            broadcastIntent(alarmId = alarm.id).apply {
                putExtra(AlarmReceiver.EXTRA_ALARM_MESSAGE, alarm.message)
                putExtra(AlarmReceiver.EXTRA_ALARM_HOUR_OF_DAY, alarm.hourOfDay)
                putExtra(AlarmReceiver.EXTRA_ALARM_MINUTE, alarm.minute)
            },
            // UPDATE_CURRENT so that editing an alarm's message replaces the extras of the pending
            // intent instead of leaving yesterday's wording armed. IMMUTABLE because nothing outside
            // this app has any business filling in parts of it.
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        try {
            if (decision.exact) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    decision.triggerAtMillis,
                    pendingIntent,
                )
                return
            }

            alarmManager.set(AlarmManager.RTC_WAKEUP, decision.triggerAtMillis, pendingIntent)
        } catch (e: SecurityException) {
            // The exact-alarm permission can be taken away between the check above and this call —
            // it is a setting the user can change while the app is running. Falling back rather than
            // letting it escape, because this is reached from a repository save and a thrown
            // exception there would turn a perfectly good write into a failure on screen.
            Log.w(TAG, "exact alarms were refused for alarm ${alarm.id}; falling back to an inexact one", e)
            runCatching {
                alarmManager.set(AlarmManager.RTC_WAKEUP, decision.triggerAtMillis, pendingIntent)
            }.onFailure { throwable -> Log.e(TAG, "alarm ${alarm.id} could not be armed at all", throwable) }
        }
    }

    /**
     * The intent every alarm of this app is armed with, minus its extras.
     *
     * Both arming and cancelling go through this one function on purpose. The system decides whether
     * two pending intents are the same by comparing action, data and target class — **not** extras —
     * so cancelling only works while these two intents are built identically. One function is the
     * only way to be sure they still are in a year's time.
     *
     * The id is put in here rather than with the other extras because it is the one piece the
     * receiver cannot do without: without it there is nothing to show and nothing to re-arm.
     */
    private fun broadcastIntent(alarmId: Long): Intent =
        Intent(context, AlarmReceiver::class.java).apply {
            action = AlarmReceiver.ACTION_ALARM_FIRED
            putExtra(AlarmReceiver.EXTRA_ALARM_ID, alarmId)
        }

    /**
     * Is this app allowed to set alarms that arrive on the minute?
     *
     * Always yes below Android 12, where the question did not exist yet. From Android 12 the user
     * can grant or withdraw it in system settings at any time, so it is asked again every time
     * rather than remembered.
     */
    private fun canScheduleExactAlarms(): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return true

        return alarmManager.canScheduleExactAlarms()
    }

    companion object {

        private const val TAG = "AlarmManagerAlarmScheduler"
    }
}
