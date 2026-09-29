package com.example.skeleton.ui.fragment.nowplaying

import com.example.skeleton.core.extension.date_and_time.formatDuration
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.RepeatMode

/** The text MediaStore writes when it does not know the artist. */
private const val MEDIA_STORE_UNKNOWN = "<unknown>"

/**
 * UI state for the Now Playing screen. It is just the player's picture ([playback])
 * plus a few ready-to-show values computed from it, so the Layout never does maths.
 *
 * Example:
 * ```kotlin
 * val uiState = NowPlayingUiState(playback = PlaybackState(currentSong = song, positionMs = 61_000L, durationMs = 122_000L))
 * uiState.elapsedText  // "1:01"
 * uiState.totalText    // "2:02"
 * uiState.seekFraction // 0.5f
 * ```
 *
 * @param playback what the player is doing right now (song, position, modes).
 * @author Phong-Kaster
 */
data class NowPlayingUiState(
    val playback: PlaybackState = PlaybackState(),
) {
    /** True when a song is loaded; false shows the "nothing is playing" message. */
    val hasSong: Boolean = playback.currentSong != null

    /** Song name, or empty text when nothing is loaded. */
    val title: String = playback.currentSong?.title.orEmpty()

    /** Raw artist text, or empty text when nothing is loaded. Check [isArtistUnknown] before showing it. */
    val artist: String = playback.currentSong?.artist.orEmpty()

    /** True when the artist is blank or MediaStore's "<unknown>"; the UI then shows "Unknown artist". */
    val isArtistUnknown: Boolean = artist.isBlank() || artist == MEDIA_STORE_UNKNOWN

    /** True while sound is coming out; decides the Play / Pause icon. */
    val isPlaying: Boolean = playback.isPlaying

    /**
     * Length of the song in milliseconds. The player may not know it for a moment after a song
     * starts (0), so we fall back to the length MediaStore gave us.
     */
    val totalDurationMs: Long =
        if (playback.durationMs > 0L) playback.durationMs else playback.currentSong?.durationMs?.coerceAtLeast(0L) ?: 0L

    /** How far into the song we are, never below 0 and never past the end. */
    val elapsedMs: Long = playback.positionMs.coerceIn(0L, totalDurationMs)

    /** Where the seek bar knob sits, from 0f (start) to 1f (end). 0f when the length is unknown. */
    val seekFraction: Float =
        if (totalDurationMs <= 0L) 0f else (elapsedMs.toFloat() / totalDurationMs.toFloat()).coerceIn(0f, 1f)

    /** True when the seek bar may be dragged: a song is loaded and its length is known. */
    val canSeek: Boolean = hasSong && totalDurationMs > 0L

    /** Clock text of the elapsed time, e.g. "1:01". */
    val elapsedText: String = formatDuration(durationMs = elapsedMs)

    /** Clock text of the whole song, e.g. "3:35". */
    val totalText: String = formatDuration(durationMs = totalDurationMs)

    /** True when "Next" can go somewhere. */
    val canSkipToNext: Boolean = playback.hasNext

    /** "Previous" always works with a song loaded: at worst it restarts the current song. */
    val canSkipToPrevious: Boolean = hasSong

    /** True when songs play in random order; the shuffle button is highlighted. */
    val shuffleEnabled: Boolean = playback.shuffleEnabled

    /** Current repeat mode; the repeat button's icon and description follow it. */
    val repeatMode: RepeatMode = playback.repeatMode

    /** True when repeat is All or One; the repeat button is highlighted. */
    val isRepeatActive: Boolean = repeatMode != RepeatMode.Off

    /**
     * Turns a seek bar position (0f..1f) into milliseconds inside the song.
     *
     * Example:
     * ```kotlin
     * NowPlayingUiState(playback = PlaybackState(durationMs = 200_000L)).positionForFraction(fraction = 0.25f) // 50_000L
     * ```
     *
     * @param fraction knob position, clamped to 0f..1f.
     * @return position in milliseconds.
     * @author Phong-Kaster
     */
    fun positionForFraction(fraction: Float): Long {
        return (fraction.coerceIn(0f, 1f) * totalDurationMs).toLong()
    }

    /**
     * Clock text for a seek bar position, used while the user drags the knob.
     *
     * Example:
     * ```kotlin
     * NowPlayingUiState(playback = PlaybackState(durationMs = 120_000L)).elapsedTextForFraction(fraction = 0.5f) // "1:00"
     * ```
     *
     * @param fraction knob position, clamped to 0f..1f.
     * @return clock-style text.
     * @author Phong-Kaster
     */
    fun elapsedTextForFraction(fraction: Float): String {
        return formatDuration(durationMs = positionForFraction(fraction = fraction))
    }
}
