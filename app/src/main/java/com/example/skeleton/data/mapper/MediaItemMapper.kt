package com.example.skeleton.data.mapper

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.RepeatMode
import com.example.skeleton.domain.model.Song

/** Artist text MediaStore uses when it does not know the artist. */
private const val UNKNOWN_ARTIST = "<unknown>"

/**
 * Turns a domain [Song] into a Media3 [MediaItem] the player can play.
 * - mediaId = song id (as text).
 * - The play address is set TWICE: as the normal uri and as `RequestMetadata.mediaUri`.
 *   The normal uri is dropped when the item travels from our controller to the service,
 *   so the service rebuilds it from `mediaUri` (see [withPlayableUri]).
 * - Title / artist / album / cover go into [MediaMetadata], which the notification shows.
 *   A blank or "<unknown>" artist is left empty so the notification does not print "<unknown>".
 *
 * Example:
 * ```kotlin
 * val item = song.toMediaItem()
 * item.mediaId // "1"
 * ```
 *
 * @author Phong-Kaster
 */
@androidx.annotation.OptIn(UnstableApi::class)
fun Song.toMediaItem(): MediaItem {
    val playUri = Uri.parse(contentUri)
    val knownArtist = artist.takeIf { value -> value.isNotBlank() && value != UNKNOWN_ARTIST }
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(knownArtist)
        .setAlbumTitle(album)
        .setArtworkUri(artworkUri?.let { value -> Uri.parse(value) })
        .setDurationMs(durationMs)
        .build()

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(playUri)
        .setRequestMetadata(
            MediaItem.RequestMetadata.Builder()
                .setMediaUri(playUri)
                .build()
        )
        .setMediaMetadata(metadata)
        .build()
}

/**
 * Used by the playback service when items arrive from a controller: puts the play address
 * back (from `RequestMetadata.mediaUri`) so the player knows which file to open.
 * If there is no address at all the item is returned unchanged.
 *
 * Example:
 * ```kotlin
 * val playable = incomingItems.map { item -> item.withPlayableUri() }
 * ```
 *
 * @author Phong-Kaster
 */
fun MediaItem.withPlayableUri(): MediaItem {
    val playUri = requestMetadata.mediaUri ?: localConfiguration?.uri ?: return this
    return buildUpon()
        .setUri(playUri)
        .build()
}

/**
 * Turns a [MediaItem] from the player back into a domain [Song], so the UI can show it.
 *
 * Example:
 * ```kotlin
 * val song = controller.currentMediaItem?.toSong(playerDurationMs = controller.duration)
 * ```
 *
 * @param playerDurationMs the length the player measured; when unknown (<= 0) we fall back
 * to the length stored in the metadata.
 * @author Phong-Kaster
 */
@androidx.annotation.OptIn(UnstableApi::class)
private fun MediaItem.toSong(playerDurationMs: Long): Song {
    val playUri = requestMetadata.mediaUri ?: localConfiguration?.uri
    val durationMs = if (playerDurationMs > 0L) playerDurationMs else mediaMetadata.durationMs ?: 0L
    return Song(
        id = mediaId.toLongOrNull() ?: 0L,
        title = mediaMetadata.title?.toString().orEmpty(),
        artist = mediaMetadata.artist?.toString().orEmpty(),
        album = mediaMetadata.albumTitle?.toString().orEmpty(),
        durationMs = durationMs.coerceAtLeast(0L),
        contentUri = playUri?.toString().orEmpty(),
        artworkUri = mediaMetadata.artworkUri?.toString(),
    )
}

/**
 * Takes a picture of a Media3 [Player] (our controller) as a domain [PlaybackState].
 * Unknown time values (`C.TIME_UNSET`) become 0.
 *
 * Example:
 * ```kotlin
 * _state.value = controller.toPlaybackState()
 * ```
 *
 * @author Phong-Kaster
 */
fun Player.toPlaybackState(): PlaybackState {
    val itemCount = mediaItemCount
    val playerDurationMs = if (duration == C.TIME_UNSET) 0L else duration
    val song = currentMediaItem?.toSong(playerDurationMs = playerDurationMs)
    return PlaybackState(
        currentSong = song,
        isPlaying = isPlaying,
        positionMs = currentPosition.coerceAtLeast(0L),
        durationMs = song?.durationMs ?: 0L,
        currentIndex = if (itemCount == 0) -1 else currentMediaItemIndex,
        queueSize = itemCount,
        shuffleEnabled = shuffleModeEnabled,
        repeatMode = repeatModeFromPlayer(playerRepeatMode = repeatMode),
    )
}

/**
 * Converts our [RepeatMode] into the Media3 number (`Player.REPEAT_MODE_*`).
 *
 * Example:
 * ```kotlin
 * controller.repeatMode = RepeatMode.All.toPlayerRepeatMode() // Player.REPEAT_MODE_ALL
 * ```
 *
 * @author Phong-Kaster
 */
fun RepeatMode.toPlayerRepeatMode(): Int {
    return when (this) {
        RepeatMode.Off -> Player.REPEAT_MODE_OFF
        RepeatMode.All -> Player.REPEAT_MODE_ALL
        RepeatMode.One -> Player.REPEAT_MODE_ONE
    }
}

/**
 * Converts a Media3 repeat number (`Player.REPEAT_MODE_*`) into our [RepeatMode].
 * Any unknown number is treated as [RepeatMode.Off].
 *
 * Example:
 * ```kotlin
 * repeatModeFromPlayer(playerRepeatMode = Player.REPEAT_MODE_ONE) // RepeatMode.One
 * ```
 *
 * @author Phong-Kaster
 */
private fun repeatModeFromPlayer(playerRepeatMode: Int): RepeatMode {
    return when (playerRepeatMode) {
        Player.REPEAT_MODE_ALL -> RepeatMode.All
        Player.REPEAT_MODE_ONE -> RepeatMode.One
        else -> RepeatMode.Off
    }
}
