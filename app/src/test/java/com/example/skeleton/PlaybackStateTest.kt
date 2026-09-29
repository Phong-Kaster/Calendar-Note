package com.example.skeleton

import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.RepeatMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks the derived values of [PlaybackState]: progress, hasNext, hasPrevious.
 *
 * @author Phong-Kaster
 */
class PlaybackStateTest {

    // ---------- progressFraction ----------

    @Test
    fun progress_isPositionOverDuration() {
        val state = PlaybackState(positionMs = 30_000L, durationMs = 120_000L)

        assertEquals(0.25f, state.progressFraction, 0.0001f)
    }

    @Test
    fun progress_unknownDuration_isZero() {
        val state = PlaybackState(positionMs = 30_000L, durationMs = 0L)

        assertEquals(0f, state.progressFraction, 0.0001f)
    }

    @Test
    fun progress_positionPastEnd_isClampedToOne() {
        val state = PlaybackState(positionMs = 200_000L, durationMs = 120_000L)

        assertEquals(1f, state.progressFraction, 0.0001f)
    }

    @Test
    fun progress_negativePosition_isClampedToZero() {
        val state = PlaybackState(positionMs = -5L, durationMs = 120_000L)

        assertEquals(0f, state.progressFraction, 0.0001f)
    }

    // ---------- hasNext / hasPrevious ----------

    @Test
    fun emptyQueue_hasNoNeighbours() {
        val state = PlaybackState()

        assertFalse(state.hasNext)
        assertFalse(state.hasPrevious)
    }

    @Test
    fun firstOfThree_hasNextOnly() {
        val state = PlaybackState(currentIndex = 0, queueSize = 3)

        assertTrue(state.hasNext)
        assertFalse(state.hasPrevious)
    }

    @Test
    fun middleOfThree_hasBoth() {
        val state = PlaybackState(currentIndex = 1, queueSize = 3)

        assertTrue(state.hasNext)
        assertTrue(state.hasPrevious)
    }

    @Test
    fun lastOfThree_hasPreviousOnly() {
        val state = PlaybackState(currentIndex = 2, queueSize = 3)

        assertFalse(state.hasNext)
        assertTrue(state.hasPrevious)
    }

    @Test
    fun repeatAll_alwaysHasBoth() {
        val state = PlaybackState(currentIndex = 2, queueSize = 3, repeatMode = RepeatMode.All)

        assertTrue(state.hasNext)
        assertTrue(state.hasPrevious)
    }

    @Test
    fun repeatOne_behavesLikeOffForNavigation() {
        val state = PlaybackState(currentIndex = 2, queueSize = 3, repeatMode = RepeatMode.One)

        assertFalse(state.hasNext)
        assertTrue(state.hasPrevious)
    }

    @Test
    fun shuffle_withSeveralSongs_hasBoth() {
        val state = PlaybackState(currentIndex = 0, queueSize = 3, shuffleEnabled = true)

        assertTrue(state.hasNext)
        assertTrue(state.hasPrevious)
    }

    @Test
    fun singleSong_hasNoNeighbours() {
        val state = PlaybackState(currentIndex = 0, queueSize = 1, shuffleEnabled = true)

        assertFalse(state.hasNext)
        assertFalse(state.hasPrevious)
    }

    @Test
    fun indexOutsideQueue_hasNoNeighbours() {
        val state = PlaybackState(currentIndex = 5, queueSize = 3)

        assertFalse(state.hasNext)
        assertFalse(state.hasPrevious)
    }
}
