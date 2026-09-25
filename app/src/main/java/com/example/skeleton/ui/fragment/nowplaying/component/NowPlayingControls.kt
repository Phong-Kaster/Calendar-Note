package com.example.skeleton.ui.fragment.nowplaying.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R

/**
 * The big player buttons: previous, a large round play / pause in the middle, and next.
 *
 * Example:
 * ```kotlin
 * NowPlayingControls(isPlaying = true, onPlayPauseClick = { viewModel.onPlayPauseClick() })
 * ```
 *
 * @param isPlaying True shows the Pause button, false shows the Play button.
 * @param modifier Size / position from the caller.
 * @param onPreviousClick Called when the user taps previous.
 * @param onPlayPauseClick Called when the user taps play / pause.
 * @param onNextClick Called when the user taps next.
 * @author Phong-Kaster
 */
@Composable
fun NowPlayingControls(
    isPlaying: Boolean,
    modifier: Modifier = Modifier,
    onPreviousClick: () -> Unit = {},
    onPlayPauseClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
) {
    val playPauseIcon = if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
    val playPauseLabel = if (isPlaying) R.string.pause else R.string.play

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 24.dp, alignment = Alignment.CenterHorizontally),
        content = {
            IconButton(
                onClick = onPreviousClick,
                modifier = Modifier.size(64.dp),
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_skip_previous),
                        contentDescription = stringResource(R.string.previous),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp),
                    )
                },
            )

            FilledIconButton(
                onClick = onPlayPauseClick,
                modifier = Modifier.size(80.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                content = {
                    Icon(
                        painter = painterResource(playPauseIcon),
                        contentDescription = stringResource(playPauseLabel),
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(40.dp),
                    )
                },
            )

            IconButton(
                onClick = onNextClick,
                modifier = Modifier.size(64.dp),
                content = {
                    Icon(
                        painter = painterResource(R.drawable.ic_skip_next),
                        contentDescription = stringResource(R.string.next),
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp),
                    )
                },
            )
        },
    )
}

@Preview(name = "Playing")
@Composable
private fun NowPlayingControlsPlayingPreview() {
    NowPlayingControls(isPlaying = true)
}

@Preview(name = "Paused")
@Composable
private fun NowPlayingControlsPausedPreview() {
    NowPlayingControls(isPlaying = false)
}
