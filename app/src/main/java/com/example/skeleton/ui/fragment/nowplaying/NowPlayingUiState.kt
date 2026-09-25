package com.example.skeleton.ui.fragment.nowplaying

import com.example.skeleton.domain.model.NowPlaying

/**
 * UI state for the Now Playing screen.
 *
 * @param nowPlaying What the player is doing: song, play/pause, song length.
 * @param positionMs Where the player is inside the song, in milliseconds (refreshed about twice a second while playing).
 * @param isSongGone True once a song was shown and then the player has no song anymore; the screen closes itself.
 * @author Phong-Kaster
 */
data class NowPlayingUiState(
    val nowPlaying: NowPlaying = NowPlaying(),
    val positionMs: Long = 0L,
    val isSongGone: Boolean = false,
) {
    /** Song length: the player's own value, or the length stored with the song while the player does not know it yet. */
    val durationMs: Long =
        if (nowPlaying.durationMs > 0L) nowPlaying.durationMs
        else nowPlaying.currentSong?.durationMs ?: 0L
}
