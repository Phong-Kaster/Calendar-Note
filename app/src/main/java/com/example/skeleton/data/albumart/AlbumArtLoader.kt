package com.example.skeleton.data.albumart

import android.content.ContentResolver
import android.content.ContentUris
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.util.Size
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import com.example.skeleton.data.mapper.albumIdFromAlbumArtUri
import kotlinx.coroutines.CancellationException

private const val TAG = "AlbumArtLoader"

/*
 * --- Why four ways to find a cover (simple story) ---
 * Old phones (Android 7 to 9) keep every album cover at "content://media/external/audio/albumart/<id>".
 * From Android 10 that address is often empty: the phone wants apps to ask for a small
 * "thumbnail" instead (ContentResolver.loadThumbnail). Some songs also carry their own picture
 * inside the audio file ("embedded picture").
 * So we try, in order, and stop at the first picture we get:
 *   a. Android 10+: the album's thumbnail.
 *   b. Android 10+: the song file's thumbnail.
 *   c. Any Android: the picture embedded inside the song file.
 *   d. Any Android: the old album cover address (the only way on Android 7 to 9).
 * Every step may fail; a failure just moves on to the next step. Nothing here ever throws.
 */

/**
 * Finds the cover picture of a song and returns it shrunk to about [targetSidePx] pixels.
 * This is blocking work: call it on a background thread (`Dispatchers.IO` or an executor).
 *
 * It tries the four ways described above and writes one warning to the log when all fail.
 *
 * Example:
 * ```kotlin
 * val bitmap = loadAlbumArtBitmap(
 *     contentResolver = context.contentResolver,
 *     albumArtUri = "content://media/external/audio/albumart/7",
 *     songUri = "content://media/external/audio/media/42",
 *     targetSidePx = 1024,
 * )
 * ```
 *
 * @param contentResolver Opens `content://` addresses.
 * @param albumArtUri The old album cover address (it also tells us the album id); null when unknown.
 * @param songUri The `content://` address of the audio file; null when unknown.
 * @param targetSidePx The longest side (in pixels) we want the picture to be about.
 * @return The picture, or null when none of the four ways found one. Never throws.
 * @author Phong-Kaster
 */
fun loadAlbumArtBitmap(
    contentResolver: ContentResolver,
    albumArtUri: String?,
    songUri: String?,
    targetSidePx: Int,
): Bitmap? {
    if (albumArtUri == null && songUri == null) return null
    val safeSidePx = targetSidePx.coerceAtLeast(1)
    val albumId = albumIdFromAlbumArtUri(uri = albumArtUri)
    // Short notes about what went wrong, written to the log once at the end.
    val failures = mutableListOf<String>()

    val bitmap = tryStep(
        stepName = "album thumbnail",
        failures = failures,
        load = {
            if (albumId != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                loadThumbnail(
                    contentResolver = contentResolver,
                    uri = ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, albumId),
                    targetSidePx = safeSidePx,
                )
            } else {
                null
            }
        },
    ) ?: tryStep(
        stepName = "song thumbnail",
        failures = failures,
        load = {
            if (songUri != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                loadThumbnail(
                    contentResolver = contentResolver,
                    uri = songUri.toUri(),
                    targetSidePx = safeSidePx,
                )
            } else {
                null
            }
        },
    ) ?: tryStep(
        stepName = "embedded picture",
        failures = failures,
        load = {
            if (songUri == null) {
                null
            } else {
                loadEmbeddedPicture(
                    contentResolver = contentResolver,
                    songUri = songUri.toUri(),
                    targetSidePx = safeSidePx,
                )
            }
        },
    ) ?: tryStep(
        stepName = "legacy album art",
        failures = failures,
        load = {
            if (albumArtUri == null) {
                null
            } else {
                decodeLegacyAlbumArt(
                    contentResolver = contentResolver,
                    albumArtUri = albumArtUri.toUri(),
                    targetSidePx = safeSidePx,
                )
            }
        },
    )

    if (bitmap == null) {
        Log.w(TAG, "No album art for song=$songUri album=$albumArtUri ${failures.joinToString(separator = "; ")}")
    }
    return bitmap
}

/**
 * Asks MediaStore for one song of the album [albumId] and returns that song's `content://` address.
 * We need a song to read its embedded picture when we only know the album (the notification case).
 * Blocking work: call it on a background thread.
 *
 * Example:
 * ```kotlin
 * val songUri = findFirstSongUriOfAlbum(contentResolver = resolver, albumId = 7L)
 * ```
 *
 * @param contentResolver Runs the MediaStore query.
 * @param albumId MediaStore `ALBUM_ID` of the album.
 * @return The address of one song in that album, or null when none is found. Never throws.
 * @author Phong-Kaster
 */
fun findFirstSongUriOfAlbum(contentResolver: ContentResolver, albumId: Long): String? {
    return try {
        val cursor = contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            arrayOf(MediaStore.Audio.Media._ID),
            "${MediaStore.Audio.Media.ALBUM_ID} = ?",
            arrayOf(albumId.toString()),
            null,
        ) ?: return null
        cursor.use { openedCursor ->
            if (!openedCursor.moveToFirst()) return null
            val songId = openedCursor.getLong(openedCursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
            ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, songId).toString()
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "No song found for album $albumId: ${e.message}")
        null
    }
}

/**
 * Turns picture bytes (for example a cover packed inside the audio file) into a shrunk picture.
 *
 * Step 1 only peeks at the width and height (no pixels loaded).
 * Step 2 loads a copy shrunk with [calculateInSampleSize].
 *
 * Example:
 * ```kotlin
 * val bitmap = decodeSampledBitmap(bytes = retriever.embeddedPicture!!, targetSidePx = 720)
 * ```
 *
 * @param bytes The encoded picture (JPEG, PNG, ...).
 * @param targetSidePx The longest side (in pixels) we want the picture to be about.
 * @return The picture, or null when the bytes are not a picture. Never throws.
 * @author Phong-Kaster
 */
fun decodeSampledBitmap(bytes: ByteArray, targetSidePx: Int): Bitmap? {
    return try {
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, boundsOptions)
        if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

        val decodeOptions = BitmapFactory.Options().apply {
            inSampleSize = calculateInSampleSize(
                width = boundsOptions.outWidth,
                height = boundsOptions.outHeight,
                targetSidePx = targetSidePx,
            )
        }
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, decodeOptions)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "Picture bytes could not be decoded: ${e.message}")
        null
    } catch (e: OutOfMemoryError) {
        Log.w(TAG, "Picture bytes too big to decode", e)
        null
    }
}

/**
 * Picks how much to shrink a picture: 1 = full size, 2 = half, 4 = a quarter, ...
 * We keep halving while the longer side is still at least twice [targetSidePx].
 *
 * Example: a 3000 x 3000 cover with target 1024 gives 2 (1500 x 1500).
 *
 * @param width Picture width in pixels.
 * @param height Picture height in pixels.
 * @param targetSidePx The longest side (in pixels) we want the result to be about.
 * @return A power of two, at least 1.
 * @author Phong-Kaster
 */
fun calculateInSampleSize(width: Int, height: Int, targetSidePx: Int): Int {
    val longerSide = maxOf(width, height)
    // A target of 0 would make the loop below run forever, so we never go under 1.
    val safeTargetSidePx = targetSidePx.coerceAtLeast(1)
    var inSampleSize = 1
    while (longerSide / (inSampleSize * 2) >= safeTargetSidePx) {
        inSampleSize *= 2
    }
    return inSampleSize
}

/**
 * Runs one way of finding a cover. If it throws, we write a short note into [failures]
 * and give back null, so the caller simply moves on to the next way.
 *
 * @param stepName A short name of the way, used in the log note.
 * @param failures Where notes about failures are collected.
 * @param load The way itself; returns the picture or null.
 * @return The picture, or null when the way found nothing or failed.
 * @author Phong-Kaster
 */
private fun tryStep(
    stepName: String,
    failures: MutableList<String>,
    load: () -> Bitmap?,
): Bitmap? {
    return try {
        load()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        failures.add("$stepName: ${e.javaClass.simpleName} ${e.message}")
        null
    } catch (e: OutOfMemoryError) {
        failures.add("$stepName: out of memory")
        null
    }
}

/**
 * Android 10+ only: asks the phone for a small thumbnail of [uri] (an album or a song).
 * Throws when there is no thumbnail; [tryStep] catches that.
 *
 * @author Phong-Kaster
 */
@RequiresApi(Build.VERSION_CODES.Q)
private fun loadThumbnail(contentResolver: ContentResolver, uri: Uri, targetSidePx: Int): Bitmap {
    return contentResolver.loadThumbnail(uri, Size(targetSidePx, targetSidePx), null)
}

/**
 * Reads the picture packed inside the audio file at [songUri], shrunk to about [targetSidePx].
 * The retriever is always released, even when reading fails.
 *
 * @return The picture, or null when the file has no picture inside.
 * @author Phong-Kaster
 */
private fun loadEmbeddedPicture(contentResolver: ContentResolver, songUri: Uri, targetSidePx: Int): Bitmap? {
    val retriever = MediaMetadataRetriever()
    try {
        val pictureBytes = contentResolver.openFileDescriptor(songUri, "r")?.use { fileDescriptor ->
            retriever.setDataSource(fileDescriptor.fileDescriptor)
            retriever.embeddedPicture
        } ?: return null
        return decodeSampledBitmap(bytes = pictureBytes, targetSidePx = targetSidePx)
    } finally {
        releaseQuietly(retriever = retriever)
    }
}

/**
 * Frees the retriever. On new Android versions `release()` may itself throw; we ignore that
 * because we are only cleaning up.
 *
 * @author Phong-Kaster
 */
private fun releaseQuietly(retriever: MediaMetadataRetriever) {
    try {
        retriever.release()
    } catch (e: Exception) {
        Log.w(TAG, "MediaMetadataRetriever.release() failed: ${e.message}")
    }
}

/**
 * Reads the old-style album cover address (the only way on Android 7 to 9), shrunk to about
 * [targetSidePx]. Step 1 peeks at the size, step 2 loads a shrunk copy.
 *
 * @return The picture, or null when the address is empty.
 * @author Phong-Kaster
 */
private fun decodeLegacyAlbumArt(contentResolver: ContentResolver, albumArtUri: Uri, targetSidePx: Int): Bitmap? {
    val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contentResolver.openInputStream(albumArtUri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, boundsOptions)
    }
    if (boundsOptions.outWidth <= 0 || boundsOptions.outHeight <= 0) return null

    val decodeOptions = BitmapFactory.Options().apply {
        inSampleSize = calculateInSampleSize(
            width = boundsOptions.outWidth,
            height = boundsOptions.outHeight,
            targetSidePx = targetSidePx,
        )
    }
    return contentResolver.openInputStream(albumArtUri)?.use { stream ->
        BitmapFactory.decodeStream(stream, null, decodeOptions)
    }
}
