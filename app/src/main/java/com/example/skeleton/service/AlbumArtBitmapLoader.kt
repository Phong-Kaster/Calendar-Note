package com.example.skeleton.service

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.OptIn
import androidx.media3.common.util.BitmapLoader
import androidx.media3.common.util.UnstableApi
import com.example.skeleton.data.albumart.decodeSampledBitmap
import com.example.skeleton.data.albumart.findFirstSongUriOfAlbum
import com.example.skeleton.data.albumart.loadAlbumArtBitmap
import com.example.skeleton.data.mapper.albumIdFromAlbumArtUri
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.ListeningExecutorService
import com.google.common.util.concurrent.MoreExecutors
import java.io.IOException
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.RejectedExecutionException

/*
 * --- Why our own bitmap loader (simple story) ---
 * The media notification (and the lock screen) show the song's cover. Media3 fetches that
 * picture from MediaMetadata.artworkUri, which is the old "albumart/<id>" address. On
 * Android 10+ that address is often empty, so the notification showed no cover.
 * This loader gets the address, reads the album id out of it, and uses the same
 * AlbumArtLoader as the Now Playing screen (album thumbnail, the picture inside one song
 * of that album, then the old address). It works on one background thread so the player
 * never waits for a picture.
 */

/**
 * Media3 [BitmapLoader] that finds album covers the way modern Android wants
 * (see [loadAlbumArtBitmap]). A failed load finishes its future with an error, as Media3
 * expects, so the notification simply shows no cover instead of waiting forever.
 *
 * Example:
 * ```kotlin
 * val bitmapLoader = AlbumArtBitmapLoader(context = this)
 * MediaSession.Builder(this, player).setBitmapLoader(bitmapLoader).build()
 * // later, in onDestroy():
 * bitmapLoader.release()
 * ```
 *
 * @param context Any context; only its application context is kept.
 * @author Phong-Kaster
 */
@OptIn(UnstableApi::class)
class AlbumArtBitmapLoader(context: Context) : BitmapLoader {

    private val contentResolver: ContentResolver = context.applicationContext.contentResolver

    /** One background thread that does every picture read, one after another. */
    private val executorService: ListeningExecutorService =
        MoreExecutors.listeningDecorator(Executors.newSingleThreadExecutor())

    /** The last picture we loaded, so the same cover is not read again and again. */
    @Volatile
    private var lastLoaded: LoadedArtwork? = null

    /**
     * Answers: can we decode pictures of this type? Any `image/...` type is fine for BitmapFactory.
     *
     * @author Phong-Kaster
     */
    override fun supportsMimeType(mimeType: String): Boolean {
        return mimeType.startsWith(prefix = "image/")
    }

    /**
     * Turns picture bytes into a shrunk picture. ExoPlayer hands us these bytes when the
     * song file carries its own cover (MediaMetadata.artworkData).
     *
     * @author Phong-Kaster
     */
    override fun decodeBitmap(data: ByteArray): ListenableFuture<Bitmap> {
        return submit(
            task = Callable<Bitmap> {
                decodeSampledBitmap(bytes = data, targetSidePx = ARTWORK_SIDE_PX)
                    ?: throw IOException("Artwork bytes are not a picture")
            },
        )
    }

    /**
     * Finds the picture for [uri] (normally an album cover address from MediaMetadata.artworkUri).
     *
     * @author Phong-Kaster
     */
    override fun loadBitmap(uri: Uri): ListenableFuture<Bitmap> {
        return submit(
            task = Callable<Bitmap> {
                loadArtwork(uri = uri) ?: throw IOException("No album art for $uri")
            },
        )
    }

    /**
     * Stops the background thread. Call it once, when the service is destroyed.
     *
     * @author Phong-Kaster
     */
    fun release() {
        executorService.shutdown()
        lastLoaded = null
    }

    /**
     * Blocking: reads the picture for [uri], reusing the last one when the address is the same.
     *
     * For an album cover address we also look up one song of that album, so the picture
     * inside that song file can be used when the album has no thumbnail.
     * Any other address is simply opened and decoded as it is.
     *
     * @author Phong-Kaster
     */
    private fun loadArtwork(uri: Uri): Bitmap? {
        val cached = lastLoaded
        if (cached != null && cached.uri == uri) return cached.bitmap

        val address = uri.toString()
        val albumId = albumIdFromAlbumArtUri(uri = address)
        val songUri = albumId?.let { id -> findFirstSongUriOfAlbum(contentResolver = contentResolver, albumId = id) }
        val bitmap = loadAlbumArtBitmap(
            contentResolver = contentResolver,
            albumArtUri = address,
            songUri = songUri,
            targetSidePx = ARTWORK_SIDE_PX,
        ) ?: return null
        lastLoaded = LoadedArtwork(uri = uri, bitmap = bitmap)
        return bitmap
    }

    /**
     * Runs [task] on the background thread. After [release] the thread is gone, so we
     * answer with a failed future instead of crashing.
     *
     * @author Phong-Kaster
     */
    private fun submit(task: Callable<Bitmap>): ListenableFuture<Bitmap> {
        return try {
            executorService.submit(task)
        } catch (e: RejectedExecutionException) {
            Futures.immediateFailedFuture(e)
        }
    }

    /**
     * One remembered picture and the address it came from.
     *
     * @author Phong-Kaster
     */
    private class LoadedArtwork(val uri: Uri, val bitmap: Bitmap)

    companion object {
        /** The longest side (in pixels) of a cover in the notification and on the lock screen. */
        private const val ARTWORK_SIDE_PX = 720
    }
}
