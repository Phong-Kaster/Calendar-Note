package com.example.skeleton.domain.repository

import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.RepeatMode
import com.example.skeleton.domain.model.Song
import kotlinx.coroutines.flow.StateFlow

/**
 * The remote control of the music player. The real player (and its queue) lives in the
 * playback service, so the notification, lock screen and headset buttons control the same songs.
 *
 * Every command is a plain `fun`: call it directly from the ViewModel (NOT inside
 * `Dispatchers.IO`). The implementation moves each command to the main thread by itself.
 * Commands sent before the player is connected are kept and run once it is ready.
 *
 * Example:
 * ```kotlin
 * playerRepository.playQueue(songs = uiState.songs, startIndex = 2)
 * playerRepository.skipToNext()
 * playerRepository.state.collectLatest { playback -> println(playback.currentSong?.title) }
 * ```
 *
 * @author Phong-Kaster
 */
interface PlayerRepository {

    /** Hot, always-up-to-date picture of the player. Position updates about twice a second while playing. */
    val state: StateFlow<PlaybackState>

    // ---------- Queue ----------

    /** Replaces the queue with [songs] and starts playing the song at [startIndex]. Empty list = ignored. */
    fun playQueue(songs: List<Song>, startIndex: Int)

    // ---------- Transport ----------

    /** Pauses when playing; plays (restarting a finished queue) when paused. */
    fun togglePlayPause()

    /** Goes to the next song in the queue. */
    fun skipToNext()

    /** Goes to the previous song (or back to the start of the current one, like every music app). */
    fun skipToPrevious()

    /** Jumps to [positionMs] milliseconds inside the current song. */
    fun seekTo(positionMs: Long)

    // ---------- Modes ----------

    /** Turns random order on or off. */
    fun setShuffleEnabled(enabled: Boolean)

    /** Chooses how songs repeat. */
    fun setRepeatMode(mode: RepeatMode)
}
