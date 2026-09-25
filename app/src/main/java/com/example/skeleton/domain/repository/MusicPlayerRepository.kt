package com.example.skeleton.domain.repository

import com.example.skeleton.domain.model.NowPlaying
import com.example.skeleton.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * The remote control of the music player.
 *
 * Screens use it to start songs, pause, and skip, and they watch [state] to draw
 * the "now playing" bar. The real player lives in a background service, so music
 * keeps going while the user moves between screens.
 *
 * Every [connect] must be paired with exactly one [release]. The connection stays open
 * while at least one screen still holds it, so one screen closing never breaks another.
 *
 * All functions must be called from the main thread.
 *
 * Example:
 * ```kotlin
 * musicPlayerRepository.connect()
 * musicPlayerRepository.playSongs(songs = songs, startIndex = 3)
 * musicPlayerRepository.release()
 * ```
 *
 * @author Phong-Kaster
 */
interface MusicPlayerRepository {

    /** What is playing right now. Starts as an empty [NowPlaying]. */
    val state: StateFlow<NowPlaying>

    /** Opens (or shares) the connection to the player. Pair with [release]. */
    fun connect()

    /** Gives back one [connect]. The connection closes when nobody holds it anymore. */
    fun release()

    /**
     * Replaces the queue with [songs] and starts playing the song at [startIndex],
     * then keeps going through the list in order.
     */
    fun playSongs(songs: List<Song>, startIndex: Int)

    /** Pauses when playing, plays when paused. */
    fun togglePlayPause()

    /** Jumps to the next song (after the last song comes the first). */
    fun skipToNext()

    /** Jumps to the previous song (before the first song comes the last). */
    fun skipToPrevious()
}
