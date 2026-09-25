package com.example.skeleton.data.mapper

/** The folder where Android keeps the cover picture of every album. */
private const val ALBUM_ART_BASE_URI = "content://media/external/audio/albumart"

/**
 * Builds the address of an album's cover picture from its MediaStore album id.
 *
 * Simple story: every album on the phone has a number. Android keeps the album's picture
 * at "content://media/external/audio/albumart/<number>". A missing number, or a number
 * that is 0 or below, means "no album", so we return null.
 * This is plain Kotlin (no Android types), so unit tests can check it on a normal JVM.
 *
 * Example:
 * ```kotlin
 * albumArtUriFor(albumId = 42L) // "content://media/external/audio/albumart/42"
 * albumArtUriFor(albumId = null) // null
 * ```
 *
 * @param albumId MediaStore `ALBUM_ID` of the song, or null when unknown.
 * @return the cover picture address as a String, or null when there is no valid album.
 * @author Phong-Kaster
 */
fun albumArtUriFor(albumId: Long?): String? {
    if (albumId == null) return null
    if (albumId <= 0L) return null
    return "$ALBUM_ART_BASE_URI/$albumId"
}
