package com.example.skeleton.ui.fragment.nowplaying.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
 * Big song title with the artist under it. Long names scroll sideways instead of being cut.
 *
 * Example:
 * ```kotlin
 * NowPlayingSongInfo(song = song)
 * ```
 *
 * @param song The song loaded in the player.
 * @param modifier Size / position from the caller.
 * @author Phong-Kaster
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingSongInfo(
    song: Song,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            Text(
                text = song.title,
                style = customizedTextStyle(
                    fontSize = 22,
                    fontWeight = 700,
                    lineHeight = 28,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            )

            Text(
                text = song.artist ?: stringResource(R.string.unknown_artist),
                style = customizedTextStyle(
                    fontSize = 16,
                    fontWeight = 400,
                    lineHeight = 22,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            )
        },
    )
}

@Preview
@Composable
private fun NowPlayingSongInfoPreview() {
    NowPlayingSongInfo(
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
