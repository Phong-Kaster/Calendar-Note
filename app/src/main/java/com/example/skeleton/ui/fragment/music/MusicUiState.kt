package com.example.skeleton.ui.fragment.music

import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.fragment.music.model.MusicScreenContent

/**
 * UI state for the Music screen.
 *
 * Example:
 * ```kotlin
 * MusicUiState(isPermissionGranted = true, songs = listOf(song)).screenContent // Songs
 * ```
 *
 * @param isPermissionGranted True when the app may read audio files on the phone.
 * @param isLoading True while songs are being read from the phone.
 * @param songs Every song found on the phone, sorted by title.
 * @author Phong-Kaster
 */
data class MusicUiState(
    val isPermissionGranted: Boolean = false,
    val isLoading: Boolean = false,

    // --- Domain data ---
    val songs: List<Song> = emptyList(),
) {
    /**
     * Which body the screen shows. Order matters:
     * no permission wins, then loading, then "no songs", else the list.
     */
    val screenContent: MusicScreenContent
        get() = when {
            !isPermissionGranted -> MusicScreenContent.PermissionDenied
            isLoading -> MusicScreenContent.Loading
            songs.isEmpty() -> MusicScreenContent.Empty
            else -> MusicScreenContent.Songs
        }
}
