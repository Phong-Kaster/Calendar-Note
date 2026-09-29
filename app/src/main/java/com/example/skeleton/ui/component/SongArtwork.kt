package com.example.skeleton.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.MyApplicationTheme

/**
 * A square "album cover" placeholder: a rounded tile with a music-note icon in the middle.
 * Shared by the library list, the mini-player and the Now Playing screen, so they all look alike.
 * Colours come from the theme, so the note is always visible on the tile.
 *
 * Example:
 * ```kotlin
 * SongArtwork(size = 48.dp)
 * SongArtwork(size = 280.dp, cornerRadius = 24.dp)
 * ```
 *
 * @param modifier extra layout tweaks from the caller.
 * @param size width and height of the tile.
 * @param cornerRadius how round the corners are.
 * @author Phong-Kaster
 */
@Composable
fun SongArtwork(
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    cornerRadius: Dp = 8.dp,
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(color = MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_music_note),
            // Decorative only: the song title next to it already tells the story.
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier
                .fillMaxSize()
                .padding(size / 4),
        )
    }
}

@Preview(name = "Small")
@Composable
private fun SongArtworkSmallPreview() {
    MyApplicationTheme(
        content = {
            SongArtwork(size = 48.dp)
        }
    )
}

@Preview(name = "Large")
@Composable
private fun SongArtworkLargePreview() {
    MyApplicationTheme(
        content = {
            SongArtwork(size = 240.dp, cornerRadius = 24.dp)
        }
    )
}
