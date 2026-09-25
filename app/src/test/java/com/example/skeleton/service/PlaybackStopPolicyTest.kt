package com.example.skeleton.service

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks when the music service should stop after the app is swiped away from Recents.
 * @author Phong-Kaster
 */
class PlaybackStopPolicyTest {

    @Test
    fun playingWithSongs_keepsPlaying() {
        assertFalse(PlaybackStopPolicy.shouldStopOnTaskRemoved(isPlaybackOngoing = true, mediaItemCount = 3))
    }

    @Test
    fun pausedWithSongs_stops() {
        assertTrue(PlaybackStopPolicy.shouldStopOnTaskRemoved(isPlaybackOngoing = false, mediaItemCount = 3))
    }

    @Test
    fun pausedWithEmptyQueue_stops() {
        assertTrue(PlaybackStopPolicy.shouldStopOnTaskRemoved(isPlaybackOngoing = false, mediaItemCount = 0))
    }

    @Test
    fun playingWithEmptyQueue_stops() {
        assertTrue(PlaybackStopPolicy.shouldStopOnTaskRemoved(isPlaybackOngoing = true, mediaItemCount = 0))
    }
}
