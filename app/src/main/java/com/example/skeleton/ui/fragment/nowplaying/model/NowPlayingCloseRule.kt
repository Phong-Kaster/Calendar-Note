package com.example.skeleton.ui.fragment.nowplaying.model

/*
 * Plain-Kotlin rule for "should the Now Playing screen close itself?".
 * It uses no Android at all, so plain JVM unit tests can check it.
 */

/**
 * Answers: should the Now Playing screen go back because there is nothing to show?
 *
 * Simple story:
 * - A song is loaded: never close.
 * - No song, but we showed one before: the music was stopped (for example from the
 *   notification), so close.
 * - No song and never saw one: close only once trying to reach the player has finished
 *   (worked with nothing loaded, or failed). While still connecting, "no song" is only the
 *   empty placeholder, so wait.
 *
 * Example:
 * ```kotlin
 * shouldCloseNowPlaying(hasSeenSong = false, hasSong = false, isConnectAttemptFinished = false) // false, still connecting
 * shouldCloseNowPlaying(hasSeenSong = false, hasSong = false, isConnectAttemptFinished = true)  // true, restored empty
 * shouldCloseNowPlaying(hasSeenSong = true, hasSong = false, isConnectAttemptFinished = false)  // true, song went away
 * ```
 *
 * @param hasSeenSong True when this screen has shown a song at least once before.
 * @param hasSong True when the player has a song loaded right now.
 * @param isConnectAttemptFinished True once trying to reach the player has finished, worked or failed.
 * @author Phong-Kaster
 */
fun shouldCloseNowPlaying(
    hasSeenSong: Boolean,
    hasSong: Boolean,
    isConnectAttemptFinished: Boolean,
): Boolean {
    if (hasSong) return false
    return hasSeenSong || isConnectAttemptFinished
}
