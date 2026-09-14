package com.example.skeleton.data.notification

import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the handful of numbers that decide whether an alarm actually *interrupts* the user.
 *
 * **Why a test about constants, of all things.** `AlarmNotifier`'s real work — building a
 * `NotificationChannel`, building a `Notification`, handing it to `NotificationManagerCompat` — is
 * completely invisible here. Unit tests in this project run against a stub `android.jar` where every
 * framework call answers `0` / `false` / `null` without a word, and there is no emulator and no
 * Robolectric. A test that built a channel and then asked it for its importance would read `0` from a
 * stub that was never told anything, so it would fail against perfectly good code and pass against
 * code that set nothing at all. Assertions on those objects are not weak here; they are **vacuous**.
 *
 * A *field* is a different thing entirely. `NotificationManager.IMPORTANCE_HIGH` is an ordinary
 * compile-time `Int`, not a call into the framework, so comparing one against another is real work
 * with a real answer. That is the whole reason these values are named constants in `AlarmNotifier`
 * instead of numbers typed inline at the builder: written inline they would be unreachable from any
 * test this project can run, and the one thing standing between the user and a silent alarm would be
 * a code review.
 *
 * **What is actually at stake.** Getting a heads-up banner needs two unrelated things to be right on
 * two eras of Android: the *channel's* importance on API 26 and up, and the builder's *priority plus
 * a sound or a vibration* on API 24 and 25. Lower either and the alarm still posts — it just arrives
 * as a quiet line in the notification shade that the user never sees, which for an alarm is the same
 * as not arriving. And the channel's importance is **frozen the first time the channel is created**,
 * so that particular mistake cannot be fixed in a later release for anyone who already ran the app.
 *
 * @author Phong-Kaster
 */
class AlarmNotifierConstantsTest {

    // ---------- Loud enough to be seen ----------

    @Test
    fun `the alarms channel is created at high importance, which is what allows a banner`() {
        // `IMPORTANCE_HIGH` is the lowest setting that still produces a heads-up on API 26 and up,
        // and this is the one number in the whole feature that cannot be corrected later: creating a
        // channel with an id that already exists is a silent no-op, so a channel shipped at
        // `IMPORTANCE_DEFAULT` stays at default forever on every phone that ever ran that build.
        assertEquals(NotificationManager.IMPORTANCE_HIGH, AlarmNotifier.CHANNEL_IMPORTANCE)
    }

    @Test
    fun `the notification itself is built at high priority, for the versions without channels`() {
        // API 24 and 25 have no channels at all and read this off the builder. It is still set on
        // newer versions, where it is simply ignored — one builder, no branching, nothing to forget.
        assertEquals(NotificationCompat.PRIORITY_HIGH, AlarmNotifier.NOTIFICATION_PRIORITY)
    }

    @Test
    fun `the alarm buzzes, which is the second half of a heads-up on the older versions`() {
        // On API 24 and 25 `PRIORITY_HIGH` **alone is not enough**: a heads-up there needs priority
        // *plus* a sound or a vibration. A pattern that was empty, or that was all zeroes, would be a
        // vibration that never happens — and the alarm would quietly stop popping up on exactly the
        // phones nobody has on their desk.
        assertTrue(AlarmNotifier.VIBRATION_PATTERN.isNotEmpty())
        assertTrue(AlarmNotifier.VIBRATION_PATTERN.any { milliseconds -> milliseconds > 0L })
    }

    @Test
    fun `the alarm is labelled as an alarm`() {
        // A label, not a permission: it tells the system how to rank and group this notification and
        // what survives a low-power or driving mode. It does not get the app past do-not-disturb.
        assertEquals(NotificationCompat.CATEGORY_ALARM, AlarmNotifier.NOTIFICATION_CATEGORY)
    }

    // ---------- The names things are filed under ----------

    @Test
    fun `the channel id is the one the user's own settings are filed under`() {
        // **Pinned because changing it is invisible and irreversible.** This string is the name under
        // which the user's choices about this app's alarms — sound, vibration, whether they may pop
        // up at all — live on their phone. Changing it does not move those settings across: it
        // silently creates a second, freshly-defaulted channel and abandons the first, which then
        // sits in the app's settings screen forever with nothing posting to it.
        assertEquals("alarms", AlarmNotifier.CHANNEL_ID)
    }

    @Test
    fun `every alarm owns its own line in the notification shade`() {
        // **Pinned for the same reason `requestCodeFor` is** — it is the number an alarm is filed
        // under, and two different things depend on it being the alarm's own row id. Two alarms must
        // never share a slot, or the 07:00 one going off wipes out the 06:45 one the user has not
        // read yet. And the *same* alarm going off again tomorrow must reuse its slot, replacing
        // yesterday's banner instead of stacking a pile of identical ones down the shade.
        assertEquals(5, AlarmNotifier.notificationIdFor(alarmId = 5L))
        assertEquals(1, AlarmNotifier.notificationIdFor(alarmId = 1L))
        assertEquals(9_000, AlarmNotifier.notificationIdFor(alarmId = 9_000L))
    }
}
