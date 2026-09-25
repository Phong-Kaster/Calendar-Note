package com.example.skeleton.ui.fragment.nowplaying.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks the Now Playing time helpers: clock text, slider spot to time, time to slider spot,
 * and when the position should keep refreshing.
 *
 * @author Phong-Kaster
 */
class PlaybackTimeTest {

    @Test
    fun format_zero_isZeroClock() {
        assertEquals("0:00", formatPlaybackTime(ms = 0L))
    }

    @Test
    fun format_underOneHour_isMinutesAndSeconds() {
        assertEquals("1:05", formatPlaybackTime(ms = 65_000L))
    }

    @Test
    fun format_fromOneHour_isHoursMinutesSeconds() {
        assertEquals("1:02:05", formatPlaybackTime(ms = 3_725_000L))
    }

    @Test
    fun format_negative_isZeroClock() {
        assertEquals("0:00", formatPlaybackTime(ms = -5_000L))
    }

    @Test
    fun positionForFraction_half_isHalfTheDuration() {
        assertEquals(100_000L, positionForFraction(fraction = 0.5f, durationMs = 200_000L))
    }

    @Test
    fun positionForFraction_belowZero_isZero() {
        assertEquals(0L, positionForFraction(fraction = -0.3f, durationMs = 200_000L))
    }

    @Test
    fun positionForFraction_aboveOne_isDuration() {
        assertEquals(200_000L, positionForFraction(fraction = 1.7f, durationMs = 200_000L))
    }

    @Test
    fun positionForFraction_zeroDuration_isZero() {
        assertEquals(0L, positionForFraction(fraction = 0.5f, durationMs = 0L))
    }

    @Test
    fun fractionForPosition_zeroDuration_isZero() {
        assertEquals(0f, fractionForPosition(positionMs = 5_000L, durationMs = 0L), 0f)
    }

    @Test
    fun fractionForPosition_quarter_isQuarter() {
        assertEquals(0.25f, fractionForPosition(positionMs = 50_000L, durationMs = 200_000L), 0.0001f)
    }

    @Test
    fun fractionForPosition_pastTheEnd_isOne() {
        assertEquals(1f, fractionForPosition(positionMs = 300_000L, durationMs = 200_000L), 0f)
    }

    @Test
    fun fractionForPosition_negative_isZero() {
        assertEquals(0f, fractionForPosition(positionMs = -1_000L, durationMs = 200_000L), 0f)
    }

    @Test
    fun shouldTick_playingWithSong_isTrue() {
        assertTrue(shouldTickPosition(isPlaying = true, hasSong = true))
    }

    @Test
    fun shouldTick_pausedWithSong_isFalse() {
        assertFalse(shouldTickPosition(isPlaying = false, hasSong = true))
    }

    @Test
    fun shouldTick_playingWithoutSong_isFalse() {
        assertFalse(shouldTickPosition(isPlaying = true, hasSong = false))
    }

    @Test
    fun shouldTick_pausedWithoutSong_isFalse() {
        assertFalse(shouldTickPosition(isPlaying = false, hasSong = false))
    }
}
