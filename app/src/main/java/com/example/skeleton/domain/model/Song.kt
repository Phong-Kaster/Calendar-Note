package com.example.skeleton.domain.model

/**
 * One song found on the phone. This is plain Kotlin (no Android types), so every layer can use it.
 * Links to the audio file and cover picture are kept as simple `String`s.
 *
 * Example:
 * ```kotlin
 * val song = Song(
 *     id = 1L,
 *     title = "Blue Sky",
 *     artist = "The Band",
 *     album = "Summer",
 *     durationMs = 215_000L,
 *     contentUri = "content://media/external/audio/media/1",
 *     artworkUri = "content://media/external/audio/albumart/7",
 * )
 * ```
 *
 * @param id MediaStore id of the song.
 * @param title song name.
 * @param artist raw artist text from the phone; may be blank or "<unknown>" (the UI shows a fallback).
 * @param album album name.
 * @param durationMs how long the song is, in milliseconds.
 * @param contentUri the address used to play the file.
 * @param artworkUri the address of the album cover, or null when unknown.
 * @author Phong-Kaster
 */
data class Song(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val durationMs: Long,
    val contentUri: String,
    val artworkUri: String?,
)
