package com.example.skeleton.data.mapper

import android.net.Uri
import android.os.Bundle
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.skeleton.domain.model.Song

/** Key used to tuck the song length into [MediaMetadata.extras]. */
private const val EXTRA_DURATION_MS = "com.example.skeleton.extra.DURATION_MS"

/**
 * Turns a [Song] into a Media3 [MediaItem] the player understands.
 *
 * - mediaId = the song id as text ("42"), so we can find the song again later.
 * - uri = the song's `content://` address (also copied into requestMetadata so the
 *   service can rebuild the address if it gets lost on the way).
 * - metadata = title, artist, album (the notification shows these), plus the length in extras.
 * - artworkUri = the album cover address, only when the song has one (the notification shows it).
 *
 * Example:
 * ```kotlin
 * val items = songs.map { song -> song.toMediaItem() }
 * ```
 *
 * @author Phong-Kaster
 */
fun Song.toMediaItem(): MediaItem {
    val uri = Uri.parse(contentUri)
    val extras = Bundle()
    extras.putLong(EXTRA_DURATION_MS, durationMs)

    val metadataBuilder = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .setAlbumTitle(album)
        .setExtras(extras)
    // Only songs with a known album get a cover picture; the others keep the default look.
    albumArtUri?.let { artworkUri -> metadataBuilder.setArtworkUri(Uri.parse(artworkUri)) }

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(uri)
        .setRequestMetadata(
            MediaItem.RequestMetadata.Builder()
                .setMediaUri(uri)
                .build()
        )
        .setMediaMetadata(metadataBuilder.build())
        .build()
}

/**
 * Turns a [MediaItem] made by [toMediaItem] back into a [Song].
 *
 * Example:
 * ```kotlin
 * val song = controller.currentMediaItem?.toSongOrNull()
 * ```
 *
 * @return the [Song], or null when the id is not a number, the title is missing
 * or no address can be found.
 * @author Phong-Kaster
 */
fun MediaItem.toSongOrNull(): Song? {
    val songId = mediaId.toLongOrNull() ?: return null
    val songTitle = mediaMetadata.title?.toString()
    if (songTitle.isNullOrBlank()) return null
    val uri = localConfiguration?.uri ?: requestMetadata.mediaUri ?: return null

    return Song(
        id = songId,
        title = songTitle,
        artist = mediaMetadata.artist?.toString(),
        album = mediaMetadata.albumTitle?.toString(),
        durationMs = mediaMetadata.extras?.getLong(EXTRA_DURATION_MS) ?: 0L,
        contentUri = uri.toString(),
        albumArtUri = mediaMetadata.artworkUri?.toString(),
    )
}

/**
 * Makes sure the player can find the audio file.
 *
 * When a [MediaItem] travels from the app's controller to the service, its playable address
 * (localConfiguration) may be removed. We put the same address in requestMetadata, so here we
 * copy it back. Items that already have an address are returned unchanged.
 *
 * Example:
 * ```kotlin
 * val playable = mediaItems.map { item -> item.withPlayableUri() }
 * ```
 *
 * @author Phong-Kaster
 */
fun MediaItem.withPlayableUri(): MediaItem {
    if (localConfiguration != null) return this
    val uri = requestMetadata.mediaUri ?: return this
    return buildUpon()
        .setUri(uri)
        .build()
}
