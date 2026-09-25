package com.example.skeleton.ui.fragment.nowplaying.model

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks when the Now Playing screen closes itself: a shown song went away, or connecting
 * finished with nothing loaded — but never while still connecting, and never with a song.
 *
 * @author Phong-Kaster
 */
class NowPlayingCloseRuleTest {

    @Test
    fun stillConnecting_noSong_neverSeen_staysOpen() {
        assertFalse(shouldCloseNowPlaying(hasSeenSong = false, hasSong = false, isConnectAttemptFinished = false))
    }

    @Test
    fun connectFinished_noSong_neverSeen_closes() {
        assertTrue(shouldCloseNowPlaying(hasSeenSong = false, hasSong = false, isConnectAttemptFinished = true))
    }

    @Test
    fun seenSongWentAway_closes() {
        assertTrue(shouldCloseNowPlaying(hasSeenSong = true, hasSong = false, isConnectAttemptFinished = true))
    }

    @Test
    fun seenSongWentAway_duringReconnect_closes() {
        assertTrue(shouldCloseNowPlaying(hasSeenSong = true, hasSong = false, isConnectAttemptFinished = false))
    }

    @Test
    fun songLoaded_connectFinished_staysOpen() {
        assertFalse(shouldCloseNowPlaying(hasSeenSong = true, hasSong = true, isConnectAttemptFinished = true))
    }

    @Test
    fun songLoaded_firstTime_staysOpen() {
        assertFalse(shouldCloseNowPlaying(hasSeenSong = false, hasSong = true, isConnectAttemptFinished = true))
    }

    @Test
    fun songLoaded_whileNotFinished_staysOpen() {
        assertFalse(shouldCloseNowPlaying(hasSeenSong = false, hasSong = true, isConnectAttemptFinished = false))
    }
}
