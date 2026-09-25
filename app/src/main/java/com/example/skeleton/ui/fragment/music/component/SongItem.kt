package com.example.skeleton.ui.fragment.music.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * One row in the song list: the song title on top, the artist below.
 * When the artist is unknown we show the "Unknown artist" text instead.
 * The row of the song loaded in the player gets a coloured background so the user sees it.
 *
 * Example:
 * ```kotlin
 * SongItem(song = song, isPlaying = true, onClick = { viewModel.onSongClick(song) })
 * ```
 *
 * @param song The song to show.
 * @param modifier Outer modifier, e.g. horizontal screen padding.
 * @param isPlaying True when this is the song loaded in the player (the row is highlighted).
 * @param onClick Called when the user taps the row.
 * @author Phong-Kaster
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongItem(
    song: Song,
    modifier: Modifier = Modifier,
    isPlaying: Boolean = false,
    onClick: () -> Unit = {},
) {
    val backgroundColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    val titleColor = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
    val artistColor = if (isPlaying) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(
                onClickLabel = stringResource(R.string.play),
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
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
                    color = titleColor,
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
                    color = artistColor,
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

@Preview(name = "Playing")
@Composable
private fun SongItemPlayingPreview() {
    SongItem(
        song = Song(
            id = 1L,
            title = "Yesterday",
            artist = "The Beatles",
            album = "Help!",
            durationMs = 125_000L,
            contentUri = "content://media/external/audio/media/1",
        ),
        isPlaying = true,
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
