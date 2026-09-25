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

/**
 * The opposite of [albumArtUriFor]: reads the album number back out of a cover picture address.
 *
 * Simple story: "content://media/external/audio/albumart/42" ends with the album number 42,
 * so we give back 42. Anything else (no address, a different folder, a word instead of a
 * number, or a number that is 0 or below) gives null, meaning "this is not an album cover address".
 * This is plain Kotlin (no Android types), so unit tests can check it on a normal JVM.
 *
 * Example:
 * ```kotlin
 * albumIdFromAlbumArtUri(uri = "content://media/external/audio/albumart/42") // 42
 * albumIdFromAlbumArtUri(uri = "content://media/external/audio/media/42") // null
 * albumIdFromAlbumArtUri(uri = albumArtUriFor(albumId = 7L)) // 7
 * ```
 *
 * @param uri A cover picture address as a String, or null.
 * @return the album id, or null when [uri] is not a valid album cover address.
 * @author Phong-Kaster
 */
fun albumIdFromAlbumArtUri(uri: String?): Long? {
    if (uri == null) return null
    val prefix = "$ALBUM_ART_BASE_URI/"
    if (!uri.startsWith(prefix)) return null
    val albumId = uri.removePrefix(prefix).toLongOrNull() ?: return null
    if (albumId <= 0L) return null
    return albumId
}
