package com.example.skeleton.domain.greeting

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Holds [greetOnceADay] to the sentence the greeting feature is actually judged on: **the user is
 * greeted once a calendar day, and the day is written down only when the greeting really went out.**
 *
 * **Why this file exists on top of [GreetingDecisionTest].** That file proves the *decision* — "is one
 * due today?" — is right. It cannot see the bug a human tester found, because the bug was not in the
 * decision at all: the app asked correctly, posted into a void, and then wrote today's date down
 * anyway. On Android 13+ a notification posted without `POST_NOTIFICATIONS` is a **silent no-op** —
 * nothing shown, nothing thrown — so on a fresh install, where the app comes to the front before the
 * user has been asked for the permission, the date was recorded against a greeting nobody saw and the
 * user was never greeted on the day they installed the app. `failure then success on the same day`
 * below is that exact report, written as a test.
 *
 * **Why this and not `GreetingNotifier`.** Unit tests in this project run against a stub `android.jar`
 * where every framework call answers `0` / `false` / `null` without complaining, and there is no
 * emulator and no Robolectric. A test aimed at `NotificationManagerCompat` would go green whatever the
 * code did. So the sequence that carries the promise is plain Kotlin in `domain/`, with the Android
 * side handed in as two lambdas, and [FakeGreetingChannel] stands in for them here.
 *
 * Every clock below is [Clock.fixed]. A test that read the real clock could not assert on a result at
 * all, and one written at five to midnight would quietly change its mind five minutes later.
 *
 * @author Phong-Kaster
 */
class GreetOnceADayTest {

    // ---------- Never greeted ----------

    @Test
    fun `somebody who has never been greeted is greeted, and today is written down`() = runTest {
        // A fresh install with notifications working: one greeting goes out, and the day is recorded
        // so that nothing greets them again until tomorrow.
        val channel = FakeGreetingChannel(delivers = true)

        channel.foreground(clock = clockAt(hour = 9, minute = 0))

        assertEquals(1, channel.greetingsSent)
        assertEquals(TODAY, channel.recordedDate)
    }

    @Test
    fun `a greeting the system refused to show is not written down`() = runTest {
        // **The install-day case.** The app is in front before the user has been asked for the
        // notification permission, so the post goes nowhere. Nothing may be recorded — otherwise the
        // rest of the day is spent believing the user has been greeted.
        val channel = FakeGreetingChannel(delivers = false)

        channel.foreground(clock = clockAt(hour = 9, minute = 0))

        assertEquals(1, channel.greetingsSent)
        assertNull(channel.recordedDate)
    }

    // ---------- Once, and only once, per day ----------

    @Test
    fun `somebody already greeted today is not greeted again and nothing is posted`() = runTest {
        // Not merely "no second notification" — no *attempt*. `onStart` fires on a cold launch, on a
        // resume from the background, and on every rotation, so this path runs many times a day and
        // must be completely silent: no post, no write.
        val channel = FakeGreetingChannel(delivers = true, alreadyGreetedOn = TODAY)

        channel.foreground(clock = clockAt(hour = 9, minute = 0))

        assertEquals(0, channel.greetingsSent)
        assertEquals(TODAY, channel.recordedDate)  // untouched: still what was already there
    }

    @Test
    fun `a second foreground the same day sends no second greeting`() = runTest {
        // T-001's original promise, and it still holds after the delivery rule was added: the first
        // foreground greets, the second reads what the first wrote and stays quiet.
        val channel = FakeGreetingChannel(delivers = true)

        channel.foreground(clock = clockAt(hour = 9, minute = 0))
        channel.foreground(clock = clockAt(hour = 21, minute = 30))

        assertEquals(1, channel.greetingsSent)
        assertEquals(TODAY, channel.recordedDate)
    }

    // ---------- The regression a human found ----------

    @Test
    fun `failure then success on the same day greets exactly once and records only the success`() = runTest {
        // **DoD criterion 15, written as a test.** App data cleared; the user opens the app (the post
        // goes nowhere, because they have not been asked yet), grants the permission, and comes back.
        // The second foreground must greet them — the failed first one recorded nothing — and the
        // day must be written down only now. Exactly one greeting reaches the user.
        val channel = FakeGreetingChannel(delivers = false)

        channel.foreground(clock = clockAt(hour = 9, minute = 0))
        assertNull(channel.recordedDate)

        // The user grants the notification permission. Posts start landing.
        channel.delivers = true
        channel.foreground(clock = clockAt(hour = 9, minute = 5))

        // Two attempts in total, and the second is the one the user actually saw.
        assertEquals(2, channel.greetingsSent)
        assertEquals(TODAY, channel.recordedDate)

        // And from here the day is spent: a third foreground adds nothing.
        channel.foreground(clock = clockAt(hour = 18, minute = 0))
        assertEquals(2, channel.greetingsSent)
    }

    @Test
    fun `a whole day of refusals never records anything`() = runTest {
        // Somebody who has turned this app's notifications off entirely. Every foreground tries and
        // every one is refused, and the stored day stays empty — so the moment they change their
        // mind, they get their greeting instead of being told they have already had it.
        val channel = FakeGreetingChannel(delivers = false)

        channel.foreground(clock = clockAt(hour = 8, minute = 0))
        channel.foreground(clock = clockAt(hour = 13, minute = 0))
        channel.foreground(clock = clockAt(hour = 22, minute = 0))

        assertEquals(3, channel.greetingsSent)
        assertNull(channel.recordedDate)
    }

    // ---------- A new day re-arms it ----------

    @Test
    fun `a new calendar day greets again`() = runTest {
        val channel = FakeGreetingChannel(delivers = true)

        channel.foreground(clock = clockAt(hour = 9, minute = 0))
        channel.foreground(clock = clockAt(hour = 21, minute = 0))
        channel.foreground(clock = clockAt(date = TOMORROW, hour = 8, minute = 0))

        assertEquals(2, channel.greetingsSent)
        assertEquals(TOMORROW, channel.recordedDate)
    }

    @Test
    fun `the day written down is the day the decision was made on`() = runTest {
        // The decision hands back a date and the caller stores *that* date, never a second reading of
        // the clock. A foreground at 23:59 is decided against today and recorded against today; if
        // the two could drift apart, the user would be greeted again the moment the clock ticked over
        // and then not again until the day after tomorrow.
        val channel = FakeGreetingChannel(delivers = true)

        channel.foreground(clock = clockAt(hour = 23, minute = 59))

        assertEquals(TODAY, channel.recordedDate)
    }

    // ---------- The fake Android side these tests drive ----------

    /**
     * Stands in for the two lambdas [greetOnceADay] takes: the notification, and the preferences file.
     *
     * [delivers] is the one thing that cannot be observed on this toolchain and is therefore the one
     * thing worth being able to switch — `true` means the system accepted the greeting for display,
     * `false` means it silently showed nothing (no `POST_NOTIFICATIONS`, or the user turned this app's
     * notifications off). It is a `var` so a test can flip it mid-way, which is exactly what granting
     * the permission looks like from in here.
     *
     * [greetingsSent] counts **attempts**, not successes, so the tests can tell "posted and refused"
     * apart from "never even tried".
     */
    private class FakeGreetingChannel(
        var delivers: Boolean,
        alreadyGreetedOn: LocalDate? = null,
    ) {

        /** How many times a greeting was handed to the system, delivered or not. */
        var greetingsSent: Int = 0
            private set

        /** What is in storage: the day the user was last greeted, or `null` for never. */
        var recordedDate: LocalDate? = alreadyGreetedOn
            private set

        /** One foreground of the app, start to finish — the same call `GreetingNotifier` makes. */
        suspend fun foreground(clock: Clock) {
            greetOnceADay(
                lastGreetedDate = recordedDate,
                clock = clock,
                postGreeting = {
                    greetingsSent++
                    delivers
                },
                recordGreetedOn = { date ->
                    recordedDate = date
                },
            )
        }
    }

    private companion object {

        /** The day most of the tests in this file pretend it is. */
        private val TODAY: LocalDate = LocalDate.of(2026, 3, 14)

        /** The day after [TODAY]. */
        private val TOMORROW: LocalDate = LocalDate.of(2026, 3, 15)

        /**
         * A clock stopped at a chosen time on a chosen day, in UTC.
         *
         * UTC rather than a real zone so that no assertion here can pass or fail on a
         * daylight-saving change that happens to land near one of the test dates.
         */
        private fun clockAt(date: LocalDate = TODAY, hour: Int, minute: Int): Clock = Clock.fixed(
            date.atTime(hour, minute).toInstant(ZoneOffset.UTC),
            ZoneOffset.UTC,
        )
    }
}
