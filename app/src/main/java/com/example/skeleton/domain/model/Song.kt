package com.example.skeleton.domain.model

/**
 * One song that lives on the phone.
 *
 * Think of it as a small card that says "this is the song, who sings it, how long it is,
 * and where the player can find the file". It holds only plain Kotlin types so it can be
 * used anywhere (tests included) without Android.
 *
 * Example:
 * ```kotlin
 * val song = Song(
 *     id = 42L,
 *     title = "Yesterday",
 *     artist = "The Beatles",
 *     album = "Help!",
 *     durationMs = 125_000L,
 *     contentUri = "content://media/external/audio/media/42",
 * )
 * ```
 *
 * @param id MediaStore row id, unique per song on this phone.
 * @param title Name shown to the user; never blank.
 * @param artist Singer or band; null when the phone does not know it.
 * @param album Album name; null when the phone does not know it.
 * @param durationMs Length of the song in milliseconds; always greater than zero.
 * @param contentUri The `content://` address of the audio file, stored as a String.
 * @author Phong-Kaster
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val contentUri: String,
)
