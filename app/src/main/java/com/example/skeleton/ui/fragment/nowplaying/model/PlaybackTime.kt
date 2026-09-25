package com.example.skeleton.ui.fragment.nowplaying.model

/*
 * Small plain-Kotlin helpers for the Now Playing screen's time line.
 * They use no Android at all, so plain JVM unit tests can check them.
 */

private const val MILLIS_PER_SECOND = 1_000L
private const val SECONDS_PER_MINUTE = 60L
private const val SECONDS_PER_HOUR = 3_600L

/**
 * Turns milliseconds into the clock text a music player shows.
 * Under one hour it is "m:ss", from one hour on it is "h:mm:ss". Negative time shows "0:00".
 *
 * Example:
 * ```kotlin
 * formatPlaybackTime(ms = 65_000L)    // "1:05"
 * formatPlaybackTime(ms = 3_725_000L) // "1:02:05"
 * ```
 *
 * @param ms Time in milliseconds.
 * @author Phong-Kaster
 */
fun formatPlaybackTime(ms: Long): String {
    val totalSeconds = ms.coerceAtLeast(0L) / MILLIS_PER_SECOND
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE

    if (hours > 0L) {
        return "$hours:${twoDigits(value = minutes)}:${twoDigits(value = seconds)}"
    }
    return "$minutes:${twoDigits(value = seconds)}"
}

/**
 * Writes a number with at least two digits, e.g. 5 becomes "05".
 *
 * @param value A number from 0 to 59.
 * @author Phong-Kaster
 */
private fun twoDigits(value: Long): String = value.toString().padStart(length = 2, padChar = '0')

/**
 * Turns a slider spot (0 = start, 1 = end) into a time inside the song.
 * The spot is kept between 0 and 1 first. When the length is unknown (0 or less) it returns 0.
 *
 * Example:
 * ```kotlin
 * positionForFraction(fraction = 0.5f, durationMs = 200_000L) // 100_000
 * ```
 *
 * @param fraction Where the slider thumb is, from 0 to 1.
 * @param durationMs Length of the song in milliseconds.
 * @author Phong-Kaster
 */
fun positionForFraction(fraction: Float, durationMs: Long): Long {
    if (durationMs <= 0L) return 0L
    val safeFraction = fraction.coerceIn(0f, 1f)
    return (safeFraction.toDouble() * durationMs).toLong().coerceIn(0L, durationMs)
}

/**
 * Turns a time inside the song into a slider spot (0 = start, 1 = end).
 * The answer always stays between 0 and 1. When the length is unknown (0 or less) it returns 0.
 *
 * Example:
 * ```kotlin
 * fractionForPosition(positionMs = 50_000L, durationMs = 200_000L) // 0.25f
 * ```
 *
 * @param positionMs Where the player is, in milliseconds.
 * @param durationMs Length of the song in milliseconds.
 * @author Phong-Kaster
 */
fun fractionForPosition(positionMs: Long, durationMs: Long): Float {
    if (durationMs <= 0L) return 0f
    return (positionMs.toDouble() / durationMs).toFloat().coerceIn(0f, 1f)
}

/**
 * Answers: should the screen keep asking the player for the newest position?
 * Only while a song is loaded AND music is meant to be playing; a paused song does not move.
 *
 * @param isPlaying True while music is meant to be playing.
 * @param hasSong True while a song is loaded in the player.
 * @author Phong-Kaster
 */
fun shouldTickPosition(isPlaying: Boolean, hasSong: Boolean): Boolean = isPlaying && hasSong
