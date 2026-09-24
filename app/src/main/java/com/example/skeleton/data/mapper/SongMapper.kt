package com.example.skeleton.data.mapper

import com.example.skeleton.domain.model.Song

/*
 * This file is plain Kotlin on purpose (no android.* imports) so every rule in it can be
 * checked with a normal JVM unit test. The repository reads the real Cursor and hands the
 * raw values over as a [SongRow].
 */

/**
 * The question we ask the device music library: which columns, which rows, which order.
 * The column names are the literal values of the `MediaStore.Audio.Media` constants.
 *
 * Example: `contentResolver.query(uri, SongQuery.projection, SongQuery.selection, null, SongQuery.sortOrder)`
 * @author Phong-Kaster
 */
object SongQuery {
    /** Same value as `MediaStore.Audio.Media._ID`. */
    const val COLUMN_ID = "_id"

    /** Same value as `MediaStore.Audio.Media.TITLE`. */
    const val COLUMN_TITLE = "title"

    /** Same value as `MediaStore.Audio.Media.ARTIST`. */
    const val COLUMN_ARTIST = "artist"

    /** Same value as `MediaStore.Audio.Media.DURATION`. */
    const val COLUMN_DURATION = "duration"

    /** Same value as `MediaStore.Audio.Media.IS_MUSIC`. */
    const val COLUMN_IS_MUSIC = "is_music"

    /** The columns we read for each song. */
    val projection: Array<String> = arrayOf(COLUMN_ID, COLUMN_TITLE, COLUMN_ARTIST, COLUMN_DURATION)

    /** Only real music: skips ringtones, notification sounds and voice notes. */
    const val selection: String = "$COLUMN_IS_MUSIC != 0"

    /** Alphabetical by song title. */
    const val sortOrder: String = "$COLUMN_TITLE ASC"
}

/**
 * The raw values of one row read from the music library, before any cleaning.
 *
 * Example: `SongRow(id = 7, title = "Song", artist = "<unknown>", durationMs = 185_000)`
 * @param id Row id (`_id`).
 * @param title Raw title, may be null or blank.
 * @param artist Raw artist, may be null, blank or the system placeholder `<unknown>`.
 * @param durationMs Raw duration in milliseconds, may be null.
 * @author Phong-Kaster
 */
data class SongRow(
    val id: Long,
    val title: String?,
    val artist: String?,
    val durationMs: Long?,
)

/** The text Android writes into the artist column when it does not know the artist. */
const val UNKNOWN_ARTIST_PLACEHOLDER = "<unknown>"

/**
 * Turns a raw [SongRow] into a clean [Song].
 *
 * Rules:
 * - title: trimmed; null or blank becomes "" (the UI shows a localized "Unknown title" text for "").
 * - artist: trimmed; null, blank or `<unknown>` becomes null (the UI shows "Unknown artist").
 * - duration: null or negative becomes 0.
 * - contentUri: "[contentUriBase]/[SongRow.id]", e.g. "content://media/external/audio/media/7".
 *
 * @param contentUriBase Text of `MediaStore.Audio.Media.EXTERNAL_CONTENT_URI`.
 * @author Phong-Kaster
 */
fun SongRow.toDomain(contentUriBase: String): Song {
    val cleanArtist = artist?.trim()
    val artistOrNull = if (cleanArtist.isNullOrBlank() || cleanArtist == UNKNOWN_ARTIST_PLACEHOLDER) {
        null
    } else {
        cleanArtist
    }

    return Song(
        id = id,
        title = title?.trim().orEmpty(),
        artist = artistOrNull,
        durationMs = (durationMs ?: 0L).coerceAtLeast(minimumValue = 0L),
        contentUri = "${contentUriBase.trimEnd('/')}/$id",
    )
}

/**
 * Formats a duration as "m:ss" (minutes are not padded, seconds are always two digits).
 *
 * Examples: 185000 → "3:05", 0 → "0:00", 3_600_000 → "60:00", negative → "0:00".
 * @param durationMs Duration in milliseconds.
 * @author Phong-Kaster
 */
fun formatDuration(durationMs: Long): String {
    val totalSeconds = durationMs.coerceAtLeast(minimumValue = 0L) / 1000L
    val minutes = totalSeconds / 60L
    val seconds = totalSeconds % 60L
    return "$minutes:${seconds.toString().padStart(length = 2, padChar = '0')}"
}
