package com.example.skeleton.ui.fragment.nowplaying.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R

/**
 * The big square picture of the song on the Now Playing screen.
 *
 * For now it always shows a music-note placeholder; showing the real album cover from
 * [albumArtUri] comes later.
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
    Surface(
        modifier = modifier.aspectRatio(1f),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        content = {
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
        },
    )
}

@Preview(name = "Placeholder")
@Composable
private fun NowPlayingArtworkPreview() {
    NowPlayingArtwork(albumArtUri = null)
}
