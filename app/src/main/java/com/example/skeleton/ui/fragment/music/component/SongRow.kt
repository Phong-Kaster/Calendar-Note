package com.example.skeleton.ui.fragment.music.component

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
import com.example.skeleton.data.mapper.formatDuration
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * One song in the Music list: title and artist on the left, duration ("m:ss") on the right.
 * A long title stays on one line with "…" and never pushes the duration off the row.
 *
 * Example: `SongRow(song = song, onClick = { viewModel.play(song) })`
 * @param song The song to show.
 * @param modifier Outer modifier.
 * @param onClick Called when the row is tapped.
 * @author Phong-Kaster
 */
@Composable
fun SongRow(
    song: Song,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = song.title.ifBlank { stringResource(R.string.unknown_title) },
                style = customizedTextStyle(
                    fontSize = 16,
                    fontWeight = 500,
                    lineHeight = 22,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                maxLines = 1,
                modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
            )
            Text(
                text = song.artist ?: stringResource(R.string.unknown_artist),
                style = customizedTextStyle(
                    fontSize = 13,
                    fontWeight = 400,
                    lineHeight = 18,
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

@Preview(name = "Normal")
@Composable
private fun SongRowPreview() {
    MyApplicationTheme(
        content = {
            SongRow(
                song = Song(
                    id = 1,
                    title = "Yesterday",
                    artist = "The Beatles",
                    durationMs = 125_000,
                    contentUri = "content://media/external/audio/media/1",
                ),
            )
        }
    )
}

@Preview(name = "Long title, unknown artist")
@Composable
private fun SongRowLongTitlePreview() {
    MyApplicationTheme(
        content = {
            SongRow(
                song = Song(
                    id = 2,
                    title = "A very very long song title that definitely does not fit on one single line",
                    artist = null,
                    durationMs = 3_605_000,
                    contentUri = "content://media/external/audio/media/2",
                ),
            )
        }
    )
}
