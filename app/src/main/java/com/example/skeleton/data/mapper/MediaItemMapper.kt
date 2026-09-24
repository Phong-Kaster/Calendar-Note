package com.example.skeleton.data.mapper

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.Song

/**
 * Turns a [Song] into a Media3 [MediaItem] the player can open.
 * - mediaId = the song id as text (so we can read it back later)
 * - uri = the song's content uri
 * - metadata = title and artist (shown in the notification / lock screen)
 *
 * Example: `song.toMediaItem()` → MediaItem(mediaId = "42", uri = "content://media/external/audio/media/42")
 * @author Phong-Kaster
 */
fun Song.toMediaItem(): MediaItem {
    val metadata = MediaMetadata.Builder()
        .setTitle(title)
        .setArtist(artist)
        .build()

    return MediaItem.Builder()
        .setMediaId(id.toString())
        .setUri(contentUri)
        .setMediaMetadata(metadata)
        .build()
}

/**
 * Reads the song id, title and artist back from a [MediaItem] built by [toMediaItem] and
 * packs them with the play flags into a [PlaybackState].
 * A blank title or artist becomes null, so the UI shows its fallback text.
 *
 * Example: `mediaItem.toPlaybackState(isPlaying = true, hasQueue = true)`
 * @param isPlaying True while the player plays.
 * @param hasQueue True when the player holds at least one song.
 * @author Phong-Kaster
 */
fun MediaItem.toPlaybackState(isPlaying: Boolean, hasQueue: Boolean): PlaybackState {
    return PlaybackState(
        songId = mediaId.toLongOrNull(),
        title = mediaMetadata.title?.toString()?.takeIf { text -> text.isNotBlank() },
        artist = mediaMetadata.artist?.toString()?.takeIf { text -> text.isNotBlank() },
        isPlaying = isPlaying,
        hasQueue = hasQueue,
    )
}
