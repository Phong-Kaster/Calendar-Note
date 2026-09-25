package com.example.skeleton.domain.model

/**
 * A snapshot of what the music player is doing right now.
 *
 * Think of it as the little screen on a music box: which song is loaded,
 * whether it is making sound, and where we are in the list.
 *
 * Example:
 * ```kotlin
 * val nothing = NowPlaying()                       // nothing loaded yet
 * val playing = NowPlaying(currentSong = song, isPlaying = true, currentIndex = 0, queueSize = 10)
 * ```
 *
 * @param currentSong The song loaded in the player; null when nothing is loaded.
 * @param isPlaying True while sound is coming out; false when paused or stopped.
 * @param currentIndex Position of [currentSong] in the queue; -1 when nothing is loaded.
 * @param queueSize How many songs are in the queue.
 * @author Phong-Kaster
 */
data class NowPlaying(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val currentIndex: Int = -1,
    val queueSize: Int = 0,
)
