package com.example.skeleton.data.notification

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

/**
 * Pins the handful of numbers and names that decide how the daily greeting behaves — and, more
 * importantly, that it is **not** the alarms notification wearing a different hat.
 *
 * **Why a test about constants, of all things.** `GreetingNotifier`'s real work — building a
 * `NotificationChannel`, building a `Notification`, handing it to `NotificationManagerCompat` — is
 * completely invisible here. Unit tests in this project run against a stub `android.jar` where every
 * framework call answers `0` / `false` / `null` without a word, and there is no emulator and no
 * Robolectric. A test that built a channel and then asked it for its importance would read `0` from a
 * stub that was never told anything: it would fail against perfectly good code and pass against code
 * that set nothing at all. Assertions on those objects are not weak here; they are **vacuous**.
 *
 * A *field* is a different thing entirely. `NotificationManager.IMPORTANCE_DEFAULT` is an ordinary
 * compile-time `Int`, not a call into the framework, so comparing one against another is real work
 * with a real answer. That is the whole reason these values are named constants in `GreetingNotifier`
 * rather than numbers typed inline at the builder — written inline they would be unreachable from any
 * test this project can run.
 *
 * This file does not touch `AlarmNotifier` beyond *reading* two of its constants, which is exactly
 * the point of the comparisons in the last section.
 *
 * @author Phong-Kaster
 */
class GreetingNotifierConstantsTest {

    // ---------- Friendly, not urgent ----------

    @Test
    fun `the greeting channel is created at default importance, which is a line in the shade`() {
        // `IMPORTANCE_DEFAULT` makes a sound and leaves a line in the notification shade, and does
        // **not** slide a banner over whatever is on screen. That is the right level for a greeting:
        // it arrives at the very moment the user has just opened the app, so a banner would be the
        // app interrupting itself.
        //
        // And this is the one number in the feature that cannot be corrected later: creating a
        // channel with an id that already exists is a silent no-op, so whatever ships first is what
        // every phone that ever ran the app is stuck with.
        assertEquals(NotificationManager.IMPORTANCE_DEFAULT, GreetingNotifier.CHANNEL_IMPORTANCE)
    }

    @Test
    fun `the greeting is built at default priority, for the versions without channels`() {
        // API 24 and 25 have no channels at all and read this off the builder instead. It matches the
        // channel importance above on purpose — the greeting must not turn into a banner on old
        // phones just because they take a different route to the same decision.
        assertEquals(NotificationCompat.PRIORITY_DEFAULT, GreetingNotifier.NOTIFICATION_PRIORITY)
    }

    // ---------- The names things are filed under ----------

    @Test
    fun `the greeting channel id is the one the user's own settings are filed under`() {
        // **Pinned because changing it is invisible and irreversible.** This string is the name under
        // which the user's choices about the greeting — its sound, whether it may appear at all —
        // live on their phone. Changing it does not move those settings across: it silently creates a
        // second, freshly-defaulted channel and abandons the first, which then sits in the app's
        // settings screen forever with nothing posting to it.
        assertEquals("greeting", GreetingNotifier.CHANNEL_ID)
    }

    // ---------- Kept well clear of the alarms ----------

    @Test
    fun `the greeting has a channel of its own and not the alarms one`() {
        // **The reason there are two channels at all.** A user who does not want a daily hello must
        // be able to switch it off in system settings without also silencing the alarms they set
        // themselves — and on one shared channel, that single switch would do both. The two also want
        // different loudness: an alarm has failed if it does not interrupt, a greeting has failed if
        // it does.
        assertNotEquals(AlarmNotifier.CHANNEL_ID, GreetingNotifier.CHANNEL_ID)
        assertNotEquals(AlarmNotifier.CHANNEL_IMPORTANCE, GreetingNotifier.CHANNEL_IMPORTANCE)
    }

    @Test
    fun `the greeting never overwrites an alarm sitting unread in the shade`() {
        // Two notifications posted under the same id are one notification: the second replaces the
        // first. `AlarmNotifier` files every alarm under its own row id, and row ids start at 1 and
        // count up — so a greeting id anywhere near them would one day quietly wipe out an alarm the
        // user had not read yet. The check is spelled out against real row ids rather than trusted to
        // a comment.
        assertNotEquals(AlarmNotifier.notificationIdFor(alarmId = 1L), GreetingNotifier.NOTIFICATION_ID)
        assertNotEquals(AlarmNotifier.notificationIdFor(alarmId = 2L), GreetingNotifier.NOTIFICATION_ID)
        assertNotEquals(AlarmNotifier.notificationIdFor(alarmId = 500L), GreetingNotifier.NOTIFICATION_ID)
    }

    @Test
    fun `there is one greeting slot, not one per day`() {
        // A fixed number, not something derived from the date. There is only ever one greeting worth
        // showing — today's — so yesterday's unread one is replaced rather than stacking a pile of
        // identical hellos down the shade.
        assertEquals(1_000_000, GreetingNotifier.NOTIFICATION_ID)
    }
}
