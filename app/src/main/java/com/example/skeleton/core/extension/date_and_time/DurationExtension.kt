package com.example.skeleton.core.extension.date_and_time

/** How many milliseconds are in one second. */
private const val MILLIS_PER_SECOND = 1_000L

/** How many seconds are in one minute. */
private const val SECONDS_PER_MINUTE = 60L

/** How many seconds are in one hour. */
private const val SECONDS_PER_HOUR = 3_600L

/**
 * Turns a length of time in milliseconds into the short clock text music apps show.
 * - Under one hour: `m:ss` (for example `3:05`).
 * - One hour or more: `h:mm:ss` (for example `1:02:09`).
 * - Negative numbers are treated as 0, so the result is never odd-looking.
 *
 * Example:
 * ```kotlin
 * formatDuration(durationMs = 61_000L)     // "1:01"
 * formatDuration(durationMs = 3_723_000L)  // "1:02:03"
 * ```
 *
 * @param durationMs time in milliseconds.
 * @return clock-style text.
 * @author Phong-Kaster
 */
fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(0L) / MILLIS_PER_SECOND
    val hours = totalSeconds / SECONDS_PER_HOUR
    val minutes = (totalSeconds % SECONDS_PER_HOUR) / SECONDS_PER_MINUTE
    val seconds = totalSeconds % SECONDS_PER_MINUTE
    val secondsText = seconds.toString().padStart(length = 2, padChar = '0')

    if (hours > 0L) {
        val minutesText = minutes.toString().padStart(length = 2, padChar = '0')
        return "$hours:$minutesText:$secondsText"
    }
    return "$minutes:$secondsText"
}
