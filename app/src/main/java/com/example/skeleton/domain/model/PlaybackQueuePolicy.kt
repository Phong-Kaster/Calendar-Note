package com.example.skeleton.domain.model

/**
 * The rules for "which song comes next / before" in the play queue.
 *
 * Simple story: the queue is a circle. After the last song comes the first one again,
 * and before the first song comes the last one. "Previous" ALWAYS jumps to the song
 * before, even when the current song is half-way through (it never just restarts it).
 *
 * It is plain Kotlin on purpose, so a JVM unit test can check every rule.
 *
 * Example:
 * ```kotlin
 * PlaybackQueuePolicy.nextIndex(current = 2, size = 3)     // 0 (wraps to the first song)
 * PlaybackQueuePolicy.previousIndex(current = 0, size = 3) // 2 (wraps to the last song)
 * PlaybackQueuePolicy.nextIndex(current = 0, size = 0)     // null (nothing to play)
 * ```
 *
 * @author Phong-Kaster
 */
object PlaybackQueuePolicy {

    /**
     * Index of the song after [current]; wraps from the last song to the first.
     *
     * @param current Index of the song playing now. A value outside the queue is treated as 0.
     * @param size How many songs are in the queue.
     * @return the next index, or null when the queue is empty.
     * @author Phong-Kaster
     */
    fun nextIndex(current: Int, size: Int): Int? {
        if (size <= 0) return null
        val safeCurrent = clampIntoQueue(current = current, size = size)
        return (safeCurrent + 1) % size
    }

    /**
     * Index of the song before [current]; wraps from the first song to the last.
     *
     * @param current Index of the song playing now. A value outside the queue is treated as 0.
     * @param size How many songs are in the queue.
     * @return the previous index, or null when the queue is empty.
     * @author Phong-Kaster
     */
    fun previousIndex(current: Int, size: Int): Int? {
        if (size <= 0) return null
        val safeCurrent = clampIntoQueue(current = current, size = size)
        return (safeCurrent - 1 + size) % size
    }

    /**
     * Returns [current] when it points inside the queue, otherwise 0,
     * so a strange index never produces a strange answer.
     *
     * @author Phong-Kaster
     */
    private fun clampIntoQueue(current: Int, size: Int): Int {
        if (current in 0 until size) return current
        return 0
    }
}
