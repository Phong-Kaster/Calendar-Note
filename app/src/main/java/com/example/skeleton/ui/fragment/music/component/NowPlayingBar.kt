package com.example.skeleton.ui.fragment.music.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * The small "now playing" bar above the bottom bar: song title and artist on the left,
 * previous / play-pause / next buttons on the right. Every button is at least 48dp so it is
 * easy to hit.
 *
 * Example:
 * ```kotlin
 * NowPlayingBar(song = song, isPlaying = true, onPlayPauseClick = { viewModel.onPlayPauseClick() })
 * ```
 *
 * @param song The song loaded in the player.
 * @param isPlaying True shows the Pause button, false shows the Play button.
 * @param onPreviousClick Called when the user taps previous.
 * @param onPlayPauseClick Called when the user taps play / pause.
 * @param onNextClick Called when the user taps next.
 * @author Phong-Kaster
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NowPlayingBar(
    song: Song,
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPreviousClick: () -> Unit = {},
    onPlayPauseClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {
    val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
    val playPauseLabel = if (isPlaying) R.string.pause else R.string.play

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .background(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                content = {
                    Text(
                        text = song.title,
                        style = customizedTextStyle(
                            fontSize = 15,
                            fontWeight = 600,
                            lineHeight = 20,
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
                },
            )

            IconButton(
                onClick = onPreviousClick,
                modifier = Modifier.size(48.dp),
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_skip_previous),
                        contentDescription = stringResource(R.string.previous),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )

            FilledIconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(48.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                content = {
                    Icon(
                        painter = painterResource(playPauseIcon),
                        contentDescription = stringResource(playPauseLabel),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                },
            )

            IconButton(
                onClick = onNextClick,
                modifier = Modifier.size(48.dp),
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_skip_next),
                        contentDescription = stringResource(R.string.next),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )
        },
    )
}

@Preview(name = "Playing")
@Composable
private fun NowPlayingBarPlayingPreview() {
    NowPlayingBar(
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

@Preview(name = "Paused, unknown artist")
@Composable
private fun NowPlayingBarPausedPreview() {
    NowPlayingBar(
        song = Song(
            id = 2L,
            title = "voice_memo_final_mix",
            artist = null,
            album = null,
            durationMs = 60_000L,
            contentUri = "content://media/external/audio/media/2",
        ),
        isPlaying = false,
    )
}
