package com.example.skeleton.domain.model

/**
 * A snapshot of "what is the player doing right now?". Plain Kotlin (no Android types).
 * The real queue lives inside the player of the playback service; this is only a picture of it.
 *
 * Example:
 * ```kotlin
 * val state = PlaybackState(currentSong = song, isPlaying = true, positionMs = 30_000L, durationMs = 120_000L,
 *     currentIndex = 0, queueSize = 3)
 * state.progressFraction // 0.25f
 * state.hasNext          // true
 * state.hasPrevious      // false
 * ```
 *
 * @param currentSong the song loaded in the player, or null when nothing is loaded.
 * @param isPlaying true while the player is playing or trying to (e.g. buffering), so the play/pause button matches what a tap will do.
 * @param positionMs how far into the song we are, in milliseconds.
 * @param durationMs how long the song is, in milliseconds (0 when unknown).
 * @param currentIndex position of [currentSong] in the queue; -1 when the queue is empty.
 * @param queueSize how many songs are in the queue.
 * @param shuffleEnabled true when songs are played in a random order.
 * @param repeatMode how songs repeat.
 * @author Phong-Kaster
 */
data class PlaybackState(
    val currentSong: Song? = null,
    val isPlaying: Boolean = false,
    val positionMs: Long = 0L,
    val durationMs: Long = 0L,
    val currentIndex: Int = -1,
    val queueSize: Int = 0,
    val shuffleEnabled: Boolean = false,
    val repeatMode: RepeatMode = RepeatMode.Off,
) {
    /** True when [currentIndex] points at a real song in the queue. */
    private val hasValidIndex: Boolean = queueSize > 0 && currentIndex in 0 until queueSize

    /** How much of the song is done, from 0f (start) to 1f (end). 0f when the length is unknown. */
    val progressFraction: Float =
        if (durationMs <= 0L) 0f else (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f)

    /**
     * True when "Next" can go to another song.
     * Repeat All always has a next song. With shuffle on, the random order is not known here,
     * so any queue with 2+ songs counts as having a next one. Otherwise: not the last song.
     */
    val hasNext: Boolean = when {
        hasValidIndex.not() -> false
        repeatMode == RepeatMode.All -> true
        shuffleEnabled -> queueSize > 1
        else -> currentIndex < queueSize - 1
    }

    /**
     * True when "Previous" can go to an earlier song.
     * Repeat All always has one; with shuffle any 2+ song queue counts; otherwise: not the first song.
     */
    val hasPrevious: Boolean = when {
        hasValidIndex.not() -> false
        repeatMode == RepeatMode.All -> true
        shuffleEnabled -> queueSize > 1
        else -> currentIndex > 0
    }
}
