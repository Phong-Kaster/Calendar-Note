package com.example.skeleton.domain.model

/**
 * What the music player is doing right now, in plain Kotlin (no Android types).
 *
 * Example: `PlaybackState(songId = 42, title = "Yesterday", artist = "The Beatles", isPlaying = true, hasQueue = true)`
 *
 * @param songId Id of the song that is loaded in the player, or null when nothing is loaded.
 * @param title Title of the loaded song, or null when unknown / nothing is loaded.
 * @param artist Artist of the loaded song, or null when unknown / nothing is loaded.
 * @param isPlaying True while the player is playing (or about to play); false when paused or stopped.
 * @param hasQueue True when the player holds at least one song, so the now-playing bar can show.
 * @author Phong-Kaster
 */
data class PlaybackState(
    val songId: Long? = null,
    val title: String? = null,
    val artist: String? = null,
    val isPlaying: Boolean = false,
    val hasQueue: Boolean = false,
) {
    companion object {
        /** Nothing loaded, nothing playing: the state before the first song is tapped. */
        val Idle = PlaybackState()
    }
}
