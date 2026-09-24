package com.example.skeleton.domain.repository

import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * Remote control for the music player that lives in the background playback service.
 *
 * Every command is fire-and-forget: it never throws. A command sent before the player is
 * connected is kept and run as soon as the connection is ready.
 *
 * Connection contract: the connection is shared by every user, so it is reference-counted.
 * Every connect() must be paired with one release() (for a ViewModel: connect() in `init`,
 * release() in `onCleared()`). The connection is only closed when the last user releases,
 * so one screen going away never cuts off another screen that is still showing.
 *
 * Example:
 * ```
 * playerRepository.connect()
 * playerRepository.playQueue(songs = songs, startIndex = 2)
 * playerRepository.togglePlayPause()
 * playerRepository.release() // later, when this user is done
 * ```
 * @author Phong-Kaster
 */
interface PlayerRepository {

    /**
     * Hot, always-current picture of the player (song, playing or paused, has a queue).
     * Goes back to [PlaybackState.Idle] when the connection is closed or lost.
     */
    val playbackState: StateFlow<PlaybackState>

    /**
     * Adds one user of the player and connects to the playback service if not connected yet.
     * Every call must be paired with exactly one [release].
     */
    fun connect()

    /**
     * Removes one user added by [connect]. The connection closes only when no users are left;
     * a call without a matching [connect] does nothing. The music keeps playing in the service.
     */
    fun release()

    /**
     * Replaces the queue with [songs] and starts playing the one at [startIndex].
     * @param songs The whole list to play, in order.
     * @param startIndex Position (0-based) in [songs] of the song to start with.
     */
    fun playQueue(songs: List<Song>, startIndex: Int)

    /** Pauses when playing, plays when paused. */
    fun togglePlayPause()

    /** Jumps to the next song (wraps to the first after the last). */
    fun next()

    /** Jumps to the previous song (wraps to the last before the first). */
    fun previous()
}
