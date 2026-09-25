package com.example.skeleton.data.mapper

import com.example.skeleton.data.mediastore.SongRow
import com.example.skeleton.domain.model.Song

/** MediaStore writes this word when it does not know the artist or album. */
private const val MEDIA_STORE_UNKNOWN = "<unknown>"

/**
 * Turns a raw MediaStore [SongRow] into a clean [Song].
 *
 * Rules (simple story):
 * - A song that lasts 0 ms or less is broken, so we return null and skip it.
 * - No title? We use the file name without its extension ("my_song.mp3" -> "my_song").
 * - Still no name (both blank, or the file is just ".mp3")? We return null and skip it.
 * - Artist blank or "<unknown>"? We say null, so the screen can show "Unknown artist".
 *
 * Example:
 * ```kotlin
 * val songs = rows.mapNotNull { row -> row.toDomain() }
 * ```
 *
 * @return the cleaned [Song], or null when the row is not a playable song.
 * @author Phong-Kaster
 */
fun SongRow.toDomain(): Song? {
    if (durationMs <= 0L) return null
    // No usable name at all (no title and no file name, or a file called ".mp3"): an empty
    // row would be confusing, so we skip the song.
    val resolvedTitle = resolveTitle(title = title, displayName = displayName)
    if (resolvedTitle.isBlank()) return null

    return Song(
        id = id,
        title = resolvedTitle,
        artist = cleanTag(value = artist),
        album = cleanTag(value = album),
        durationMs = durationMs,
        contentUri = contentUri,
    )
}

/**
 * Picks the title to show: the real title when present, otherwise the file name
 * without its extension, otherwise an empty text.
 *
 * @author Phong-Kaster
 */
private fun resolveTitle(title: String?, displayName: String?): String {
    if (!title.isNullOrBlank()) return title.trim()
    if (displayName.isNullOrBlank()) return ""
    return displayName.trim().substringBeforeLast(delimiter = ".").trim()
}

/**
 * Returns null for blank text or MediaStore's "<unknown>" placeholder, otherwise the trimmed text.
 *
 * @author Phong-Kaster
 */
private fun cleanTag(value: String?): String? {
    if (value.isNullOrBlank()) return null
    if (value.trim() == MEDIA_STORE_UNKNOWN) return null
    return value.trim()
}
