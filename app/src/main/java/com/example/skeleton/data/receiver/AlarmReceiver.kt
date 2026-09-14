package com.example.skeleton.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.skeleton.data.notification.AlarmNotifier
import com.example.skeleton.domain.enums.AlarmRepeatMode
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.domain.scheduler.AlarmScheduler
import com.example.skeleton.domain.scheduler.toRepeatDaySet
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * The doorbell. The system rings it at the moment an alarm is due, and this class does two things:
 * shows the notification, then — unless the alarm is [AlarmRepeatMode.ONE_TIME] — **arms its next
 * occurrence**.
 *
 * **That second half is the entire repeat, daily or custom, and it is easy to miss.** The alarm was
 * armed with `setExactAndAllowWhileIdle`, which fires **once** and is then finished — there is no
 * repeating alarm anywhere in this feature. Nothing at all would happen again unless this receiver
 * asks for the next occurrence right here, every single time it fires. Delete the re-arm call in
 * [onReceive] and the app still works perfectly the first time an alarm goes off and is silent
 * forever after, which is a bug nobody notices until the following morning.
 *
 * **[AlarmRepeatMode.ONE_TIME] is the one case that must *not* re-arm.** Its next occurrence would
 * otherwise be computed exactly like a daily alarm's — [com.example.skeleton.domain.scheduler.nextFireTime]
 * does not know the difference on its own — so the guard has to live here, the one place that knows
 * an alarm just fired rather than being newly saved.
 *
 * **Why the alarm is rebuilt out of the intent instead of read back from the database.** `onReceive`
 * runs on the main thread and has only a few seconds to live; reading Room from it means
 * `goAsync()` and a coroutine, which is real machinery to maintain. Everything needed to show the
 * notification and arm the next one — the id, the time, the message — was put into the intent when
 * the alarm was armed, and it is kept honest by the write path: editing an alarm cancels the old
 * schedule and arms a new one with the new extras, and deleting it cancels the schedule outright. So
 * the intent cannot describe an alarm that no longer matches the table.
 *
 * **The alarm object this builds is schedule-shaped, not the stored row.** Its `createdAt` is a
 * placeholder, because the intent does not carry one and nothing here needs one. It must never be
 * handed to `AlarmRepository.save` — that would restamp the alarm's creation time. It exists only to
 * be shown and re-armed.
 *
 * Koin is reached through [KoinComponent] rather than a constructor, because the system builds
 * receivers itself and there is no constructor to inject into. By the time any broadcast arrives,
 * `MainApplication.onCreate` has long since started Koin, and `by inject()` resolves lazily anyway.
 *
 * There is no unit test for this class and there cannot be one on this toolchain: `Intent`, `Bundle`
 * and `PendingIntent` are all stubs that answer `null` here, so a test would prove nothing. What
 * *is* tested is everything it delegates to — `nextFireTime`, the scheduling decision, the notifier's
 * constants — which is the reason those seams exist.
 *
 * @author Phong-Kaster
 */
class AlarmReceiver : BroadcastReceiver(), KoinComponent {

    private val alarmScheduler: AlarmScheduler by inject()

    private val alarmNotifier: AlarmNotifier by inject()

    override fun onReceive(context: Context, intent: Intent) {
        val alarm = intent.toFiredAlarm()
        if (alarm == null) {
            // Nothing sensible to show and nothing sensible to re-arm. Bailing out is the only
            // honest answer; guessing an id here would arm a schedule pointing at no row at all.
            Log.w(TAG, "a broadcast arrived without a usable alarm in it; ignoring")
            return
        }

        // Notify first, re-arm second. If something goes wrong in between, the user has at least had
        // the reminder they asked for — the opposite order would risk arming tomorrow's alarm for
        // one that never appeared today.
        try {
            alarmNotifier.notifyAlarm(alarm = alarm)
        } catch (e: Exception) {
            // An exception escaping `onReceive` crashes the whole app from the user's point of view,
            // and it would take the re-arm below down with it — one failed notification would end
            // the alarm for good. Caught, logged, carry on.
            Log.e(TAG, "showing alarm ${alarm.id} failed", e)
        }

        // A one-time alarm has no next occurrence — asking `alarmScheduler.schedule` for one would
        // silently turn it back into a daily alarm, computing tomorrow's instant exactly as it would
        // for `DAILY`. So this is the one case that stops here.
        //
        // The alarm's `enabled` flag is left as it was — still true, still reading ON in the list —
        // even though nothing is armed for it any more. Fixing that would mean writing to Room from
        // here, and this class's own KDoc explains why that is not done: `onReceive` has only seconds
        // to live and reading or writing the database from it means `goAsync()` and a coroutine, real
        // machinery this feature does not carry. The next edit or delete of the alarm corrects the
        // schedule either way.
        if (alarm.repeatMode == AlarmRepeatMode.ONE_TIME) return

        try {
            alarmScheduler.schedule(alarm = alarm)
        } catch (e: Exception) {
            Log.e(TAG, "re-arming alarm ${alarm.id} failed; it will not repeat until it is edited", e)
        }
    }

    /**
     * Reads the alarm back out of the intent the schedule was armed with.
     *
     * Returns `null` when the intent is not one of ours — a missing id is the giveaway, because
     * [Alarm.UNSAVED_ID] is the one value a stored alarm can never have, so it doubles as the
     * "nothing was here" answer without needing a separate flag.
     */
    private fun Intent.toFiredAlarm(): Alarm? {
        val id = getLongExtra(EXTRA_ALARM_ID, Alarm.UNSAVED_ID)
        if (id == Alarm.UNSAVED_ID) return null

        // `valueOf` rather than a safe lookup: the extra was written by `AlarmManagerAlarmScheduler`
        // in the same app version, from `AlarmRepeatMode.name`, so an unrecognised value here would
        // mean the two have drifted apart — a bug worth a crash log, not a silent fallback that hides
        // it. `DAILY` is still supplied to `getStringExtra`'s default, for the one intent this app
        // will ever see without the extra at all: one already pending from before this field existed,
        // delivered after an app update replaces the receiver mid-flight.
        val repeatMode = AlarmRepeatMode.valueOf(
            getStringExtra(EXTRA_ALARM_REPEAT_MODE) ?: AlarmRepeatMode.DAILY.name,
        )

        return Alarm(
            id = id,
            message = getStringExtra(EXTRA_ALARM_MESSAGE).orEmpty(),
            hourOfDay = getIntExtra(EXTRA_ALARM_HOUR_OF_DAY, Alarm.DEFAULT_HOUR_OF_DAY),
            minute = getIntExtra(EXTRA_ALARM_MINUTE, Alarm.DEFAULT_MINUTE),
            // It fired, so it was armed. The re-arm below depends on this being true.
            enabled = true,
            repeatMode = repeatMode,
            repeatDays = getIntExtra(EXTRA_ALARM_REPEAT_DAYS, 0).toRepeatDaySet(),
            // A placeholder, and never written anywhere. See the class KDoc.
            createdAt = Alarm.UNSAVED_AT,
        )
    }

    companion object {

        private const val TAG = "AlarmReceiver"

        /**
         * The action on every alarm intent.
         *
         * The intent names this receiver's class directly, so an action is not needed to *route* it.
         * It is here because two `PendingIntent`s are considered the same one by the system when
         * their intents match on action, data and component — and cancelling an alarm depends on
         * building an intent the system recognises as the one already pending. A single action
         * constant used by both the arming and the cancelling path is the cheapest way to make sure
         * those two can never drift apart.
         */
        const val ACTION_ALARM_FIRED = "com.example.skeleton.action.ALARM_FIRED"

        /** The alarm's row id, as a `Long`. Absent means "not one of ours". */
        const val EXTRA_ALARM_ID = "com.example.skeleton.extra.ALARM_ID"

        /** What the alarm says — the body of the notification. A `String`. */
        const val EXTRA_ALARM_MESSAGE = "com.example.skeleton.extra.ALARM_MESSAGE"

        /** The hour the alarm is set for, 0–23, as an `Int`. Needed to arm the next occurrence. */
        const val EXTRA_ALARM_HOUR_OF_DAY = "com.example.skeleton.extra.ALARM_HOUR_OF_DAY"

        /** The minute past the hour, 0–59, as an `Int`. Needed to arm the next occurrence. */
        const val EXTRA_ALARM_MINUTE = "com.example.skeleton.extra.ALARM_MINUTE"

        /**
         * [AlarmRepeatMode.name], as a `String`. Decides whether this receiver re-arms at all after
         * notifying — see [onReceive].
         */
        const val EXTRA_ALARM_REPEAT_MODE = "com.example.skeleton.extra.ALARM_REPEAT_MODE"

        /**
         * Which weekdays a [AlarmRepeatMode.CUSTOM] alarm repeats on, packed by
         * [com.example.skeleton.domain.scheduler.toRepeatDaysBitmask], as an `Int`. Ignored for every
         * other [AlarmRepeatMode].
         */
        const val EXTRA_ALARM_REPEAT_DAYS = "com.example.skeleton.extra.ALARM_REPEAT_DAYS"
    }
}
