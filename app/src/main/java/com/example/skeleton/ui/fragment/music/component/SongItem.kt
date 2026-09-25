package com.example.skeleton.ui.fragment.music.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * One row in the song list: the song title on top, the artist below.
 * When the artist is unknown we show the "Unknown artist" text instead.
 *
 * Example:
 * ```kotlin
 * SongItem(song = song, modifier = Modifier.padding(horizontal = 16.dp))
 * ```
 *
 * @param song The song to show.
 * @param modifier Outer modifier, e.g. horizontal screen padding.
 * @author Phong-Kaster
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song: Song,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            Text(
                text = song.title,
                style = customizedTextStyle(
                    fontSize = 16,
                    fontWeight = 600,
                    lineHeight = 22,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            )

            Text(
                text = song.artist ?: stringResource(R.string.unknown_artist),
                style = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = 400,
                    lineHeight = 20,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            )
        },
    )
}

@Preview(name = "Known artist")
@Composable
private fun SongItemPreview() {
    SongItem(
        song = Song(
            id = 1L,
            title = "Yesterday",
            artist = "The Beatles",
            album = "Help!",
            durationMs = 125_000L,
            contentUri = "content://media/external/audio/media/1",
        ),
    )
}

@Preview(name = "Unknown artist")
@Composable
private fun SongItemUnknownArtistPreview() {
    SongItem(
        song = Song(
            id = 2L,
            title = "voice_memo_final_mix",
            artist = null,
            album = null,
            durationMs = 60_000L,
            contentUri = "content://media/external/audio/media/2",
        ),
    )
}
