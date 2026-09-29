package com.example.skeleton.ui.fragment.library.component

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.core.extension.date_and_time.formatDuration
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.component.SongArtwork
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/** The text MediaStore writes when it does not know the artist. */
private const val MEDIA_STORE_UNKNOWN = "<unknown>"

/**
 * One row in the song list: cover placeholder, title, artist and length (`m:ss`).
 * Long titles scroll slowly (marquee) so they never wrap to two lines.
 *
 * Example:
 * ```kotlin
 * SongItem(song = song, onClick = { onSongClick(index) })
 * ```
 *
 * @param song the song to show.
 * @param modifier extra layout tweaks.
 * @param onClick called when the row is tapped.
 * @author Phong-Kaster
 */
@Composable
fun SongItem(
    song: Song,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    val isArtistUnknown = song.artist.isBlank() || song.artist == MEDIA_STORE_UNKNOWN
    val artistText = if (isArtistUnknown) stringResource(R.string.unknown_artist) else song.artist

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = stringResource(R.string.play),
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SongArtwork(size = 48.dp)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = song.title,
                style = customizedTextStyle(
                    fontSize = 16,
                    fontWeight = 500,
                    lineHeight = 22,
                    color = MaterialTheme.colorScheme.onBackground,
                ),
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            )
            Text(
                text = artistText,
                style = customizedTextStyle(
                    fontSize = 14,
                    fontWeight = 400,
                    lineHeight = 20,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            )
        }

        Text(
            text = formatDuration(durationMs = song.durationMs),
            style = customizedTextStyle(
                fontSize = 13,
                fontWeight = 400,
                lineHeight = 18,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            maxLines = 1,
        )
    }
}

@Preview(name = "Known artist", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun SongItemPreview() {
    MyApplicationTheme(
        content = {
            SongItem(
                song = Song(
                    id = 1L,
                    title = "A very long song title that should scroll instead of wrapping",
                    artist = "The Band",
                    album = "Summer",
                    durationMs = 215_000L,
                    contentUri = "content://media/external/audio/media/1",
                    artworkUri = null,
                ),
            )
        }
    )
}

@Preview(name = "Unknown artist", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun SongItemUnknownArtistPreview() {
    MyApplicationTheme(
        content = {
            SongItem(
                song = Song(
                    id = 2L,
                    title = "Voice memo",
                    artist = "<unknown>",
                    album = "",
                    durationMs = 59_000L,
                    contentUri = "content://media/external/audio/media/2",
                    artworkUri = null,
                ),
            )
        }
    )
}
