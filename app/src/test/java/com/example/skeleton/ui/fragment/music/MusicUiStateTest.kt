package com.example.skeleton.ui.fragment.music

import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.fragment.music.model.MusicScreenContent
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Checks which body the Music screen shows for each state, and the priority order
 * (permission first, then loading, then empty, else songs).
 *
 * @author Phong-Kaster
 */
class MusicUiStateTest {

    private val song = Song(
        id = 1L,
        title = "Yesterday",
        artist = "The Beatles",
        album = "Help!",
        durationMs = 125_000L,
        contentUri = "content://media/external/audio/media/1",
    )

    @Test
    fun defaultState_isPermissionDenied() {
        assertEquals(MusicScreenContent.PermissionDenied, MusicUiState().screenContent)
    }

    @Test
    fun notGranted_winsOverLoadingAndSongs() {
        val state = MusicUiState(isPermissionGranted = false, isLoading = true, songs = listOf(song))
        assertEquals(MusicScreenContent.PermissionDenied, state.screenContent)
    }

    @Test
    fun grantedAndLoading_isLoading() {
        val state = MusicUiState(isPermissionGranted = true, isLoading = true)
        assertEquals(MusicScreenContent.Loading, state.screenContent)
    }

    @Test
    fun grantedAndLoading_withSongs_isStillLoading() {
        val state = MusicUiState(isPermissionGranted = true, isLoading = true, songs = listOf(song))
        assertEquals(MusicScreenContent.Loading, state.screenContent)
    }

    @Test
    fun grantedNotLoadingNoSongs_isEmpty() {
        val state = MusicUiState(isPermissionGranted = true, isLoading = false, songs = emptyList())
        assertEquals(MusicScreenContent.Empty, state.screenContent)
    }

    @Test
    fun grantedNotLoadingWithSongs_isSongs() {
        val state = MusicUiState(isPermissionGranted = true, isLoading = false, songs = listOf(song))
        assertEquals(MusicScreenContent.Songs, state.screenContent)
    }
}
