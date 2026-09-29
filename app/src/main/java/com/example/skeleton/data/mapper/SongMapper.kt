package com.example.skeleton.data.mapper

import com.example.skeleton.domain.model.Song

/**
 * One raw row read from the phone's MediaStore, before it becomes a [Song].
 * It only holds plain values, so it can be tested on the JVM without a phone.
 *
 * @param id MediaStore `_ID`.
 * @param title `TITLE` column (may be null on odd files).
 * @param artist `ARTIST` column (may be null, blank or "<unknown>").
 * @param album `ALBUM` column.
 * @param albumId `ALBUM_ID` column, used to build the cover address.
 * @param durationMs `DURATION` column in milliseconds.
 * @author Phong-Kaster
 */
data class MediaStoreSongRow(
    val id: Long,
    val title: String?,
    val artist: String?,
    val album: String?,
    val albumId: Long,
    val durationMs: Long,
)

/** Base address of every audio file in MediaStore. Same value as `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI`. */
private const val AUDIO_MEDIA_BASE_URI = "content://media/external/audio/media"

/** Base address of album covers in MediaStore. */
private const val ALBUM_ART_BASE_URI = "content://media/external/audio/albumart"

/**
 * Turns a raw MediaStore row into a domain [Song].
 * - The play address is `content://media/external/audio/media/<id>`.
 * - The cover address is `content://media/external/audio/albumart/<albumId>` (null when albumId <= 0).
 * - Artist text is kept raw; the UI decides how to show blank / "<unknown>".
 * - Negative durations become 0.
 *
 * Example:
 * ```kotlin
 * val song = MediaStoreSongRow(id = 5, title = "Hi", artist = "Me", album = "A", albumId = 9, durationMs = 1000).toDomain()
 * // song.contentUri == "content://media/external/audio/media/5"
 * ```
 *
 * @author Phong-Kaster
 */
fun MediaStoreSongRow.toDomain(): Song {
    val artworkUri = if (albumId > 0L) "$ALBUM_ART_BASE_URI/$albumId" else null
    return Song(
        id = id,
        title = title.orEmpty(),
        artist = artist.orEmpty(),
        album = album.orEmpty(),
        durationMs = durationMs.coerceAtLeast(0L),
        contentUri = "$AUDIO_MEDIA_BASE_URI/$id",
        artworkUri = artworkUri,
    )
}
