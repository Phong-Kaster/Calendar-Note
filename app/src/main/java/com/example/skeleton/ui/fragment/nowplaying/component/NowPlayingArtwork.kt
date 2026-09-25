package com.example.skeleton.ui.fragment.nowplaying.component

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
import com.example.skeleton.data.albumart.loadAlbumArtBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** The biggest side (in pixels) we ever decode a cover to; bigger pictures are shrunk. */
private const val MAX_ARTWORK_SIDE_PX = 1024

/**
 * The big square picture of the song on the Now Playing screen.
 *
 * It finds the real album cover in the background (see [loadAlbumArtBitmap]: the album's
 * thumbnail, the song's thumbnail, the picture inside the song file, then the old cover
 * address) and shows it cropped to a square with rounded corners. While the cover is loading,
 * when the song has no cover, or when reading it fails, a music-note placeholder is shown
 * instead. When the song changes the picture is loaded again.
 *
 * Example:
 * ```kotlin
 * NowPlayingArtwork(albumArtUri = song.albumArtUri, contentUri = song.contentUri)
 * ```
 *
 * @param albumArtUri The `content://` address of the album cover; null when there is none.
 * @param contentUri The `content://` address of the song's audio file; null when unknown.
 * @param modifier Size / position from the caller; the artwork is always square and as big
 * as the space allows.
 * @author Phong-Kaster
 */
@Composable
fun NowPlayingArtwork(
    albumArtUri: String?,
    contentUri: String?,
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
        key2 = contentUri,
        producer = {
            // Show the placeholder while the new song's cover is loading.
            value = null
            if (albumArtUri == null && contentUri == null) return@produceState
            value = withContext(Dispatchers.IO) {
                loadAlbumArtBitmap(
                    contentResolver = context.contentResolver,
                    albumArtUri = albumArtUri,
                    songUri = contentUri,
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

@Preview(name = "Placeholder")
@Composable
private fun NowPlayingArtworkPreview() {
    NowPlayingArtwork(albumArtUri = null, contentUri = null)
}
