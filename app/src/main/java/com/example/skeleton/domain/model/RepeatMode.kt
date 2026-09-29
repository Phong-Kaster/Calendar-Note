package com.example.skeleton.domain.model

/**
 * How the player repeats songs. Plain Kotlin, so every layer (and JVM tests) can use it.
 *
 * - [Off]: play the list once and stop at the end.
 * - [All]: when the last song ends, start again from the first one.
 * - [One]: play the same song again and again.
 *
 * Example:
 * ```kotlin
 * var mode = RepeatMode.Off
 * mode = mode.next() // All
 * mode = mode.next() // One
 * mode = mode.next() // Off again
 * ```
 *
 * @author Phong-Kaster
 */
enum class RepeatMode {
    Off,
    All,
    One;

    /**
     * Gives the mode the repeat button should switch to when tapped: Off -> All -> One -> Off.
     *
     * Example:
     * ```kotlin
     * RepeatMode.One.next() // RepeatMode.Off
     * ```
     *
     * @return the next mode in the cycle.
     * @author Phong-Kaster
     */
    fun next(): RepeatMode {
        return when (this) {
            Off -> All
            All -> One
            One -> Off
        }
    }
}
