package com.example.skeleton.data.mediastore

/**
 * One raw row read from MediaStore, before any cleaning.
 *
 * The values are copied exactly as the phone gives them (they may be null, blank or
 * "<unknown>"). The mapper `SongRow.toDomain()` turns this into a clean `Song`.
 * It has no Android types on purpose, so unit tests can build it on a plain JVM.
 *
 * Example:
 * ```kotlin
 * val row = SongRow(
 *     id = 7L,
 *     title = null,
 *     artist = "<unknown>",
 *     album = null,
 *     durationMs = 180_000L,
 *     displayName = "my_song.mp3",
 *     contentUri = "content://media/external/audio/media/7",
 * )
 * ```
 *
 * @param id MediaStore `_ID` column.
 * @param title MediaStore `TITLE` column.
 * @param artist MediaStore `ARTIST` column.
 * @param album MediaStore `ALBUM` column.
 * @param durationMs MediaStore `DURATION` column, in milliseconds.
 * @param displayName MediaStore `DISPLAY_NAME` column (the file name, e.g. "song.mp3").
 * @param contentUri The `content://` address built from [id], as a String.
 * @param albumId MediaStore `ALBUM_ID` column; null when the phone does not know the album.
 * @author Phong-Kaster
 */
data class SongRow(
    val id: Long,
    val title: String?,
    val artist: String?,
    val album: String?,
    val durationMs: Long,
    val displayName: String?,
    val contentUri: String,
    val albumId: Long? = null,
)
