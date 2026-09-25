package com.example.skeleton.service

/**
 * Plain rules that decide when the music service should stop by itself.
 * It has no Android code, so it can be tested with plain JUnit.
 *
 * Example:
 * ```kotlin
 * val stop = PlaybackStopPolicy.shouldStopOnTaskRemoved(isPlaybackOngoing = false, mediaItemCount = 3) // true
 * ```
 *
 * @author Phong-Kaster
 */
object PlaybackStopPolicy {

    /**
     * Answers: the user swiped the app away from Recents - should the music service stop now?
     *
     * - Music is playing and there are songs -> keep going (false), the user still listens.
     * - Music is paused -> stop (true), so the notification goes away.
     * - The queue is empty -> stop (true), there is nothing to play.
     *
     * @param isPlaybackOngoing True when the player wants to play and is not idle or finished.
     * Pass this, not `Player.isPlaying`: `isPlaying` is false during a phone call (audio focus
     * lost for a moment), and a swipe then must not stop the music.
     * @param mediaItemCount How many songs are in the player's queue.
     * @author Phong-Kaster
     */
    fun shouldStopOnTaskRemoved(isPlaybackOngoing: Boolean, mediaItemCount: Int): Boolean {
        if (mediaItemCount <= 0) return true
        return !isPlaybackOngoing
    }
}
