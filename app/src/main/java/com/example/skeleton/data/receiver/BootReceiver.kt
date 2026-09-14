package com.example.skeleton.data.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.example.skeleton.domain.repository.AlarmRepository
import com.example.skeleton.domain.scheduler.AlarmScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

/**
 * Puts every alarm back after the phone has been switched off and on again.
 *
 * **The bug this exists to prevent is completely silent.** Everything Android knows about this app's
 * alarms lives in `AlarmManager`'s memory, and a restart throws all of it away. The rows are still in
 * the database, the list still shows them, every switch still reads ON — and not one of them will
 * ever go off again. Nobody notices until the morning they oversleep. Nothing in the app can detect
 * this afterwards either: from the app's side an armed alarm and a forgotten one look identical.
 *
 * So the system is asked to tell us when the phone has finished starting up
 * (`android.intent.action.BOOT_COMPLETED`, which needs the `RECEIVE_BOOT_COMPLETED` permission and a
 * receiver declared `exported="true"` — the boot broadcast comes from outside the app, so this is the
 * one receiver in the feature the system must be allowed to reach), and this reads the store and arms
 * the lot again.
 *
 * **Why this one needs a coroutine when [AlarmReceiver] does not.** `AlarmReceiver` rebuilds its
 * alarm out of the intent it was delivered with, so it finishes in microseconds. This one has no
 * intent to read from — the boot broadcast says only "the phone has started" — so it has to go to the
 * database, and that suspends. `onReceive` runs on the **main thread** and the process may be killed
 * the moment it returns, so:
 *
 * - [goAsync] asks the system to keep the process alive past the end of `onReceive`, and
 * - the returned `PendingResult` is finished in a `finally`, on **every** path including failure.
 *   Forgetting that leaves the process pinned for its whole grace period and then killed by the
 *   system with an ANR-shaped complaint in the log.
 *
 * **Nothing here is allowed to throw.** An exception escaping a boot receiver is a crash at the worst
 * possible moment — during start-up, before the user has even unlocked the phone. Every step is
 * caught and logged instead.
 *
 * There is no unit test for this class, and there cannot be one on this toolchain: `BroadcastReceiver`
 * and `Intent` are stubs that answer `null` here, so a test would go green without proving anything.
 * What *is* tested is the decision it delegates to — see `RearmAllTest` for `AlarmScheduler.rearmAll`,
 * and `NextFireTimeTest` for the instant each re-armed alarm lands on.
 *
 * Koin is reached through [KoinComponent] rather than a constructor, for the same reason as
 * [AlarmReceiver]: the system builds receivers itself and there is no constructor to inject into.
 *
 * @author Phong-Kaster
 */
class BootReceiver : BroadcastReceiver(), KoinComponent {

    private val alarmScheduler: AlarmScheduler by inject()

    private val alarmRepository: AlarmRepository by inject()

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) {
            // Not the broadcast this receiver was declared for. Re-arming on some other intent would
            // be harmless but pointless, and a receiver that acts on anything it is handed is the
            // kind that surprises somebody later.
            Log.w(TAG, "ignoring a broadcast that is not BOOT_COMPLETED: ${intent.action}")
            return
        }

        // Keep the process alive past the end of this method — the read below has not happened yet.
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Bounded so that a database that never answers — locked, corrupt, whatever the
                // reason — cannot hold `pendingResult` open until the system's own broadcast timeout
                // kills the process anyway. Better to give up cleanly at a time of this class's own
                // choosing than to be killed mid-read and finish nothing at all.
                val rearmed = withTimeoutOrNull(timeMillis = REARM_TIMEOUT_MILLIS) {
                    rearmEverything()
                    true
                }
                if (rearmed == null) Log.e(TAG, "re-arming alarms after a restart timed out")
            } catch (e: Exception) {
                // Nothing above this catch can be retried, and the alternative to swallowing is a
                // crash during boot. The alarms stay unarmed until the user next opens the app and
                // edits one, which is bad — but a boot-time crash is worse and hides the reason.
                Log.e(TAG, "alarms could not be re-armed after a restart", e)
            } finally {
                // Exactly once, on every path. See the class KDoc.
                pendingResult.finish()
            }
        }
    }

    /**
     * Reads every alarm the app has and hands the whole list to the scheduler.
     *
     * [AlarmRepository.alarmsFlow] is a live stream, so `first()` is what turns it into the one-off
     * read this needs: take the current list and stop collecting. Collecting it for longer would keep
     * the receiver alive watching for changes that nobody is making during boot.
     *
     * The list is handed over **whole**, switched-off alarms included. Deciding which of them deserve
     * arming is [AlarmScheduler.rearmAll]'s job and it already knows — filtering here would be a
     * second copy of that rule.
     */
    private suspend fun rearmEverything() {
        val alarms = alarmRepository.alarmsFlow.first()
        if (alarms.isEmpty()) {
            // Perfectly ordinary on a fresh install. Said out loud so that "the boot receiver ran and
            // did nothing" can be told apart from "the boot receiver never ran" in a log.
            Log.w(TAG, "there are no alarms to re-arm after the restart")
            return
        }

        alarmScheduler.rearmAll(alarms = alarms)
        Log.w(TAG, "re-armed ${alarms.size} alarm(s) after a restart")
    }

    companion object {

        private const val TAG = "BootReceiver"

        /**
         * How long this receiver waits for the store read and the re-arm before giving up.
         *
         * Comfortably inside the ten seconds or so the system allows a `goAsync()` receiver before
         * killing the process regardless — the point is to let go on this class's own terms, with a
         * clear log line, rather than being killed mid-read with `pendingResult` still open.
         */
        private const val REARM_TIMEOUT_MILLIS = 8_000L
    }
}
