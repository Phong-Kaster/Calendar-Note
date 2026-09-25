package com.example.skeleton.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Checks the next / previous rules of the play queue: wrap at both ends,
 * `null` for an empty queue, and `0` for a one-song queue.
 *
 * @author Phong-Kaster
 */
class PlaybackQueuePolicyTest {

    private val size = 5

    @Test
    fun next_fromFirst_goesToSecond() {
        assertEquals(1, PlaybackQueuePolicy.nextIndex(current = 0, size = size))
    }

    @Test
    fun next_fromMiddle_goesToFollowing() {
        assertEquals(3, PlaybackQueuePolicy.nextIndex(current = 2, size = size))
    }

    @Test
    fun next_fromLast_wrapsToFirst() {
        assertEquals(0, PlaybackQueuePolicy.nextIndex(current = 4, size = size))
    }

    @Test
    fun previous_fromFirst_wrapsToLast() {
        assertEquals(4, PlaybackQueuePolicy.previousIndex(current = 0, size = size))
    }

    @Test
    fun previous_fromMiddle_goesToPreceding() {
        assertEquals(1, PlaybackQueuePolicy.previousIndex(current = 2, size = size))
    }

    @Test
    fun previous_fromLast_goesToSecondLast() {
        assertEquals(3, PlaybackQueuePolicy.previousIndex(current = 4, size = size))
    }

    @Test
    fun next_emptyQueue_isNull() {
        assertNull(PlaybackQueuePolicy.nextIndex(current = 0, size = 0))
    }

    @Test
    fun previous_emptyQueue_isNull() {
        assertNull(PlaybackQueuePolicy.previousIndex(current = 0, size = 0))
    }

    @Test
    fun next_singleSong_staysOnIt() {
        assertEquals(0, PlaybackQueuePolicy.nextIndex(current = 0, size = 1))
    }

    @Test
    fun previous_singleSong_staysOnIt() {
        assertEquals(0, PlaybackQueuePolicy.previousIndex(current = 0, size = 1))
    }
}
