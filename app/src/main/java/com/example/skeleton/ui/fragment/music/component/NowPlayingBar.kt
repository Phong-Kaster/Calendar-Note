package com.example.skeleton.ui.fragment.music.component

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * Small bar that shows the song being played and previous / play-pause / next buttons.
 *
 * Example: `NowPlayingBar(playback = uiState.playback, onTogglePlayPause = { viewModel.onTogglePlayPause() })`
 * @param playback What the player is doing (song title / artist, playing or paused).
 * @param modifier Outer modifier.
 * @param onPrevious Previous button tapped.
 * @param onTogglePlayPause Play / pause button tapped.
 * @param onNext Next button tapped.
 * @author Phong-Kaster
 */
@Composable
fun NowPlayingBar(
    playback: PlaybackState,
    modifier: Modifier = Modifier,
    onPrevious: () -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onNext: () -> Unit = {},
) {
    val playPauseIcon = if (playback.isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow
    val playPauseLabel = if (playback.isPlaying) stringResource(R.string.pause) else stringResource(R.string.play)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        content = {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
                content = {
                    Text(
                        text = playback.title ?: stringResource(R.string.unknown_title),
                        style = customizedTextStyle(
                            fontSize = 15,
                            fontWeight = 500,
                            lineHeight = 20,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        maxLines = 1,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                    )
                    Text(
                        text = playback.artist ?: stringResource(R.string.unknown_artist),
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
                onClick = onPrevious,
                content = {
                    Icon(
                        imageVector = Icons.Filled.SkipPrevious,
                        contentDescription = stringResource(R.string.previous_song),
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                },
            )
            IconButton(
                onClick = onTogglePlayPause,
                content = {
                    Icon(
                        imageVector = playPauseIcon,
                        contentDescription = playPauseLabel,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                },
            )
            IconButton(
                onClick = onNext,
                content = {
                    Icon(
                        imageVector = Icons.Filled.SkipNext,
                        contentDescription = stringResource(R.string.next_song),
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
    MyApplicationTheme(
        content = {
            NowPlayingBar(
                playback = PlaybackState(
                    songId = 1,
                    title = "Yesterday",
                    artist = "The Beatles",
                    isPlaying = true,
                    hasQueue = true,
                ),
            )
        }
    )
}

@Preview(name = "Paused, unknown song")
@Composable
private fun NowPlayingBarPausedPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingBar(
                playback = PlaybackState(songId = 2, isPlaying = false, hasQueue = true),
            )
        }
    )
}
