package com.example.skeleton

import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.fragment.library.LibraryUiState
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Checks when the Library screen shows its "no songs" message.
 *
 * @author Phong-Kaster
 */
class LibraryUiStateTest {

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
    fun granted_notLoading_empty_showsEmptyState() {
        val state = LibraryUiState(hasPermission = true, isLoading = false, songs = emptyList())

        assertTrue(state.showEmptyState)
    }

    @Test
    fun loading_hidesEmptyState() {
        val state = LibraryUiState(hasPermission = true, isLoading = true, songs = emptyList())

        assertFalse(state.showEmptyState)
    }

    @Test
    fun notGranted_hidesEmptyState() {
        val state = LibraryUiState(hasPermission = false, isLoading = false, songs = emptyList())

        assertFalse(state.showEmptyState)
    }

    @Test
    fun hasSongs_hidesEmptyState() {
        val state = LibraryUiState(hasPermission = true, isLoading = false, songs = listOf(sampleSong))

        assertFalse(state.showEmptyState)
    }

    @Test
    fun defaults_hideEmptyState() {
        assertFalse(LibraryUiState().showEmptyState)
    }
}
