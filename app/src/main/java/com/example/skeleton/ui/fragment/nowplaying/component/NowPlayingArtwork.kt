package com.example.skeleton.ui.fragment.nowplaying.component

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The biggest side (in pixels) we ever decode a cover to; bigger pictures are shrunk. */
private const val MAX_ARTWORK_SIDE_PX = 1024

private const val TAG = "NowPlayingArtwork"

/**
 * The big square picture of the song on the Now Playing screen.
 *
 * It reads the real album cover from [albumArtUri] in the background and shows it cropped
 * to a square with rounded corners. While the cover is loading, when the song has no cover,
 * or when reading it fails, a music-note placeholder is shown instead. When the song
 * changes (a new [albumArtUri]) the picture is loaded again.
 *
 * Example:
 * ```kotlin
 * NowPlayingArtwork(albumArtUri = song.albumArtUri)
 * ```
 *
 * @param albumArtUri The `content://` address of the album cover; null when there is none.
 * @param modifier Size / position from the caller; the artwork is always square and as big
 * as the space allows.
 * @author Phong-Kaster
 */
@Composable
fun NowPlayingArtwork(
    albumArtUri: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val displayMetrics = context.resources.displayMetrics
    // The square artwork is never wider than the screen's shorter side.
    val targetSidePx = minOf(displayMetrics.widthPixels, displayMetrics.heightPixels)
        .coerceAtMost(MAX_ARTWORK_SIDE_PX)
    val artwork by produceState<ImageBitmap?>(
        initialValue = null,
        key1 = albumArtUri,
        producer = {
            // Show the placeholder while the new song's cover is loading.
            value = null
            if (albumArtUri == null) return@produceState
            value = withContext(Dispatchers.IO) {
                decodeAlbumArt(
                    contentResolver = context.contentResolver,
                    albumArtUri = albumArtUri,
                    targetSidePx = targetSidePx,
                )
            }?.asImageBitmap()
        },
    )

    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        content = {
            if (artwork == null) {
                ArtworkPlaceholder()
            } else {
                Image(
                    bitmap = artwork!!,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        },
    )
}

/**
 * The music-note picture shown when there is no real cover (yet).
 *
 * @author Phong-Kaster
 */
@Composable
private fun ArtworkPlaceholder() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
        content = {
            Icon(
                painter = painterResource(R.drawable.ic_bottom_music),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(96.dp),
            )
        },
    )
}

/**
 * Reads the album cover at [albumArtUri] and shrinks it so its longer side is close to
 * [targetSidePx]. This is blocking work: call it on `Dispatchers.IO`.
 *
 * Step 1 only peeks at the picture's width and height (no pixels loaded).
 * Step 2 opens the picture again and loads a shrunk copy.
 *
 * Example:
 * ```kotlin
 * val bitmap = decodeAlbumArt(contentResolver, "content://media/external/audio/albumart/7", 1024)
 * ```
 *
 * @param contentResolver Opens `content://` addresses.
 * @param albumArtUri The `content://` address of the cover.
 * @param targetSidePx The longest side (in pixels) we want the result to be about.
 * @return The picture, or null when it is missing or cannot be read. Never throws.
 * @author Phong-Kaster
 */
private fun decodeAlbumArt(
    contentResolver: ContentResolver,
    albumArtUri: String,
    targetSidePx: Int,
): Bitmap? {
    return try {
        val uri = Uri.parse(albumArtUri)
        val boundsOptions = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        contentResolver.openInputStream(uri)?.use { stream ->
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
        contentResolver.openInputStream(uri)?.use { stream ->
            BitmapFactory.decodeStream(stream, null, decodeOptions)
        }
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        Log.w(TAG, "No album art for $albumArtUri: ${e.message}")
        null
    } catch (e: OutOfMemoryError) {
        Log.e(TAG, "Album art too big: $albumArtUri", e)
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
private fun calculateInSampleSize(width: Int, height: Int, targetSidePx: Int): Int {
    val longerSide = maxOf(width, height)
    // A target of 0 would make the loop below run forever, so we never go under 1.
    val safeTargetSidePx = targetSidePx.coerceAtLeast(1)
    var inSampleSize = 1
    while (longerSide / (inSampleSize * 2) >= safeTargetSidePx) {
        inSampleSize *= 2
    }
    return inSampleSize
}

@Preview(name = "Placeholder")
@Composable
private fun NowPlayingArtworkPreview() {
    NowPlayingArtwork(albumArtUri = null)
}
