package com.example.skeleton.domain.greeting

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Clock
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Holds [greetingDueOn] to the one sentence the whole greeting feature is: **once per calendar day,
 * and exactly once**.
 *
 * **Why this function is tested and the notification is not.** Nothing in this project can watch a
 * real notification arrive — no emulator, no Robolectric, and a stub `android.jar` where every
 * framework call answers `0` / `false` / `null` without complaining. A test pointed at
 * `NotificationManager` would go green while the app greeted the user on every single launch. So the
 * decision that actually matters — *is one due?* — was pulled out into a plain Kotlin function in
 * `domain/`, and this file is the only thing in the feature that can prove the user is greeted once a
 * day rather than never or endlessly.
 *
 * Every clock below is [Clock.fixed]. A test that read the real clock could not assert on a result at
 * all, and one written at five to midnight would quietly change its mind five minutes later.
 *
 * [FakeGreetedDateStore] stands in for the preferences file. It holds **text**, not a date, exactly
 * as the real storage does, so these tests run the full round trip — decide, format, store, read
 * back, parse, decide again — which is where an ISO-8601 mismatch would show up.
 *
 * @author Phong-Kaster
 */
class GreetingDecisionTest {

    // ---------- Never greeted ----------

    @Test
    fun `somebody who has never been greeted is greeted`() {
        // A fresh install: there is nothing in storage, so `lastGreetedDate` is null. The answer must
        // be today's date — both "yes, greet" and "write this down".
        val greetingDate = greetingDueOn(lastGreetedDate = null, clock = clockAt(hour = 9, minute = 0))

        assertEquals(TODAY, greetingDate)
    }

    @Test
    fun `storage that cannot be read counts as never greeted`() {
        // A half-written preferences file, or a leftover value from an older version of the app. We
        // do not know when the user was last greeted, so we greet — a greeting too many is a friendly
        // message, while a crash on app start is not.
        assertNull(parseGreetedDate(stored = null))
        assertNull(parseGreetedDate(stored = ""))
        assertNull(parseGreetedDate(stored = "   "))
        assertNull(parseGreetedDate(stored = "yesterday"))
        assertNull(parseGreetedDate(stored = "14/03/2026"))
        assertNull(parseGreetedDate(stored = "2026-02-30"))
    }

    // ---------- Once, and only once, per day ----------

    @Test
    fun `asking twice on the same day greets only the first time`() {
        // **This is the whole feature.** `onStart` fires on a cold launch, on a resume from
        // background, and again on every rotation — so this exact sequence happens many times a day
        // and must produce exactly one greeting.
        val store = FakeGreetedDateStore()
        val morning = clockAt(hour = 9, minute = 0)

        assertTrue(store.askForAGreeting(clock = morning))
        assertFalse(store.askForAGreeting(clock = morning))
    }

    @Test
    fun `asking again later the same day still greets only once`() {
        // The same promise across a whole day rather than across two calls in a row: the app is
        // opened in the morning, at lunchtime and last thing at night, and the user hears from it
        // once. Note the last call is at 23 58, right up against the boundary — and still the same
        // day, so still silent.
        val store = FakeGreetedDateStore()

        assertTrue(store.askForAGreeting(clock = clockAt(hour = 0, minute = 2)))
        assertFalse(store.askForAGreeting(clock = clockAt(hour = 12, minute = 30)))
        assertFalse(store.askForAGreeting(clock = clockAt(hour = 23, minute = 58)))
    }

    @Test
    fun `a nearly twenty-four hour gap inside one day is still one day`() {
        // The case that tells a *calendar day* apart from a *rolling timer*, in the direction that
        // catches the lazy implementation. 00 02 to 23 58 is twenty-three hours and fifty-six
        // minutes — a "has it been 24 hours?" check says no and a calendar-day check says "same day";
        // here they happen to agree. The next test is the one where they disagree.
        val store = FakeGreetedDateStore()

        assertTrue(store.askForAGreeting(clock = clockAt(hour = 0, minute = 2)))
        assertFalse(store.askForAGreeting(clock = clockAt(hour = 23, minute = 58)))
    }

    // ---------- A new day re-arms it ----------

    @Test
    fun `a new calendar day greets again`() {
        val store = FakeGreetedDateStore()

        assertTrue(store.askForAGreeting(clock = clockAt(hour = 9, minute = 0)))
        assertFalse(store.askForAGreeting(clock = clockAt(hour = 21, minute = 0)))
        assertTrue(store.askForAGreeting(clock = clockAt(date = TOMORROW, hour = 8, minute = 0)))
    }

    @Test
    fun `four minutes across midnight is two different days and greets twice`() {
        // **The case a rolling twenty-four-hour timer gets wrong.** 23 58 and 00 02 are four minutes
        // apart, so a timer says "far too soon, stay quiet" — and the user, who opened the app last
        // thing at night and first thing in the morning, is never greeted on the new day at all. A
        // calendar-day comparison says these are two different days, and both greet.
        val store = FakeGreetedDateStore()

        assertTrue(store.askForAGreeting(clock = clockAt(hour = 23, minute = 58)))
        assertTrue(store.askForAGreeting(clock = clockAt(date = TOMORROW, hour = 0, minute = 2)))
    }

    @Test
    fun `the new day is found across the end of a month and the end of a year`() {
        // A hand-rolled "is the day number different" comparison gets both of these wrong: 31
        // December and 1 January have different day numbers by luck, but 31 January and 1 March
        // would too, and 14 March 2026 versus 14 March 2027 would not. `LocalDate` equality compares
        // the whole date, and this is what says so.
        val newYearsEve = clockAt(date = LocalDate.of(2026, 12, 31), hour = 23, minute = 0)
        val newYearsDay = clockAt(date = LocalDate.of(2027, 1, 1), hour = 1, minute = 0)
        val store = FakeGreetedDateStore()

        assertTrue(store.askForAGreeting(clock = newYearsEve))
        assertTrue(store.askForAGreeting(clock = newYearsDay))

        // And a whole year later on the same day-of-month and month is still a different day.
        val aYearOn = clockAt(date = LocalDate.of(2028, 1, 1), hour = 1, minute = 0)
        assertTrue(store.askForAGreeting(clock = aYearOn))
    }

    // ---------- What gets written down ----------

    @Test
    fun `the date handed back is the date that gets stored`() {
        // The answer is a date rather than a `Boolean` so that the caller never has to read the clock
        // a second time to work out what to remember. This is that promise: what comes out of the
        // decision is exactly what goes into storage.
        val greetingDate = greetingDueOn(lastGreetedDate = null, clock = clockAt(hour = 23, minute = 59))

        assertEquals(TODAY, greetingDate)
        assertEquals("2026-03-14", formatGreetedDate(date = greetingDate!!))
    }

    @Test
    fun `a stored day reads back as the same day`() {
        // The round trip. If the writing side and the reading side ever disagreed about the format,
        // every read would answer `null`, every launch would look like a fresh install, and the user
        // would be greeted over and over — with no error anywhere to say why.
        val stored = formatGreetedDate(date = TODAY)

        assertEquals("2026-03-14", stored)
        assertEquals(TODAY, parseGreetedDate(stored = stored))
    }

    // ---------- The fake storage these tests drive ----------

    /**
     * The preferences file, as a single string in memory.
     *
     * It holds **text** rather than a `LocalDate` on purpose: that is what the real `SettingDatastore`
     * holds, and keeping the fake honest about it is what makes the format round trip part of every
     * test above instead of something nobody checks.
     */
    private class FakeGreetedDateStore {

        private var stored: String? = null

        /** What `SettingDatastore.lastGreetedDateFlow` does: text in, date or `null` out. */
        fun lastGreetedDate(): LocalDate? = parseGreetedDate(stored = stored)

        /** What `SettingDatastore.setLastGreetedDate` does: date in, text out. */
        fun remember(date: LocalDate) {
            stored = formatGreetedDate(date = date)
        }
    }

    /**
     * One foreground of the app **on a phone where notifications work**, start to finish.
     *
     * Read what was stored, ask [greetingDueOn], and — only when one is due — write the day down.
     * That is the decision and the storage round trip, which is all this file is about.
     *
     * **It is not the shipped sequence, and must not be read as a copy of it.** The real one posts a
     * notification first and writes the day down **only if the system actually showed it**, because
     * a post without `POST_NOTIFICATIONS` is a silent no-op on Android 13+ and recording it cost one
     * user their install-day greeting. That rule lives in `GreetOnceADay.kt` and is tested by
     * `GreetOnceADayTest` — not here. This helper simply assumes every greeting lands, which is the
     * uninteresting case for the questions below and the only case in which the two agree.
     *
     * @return `true` when the user would have been greeted on this foreground.
     */
    private fun FakeGreetedDateStore.askForAGreeting(clock: Clock): Boolean {
        val greetingDate = greetingDueOn(lastGreetedDate = lastGreetedDate(), clock = clock) ?: return false

        remember(date = greetingDate)
        return true
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
