package com.example.skeleton.ui.fragment.music

import com.example.skeleton.domain.model.NowPlaying
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
 * @param nowPlaying What the music player is doing right now (drives the now-playing bar).
 * @author Phong-Kaster
 */
data class MusicUiState(
    val isPermissionGranted: Boolean = false,
    val isLoading: Boolean = false,

    // --- Domain data ---
    val songs: List<Song> = emptyList(),
    val nowPlaying: NowPlaying = NowPlaying(),
) {
    /** Id of the song loaded in the player (its row is highlighted); null when nothing is loaded. */
    val currentSongId: Long?
        get() = nowPlaying.currentSong?.id

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
