package com.example.skeleton.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.skeleton.MainActivity
import com.example.skeleton.R
import com.example.skeleton.domain.greeting.greetingDueOn
import com.example.skeleton.domain.repository.SettingRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.time.Clock

/**
 * Says hello, once a day, the first time the user brings the app to the front.
 *
 * One job, one class. `MainActivity.onStart` tells it "the app is in front now" and everything else
 * — has this already happened today, should anything be posted, what gets written down so it does
 * not happen again — is decided in here.
 *
 * **What the user actually sees.** An ordinary notification: a line in the shade, with the app's
 * usual notification sound. Deliberately **not** a heads-up banner that slides over whatever they are
 * doing. A greeting is pleasant, not urgent, and this one arrives at the exact moment the user is
 * already looking at the app — a banner covering the screen they just opened would be an
 * interruption by the app, of the app.
 *
 * **The check-and-write sequence is guarded by [greetingMutex], and that is not decoration.**
 * `onStart` fires on a cold launch, on a resume from the background, **and on every configuration
 * change** — a rotation, or this app's own in-app language picker, which recreates the activity.
 * Two of those arriving close together both start this sequence, and without the lock both could read
 * "not greeted yet" before either had written anything down, and the user would get two identical
 * greetings side by side in the shade. The lock makes the whole read-decide-post-write run happen one
 * at a time, so the second one reads what the first one wrote and stays quiet.
 *
 * **Nothing in here can be unit tested on this toolchain** — `NotificationManager`,
 * `NotificationChannel` and `PendingIntent` are all stubs that silently answer `0` / `null`, so an
 * assertion about them would pass whatever the code did. That is precisely why the *decision* lives in
 * `domain/greeting/GreetingDecision.kt` as a plain function with a test of its own, and why the
 * numbers that decide how the notification behaves are named constants below with
 * `GreetingNotifierConstantsTest` pinning them.
 *
 * @param context used to read strings, build the tap target and reach the notification service. The
 *   application context is the right one to hand over — a notification outlives any screen.
 * @param settingRepository where "the day the user was last greeted" is kept. It goes through
 *   storage and never through a field on this class: a field would be forgotten the moment the
 *   process is killed, and the user would be greeted again the next time they opened the app.
 * @param clock where "today" comes from. A constructor parameter, following `NoteRepositoryImpl` —
 *   a store owns its clock and the clock is injected — so that the decision this class delegates to
 *   can be tested against a day of the test's choosing.
 * @author Phong-Kaster
 */
class GreetingNotifier(
    private val context: Context,
    private val settingRepository: SettingRepository,
    private val clock: Clock = Clock.systemDefaultZone(),
) {

    /**
     * Guards the one thing that must not be interleaved: read the stored day → decide → post →
     * write the new day. Named for what it guards, one mutex for this one concern.
     */
    private val greetingMutex = Mutex()

    /**
     * Greets the user if this is the first time the app has come to the front today, and does
     * nothing at all otherwise.
     *
     * Safe to call on every single foreground — that is how it is meant to be used. `MainActivity`
     * calls it from `onStart` without asking any questions first, because every question worth
     * asking is answered in here.
     *
     * Nothing that goes wrong inside ever leaves this function. It is called from an activity
     * starting up; an exception escaping a DataStore read would take the whole app down on launch,
     * and the worst thing that can actually happen — the greeting is not shown — is a missing "hello",
     * not a broken app.
     */
    suspend fun greetIfFirstForegroundToday() {
        greetingMutex.withLock {
            greetOnceToday()
        }
    }

    /**
     * The sequence itself: read the stored day, decide, post, write the new day.
     *
     * Private, and **only ever called from inside [greetingMutex]** — see
     * [greetIfFirstForegroundToday], which is the only caller. Split out of that function rather
     * than written inline purely for readability: it lets the "nothing to do today" case leave
     * through an ordinary guard clause instead of nesting the real work inside an `if`.
     */
    private suspend fun greetOnceToday() {
        try {
            // `.first()` and not `.collect { }`: this is one question asked once, not a stream to
            // watch. The flow is a DataStore read, so the first value it emits is what is on disk
            // right now, and the collection ends there.
            val lastGreetedDate = settingRepository.lastGreetedDateFlow.first()

            // The whole decision, and the only part of this class that is tested. `null` means the
            // user has already heard from us today.
            val greetingDate = greetingDueOn(lastGreetedDate = lastGreetedDate, clock = clock)
            if (greetingDate == null) return

            // --- Why finishing is not optional once the decision is made (simple story) ---
            //
            // This runs in the activity's own scope, which is cancelled when the activity stops — and
            // a rotation stops it. Cancelled *between* posting the greeting and writing the day down,
            // the app would have greeted the user without remembering it, and the very next
            // foreground would greet them all over again. `NonCancellable` keeps these two lines
            // together: either the greeting is shown and recorded, or neither happens.
            withContext(NonCancellable) {
                postGreeting()

                // Written whether or not the notification actually reached the shade. On Android 13+
                // a post without POST_NOTIFICATIONS goes nowhere, and telling the user about that is
                // the permission notice's job, not this class's — exactly as `AlarmNotifier` treats
                // it. Retrying it on the next foreground would only greet them repeatedly on the day
                // they finally grant the permission.
                settingRepository.setLastGreetedDate(date = greetingDate)
            }
        } catch (e: CancellationException) {
            // Re-thrown, never swallowed: a cancelled read is the screen going away, not a failure,
            // and swallowing it breaks coroutine cancellation for everything above.
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "the daily greeting was not shown", e)
        }
    }

    /**
     * Creates the greeting channel the first time, and does nothing on every later call.
     *
     * Called once from `MainApplication.onCreate`, so the channel — and the choices the user makes
     * about it in system settings, including turning it off entirely — exists from the first launch
     * rather than appearing only after the first greeting. Also called before every post, because the
     * channel is gone again after the user clears the app's data.
     */
    fun createChannelIfNeeded() {
        // API 24 and 25 have no channels at all. There is nothing to create there, and the
        // notification's own priority is what speaks for it — see [NOTIFICATION_PRIORITY].
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            // What the user reads in the system settings list of this app's notifications. It has to
            // say what these notifications *are*, because that screen is where somebody who does not
            // want them goes to turn them off — and turning off the greeting must not also turn off
            // their alarms, which is the whole reason this is a second channel.
            context.getString(R.string.daily_greeting),
            CHANNEL_IMPORTANCE,
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (manager == null) {
            Log.e(TAG, "no NotificationManager; the greeting channel was not created")
            return
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * Builds the greeting and hands it to the system.
     *
     * Private on purpose: posting a greeting without first asking [greetingDueOn] and then writing
     * the day down is exactly the bug this feature exists to avoid, so there is no way to do it from
     * outside.
     */
    private fun postGreeting() {
        createChannelIfNeeded()

        val greeting = context.getString(R.string.hello_what_will_you_write_today)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // Never `@mipmap/ic_launcher`. Android re-tints a status-bar icon to a flat white
            // silhouette, so a full-colour launcher icon arrives as an unreadable white blob.
            // `ic_notification_alarm` is already drawn solid white for exactly that reason and is
            // reused here rather than adding a second, identical-looking asset.
            .setSmallIcon(R.drawable.ic_notification_alarm)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(greeting)
            // A longer greeting is cut to one line in the shade; `BigTextStyle` is what lets the user
            // pull it open and read the rest. Translations are frequently longer than the English.
            .setStyle(NotificationCompat.BigTextStyle().bigText(greeting))
            .setPriority(NOTIFICATION_PRIORITY)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()

        try {
            // On Android 13+ this quietly posts nothing when POST_NOTIFICATIONS was never granted.
            // All that matters here is that it does not throw on the way back into the activity.
            NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "the greeting was not posted; permission is missing", e)
        }
    }

    /**
     * Where a tap on the greeting lands: the app, wherever it ordinarily starts.
     *
     * No action on the intent, unlike `AlarmNotifier`'s — a greeting is not about any particular
     * screen, so `MainActivity` should do what it always does. That missing action is also what keeps
     * the two pending intents apart in the system's eyes: two `PendingIntent`s count as the same one
     * when their intents match on action, data and component, and these two differ on action.
     *
     * `CLEAR_TOP or SINGLE_TOP` so that a tap brings the copy of the app the user already has to the
     * front instead of stacking a second one behind it.
     */
    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        OPEN_APP_REQUEST_CODE,
        Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {

        private const val TAG = "GreetingNotifier"

        /**
         * The id of the channel every greeting is posted to — **its own, never the alarms one.**
         *
         * Two channels rather than one because the user must be able to turn the greeting off
         * without silencing their alarms, and because the two want different loudness (see
         * [CHANNEL_IMPORTANCE]). `AlarmNotifier.CHANNEL_ID` is `"alarms"`; these two strings must
         * never be the same.
         *
         * **Treat this string as permanent.** It is the name under which the user's own choices about
         * the greeting live on their phone. Changing it does not move those settings across: it
         * silently creates a second, freshly-defaulted channel and abandons the first, which then
         * sits in the app's settings screen forever with nothing posting to it.
         */
        const val CHANNEL_ID = "greeting"

        /**
         * How loudly the channel may interrupt on API 26 and up: `IMPORTANCE_DEFAULT` — a line in the
         * shade with the usual notification sound, and **no** banner over whatever is on screen.
         *
         * Lower than the alarms channel's `IMPORTANCE_HIGH`, on purpose and not by accident. An alarm
         * has to interrupt or it has failed; a greeting arrives at the very moment the user has just
         * opened the app, and a banner then would be the app talking over itself.
         *
         * **This number is frozen the first time the channel is created on a device.** Creating a
         * channel with an id that already exists is a silent no-op, so it cannot be raised or lowered
         * in a later release for anybody who has already run the app. That is why it is a named
         * constant with a test on it rather than a number typed into the builder.
         */
        val CHANNEL_IMPORTANCE: Int = NotificationManager.IMPORTANCE_DEFAULT

        /**
         * The same idea for API 24 and 25, which have no channels and read this off the builder:
         * `PRIORITY_DEFAULT`, which matches [CHANNEL_IMPORTANCE] so the greeting behaves the same way
         * on every version of Android this app runs on.
         *
         * Still set on newer versions, where it is simply ignored — one builder, no branching,
         * nothing to forget.
         */
        val NOTIFICATION_PRIORITY: Int = NotificationCompat.PRIORITY_DEFAULT

        /**
         * The one slot in the notification shade the greeting occupies.
         *
         * A single fixed number because there is only ever one greeting: today's. If yesterday's is
         * somehow still sitting there unread, today's replaces it rather than stacking a second copy
         * underneath.
         *
         * Chosen far away from the numbers `AlarmNotifier` uses — those are alarm row ids, which
         * start at 1 and count up — so that a greeting can never overwrite an alarm the user has not
         * read yet, or be overwritten by one. `GreetingNotifierConstantsTest` holds it apart from
         * them.
         */
        const val NOTIFICATION_ID = 1_000_000

        /**
         * The request code of the "open the app" tap target.
         *
         * Not shared with `AlarmNotifier`'s, though it would in fact be safe to share: what really
         * keeps the two pending intents apart is that their intents differ (this one carries no
         * action). A distinct number is simply one less thing to have to reason about.
         */
        private const val OPEN_APP_REQUEST_CODE = 1
    }
}
