package com.example.skeleton

import com.example.skeleton.core.extension.date_and_time.formatDuration
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks the `m:ss` / `h:mm:ss` text made by [formatDuration].
 *
 * @author Phong-Kaster
 */
class DurationExtensionTest {

    @Test
    fun zeroMs_isZeroColonZeroZero() {
        assertEquals("0:00", formatDuration(durationMs = 0L))
    }

    @Test
    fun fiftyNineSeconds() {
        assertEquals("0:59", formatDuration(durationMs = 59_000L))
    }

    @Test
    fun sixtyOneSeconds() {
        assertEquals("1:01", formatDuration(durationMs = 61_000L))
    }

    @Test
    fun partialSecond_isRoundedDown() {
        assertEquals("0:59", formatDuration(durationMs = 59_999L))
    }

    @Test
    fun justUnderOneHour() {
        assertEquals("59:59", formatDuration(durationMs = 3_599_000L))
    }

    @Test
    fun exactlyOneHour_usesHourFormat() {
        assertEquals("1:00:00", formatDuration(durationMs = 3_600_000L))
    }

    @Test
    fun overOneHour_padsMinutesAndSeconds() {
        assertEquals("1:02:03", formatDuration(durationMs = 3_723_000L))
    }

    @Test
    fun negative_isTreatedAsZero() {
        assertEquals("0:00", formatDuration(durationMs = -1_000L))
    }
}
