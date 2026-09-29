package com.example.skeleton

import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.RepeatMode
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.fragment.nowplaying.NowPlayingUiState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks the ready-to-show values of the Now Playing screen: seek bar spot, time labels,
 * empty state, unknown artist and the active-mode flags.
 *
 * @author Phong-Kaster
 */
class NowPlayingUiStateTest {

    private val sampleSong = Song(
        id = 1L,
        title = "Blue Sky",
        artist = "The Band",
        album = "Summer",
        durationMs = 215_000L,
        contentUri = "content://media/external/audio/media/1",
        artworkUri = null,
    )

    @Test
    fun defaults_showNothingPlaying() {
        val state = NowPlayingUiState()

        assertFalse(state.hasSong)
        assertFalse(state.canSeek)
        assertFalse(state.canSkipToPrevious)
        assertFalse(state.canSkipToNext)
        assertEquals(0f, state.seekFraction, 0.0001f)
        assertEquals("0:00", state.elapsedText)
        assertEquals("0:00", state.totalText)
    }

    @Test
    fun halfway_givesHalfFractionAndLabels() {
        val state = NowPlayingUiState(
            playback = PlaybackState(currentSong = sampleSong, positionMs = 61_000L, durationMs = 122_000L),
        )

        assertTrue(state.hasSong)
        assertTrue(state.canSeek)
        assertEquals(0.5f, state.seekFraction, 0.0001f)
        assertEquals("1:01", state.elapsedText)
        assertEquals("2:02", state.totalText)
    }

    @Test
    fun unknownPlayerDuration_fallsBackToSongDuration() {
        val state = NowPlayingUiState(
            playback = PlaybackState(currentSong = sampleSong, positionMs = 0L, durationMs = 0L),
        )

        assertEquals(215_000L, state.totalDurationMs)
        assertEquals("3:35", state.totalText)
        assertTrue(state.canSeek)
    }

    @Test
    fun positionPastEnd_isClampedToEnd() {
        val state = NowPlayingUiState(
            playback = PlaybackState(currentSong = sampleSong, positionMs = 999_000L, durationMs = 120_000L),
        )

        assertEquals(1f, state.seekFraction, 0.0001f)
        assertEquals("2:00", state.elapsedText)
    }

    @Test
    fun negativePosition_isClampedToStart() {
        val state = NowPlayingUiState(
            playback = PlaybackState(currentSong = sampleSong, positionMs = -5_000L, durationMs = 120_000L),
        )

        assertEquals(0f, state.seekFraction, 0.0001f)
        assertEquals("0:00", state.elapsedText)
    }

    @Test
    fun positionForFraction_mapsAndClamps() {
        val state = NowPlayingUiState(
            playback = PlaybackState(currentSong = sampleSong, durationMs = 200_000L),
        )

        assertEquals(50_000L, state.positionForFraction(fraction = 0.25f))
        assertEquals(0L, state.positionForFraction(fraction = -1f))
        assertEquals(200_000L, state.positionForFraction(fraction = 2f))
        assertEquals("1:40", state.elapsedTextForFraction(fraction = 0.5f))
    }

    @Test
    fun unknownArtist_isDetected() {
        val blank = NowPlayingUiState(playback = PlaybackState(currentSong = sampleSong.copy(artist = " ")))
        val mediaStoreUnknown = NowPlayingUiState(playback = PlaybackState(currentSong = sampleSong.copy(artist = "<unknown>")))
        val known = NowPlayingUiState(playback = PlaybackState(currentSong = sampleSong))

        assertTrue(blank.isArtistUnknown)
        assertTrue(mediaStoreUnknown.isArtistUnknown)
        assertFalse(known.isArtistUnknown)
        assertEquals("Blue Sky", known.title)
        assertEquals("The Band", known.artist)
    }

    @Test
    fun repeatActive_onlyWhenNotOff() {
        val off = NowPlayingUiState(playback = PlaybackState(currentSong = sampleSong, repeatMode = RepeatMode.Off))
        val all = NowPlayingUiState(playback = PlaybackState(currentSong = sampleSong, repeatMode = RepeatMode.All))
        val one = NowPlayingUiState(playback = PlaybackState(currentSong = sampleSong, repeatMode = RepeatMode.One))

        assertFalse(off.isRepeatActive)
        assertTrue(all.isRepeatActive)
        assertTrue(one.isRepeatActive)
    }

    @Test
    fun shuffleAndNext_followPlayback() {
        val state = NowPlayingUiState(
            playback = PlaybackState(
                currentSong = sampleSong,
                currentIndex = 2,
                queueSize = 3,
                shuffleEnabled = true,
                isPlaying = true,
            ),
        )

        assertTrue(state.shuffleEnabled)
        assertTrue(state.isPlaying)
        assertTrue(state.canSkipToNext)
        assertTrue(state.canSkipToPrevious)
    }

    @Test
    fun lastSong_noShuffleNoRepeat_cannotSkipToNext() {
        val state = NowPlayingUiState(
            playback = PlaybackState(currentSong = sampleSong, currentIndex = 2, queueSize = 3),
        )

        assertFalse(state.canSkipToNext)
        assertTrue(state.canSkipToPrevious)
    }
}
