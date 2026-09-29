package com.example.skeleton.ui.fragment.nowplaying.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.RepeatMode
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.fragment.nowplaying.NowPlayingUiState
import com.example.skeleton.ui.theme.MyApplicationTheme

/**
 * The row of player buttons: shuffle, previous, big play/pause, next, repeat.
 * Shuffle and repeat get a filled circle in the theme's container colour while they are on,
 * so "on" and "off" look clearly different. Every button has a screen-reader description;
 * the repeat one says the CURRENT mode (off / all / one).
 *
 * Example:
 * ```kotlin
 * NowPlayingControls(uiState = uiState, onTogglePlayPause = { viewModel.togglePlayPause() })
 * ```
 *
 * @param uiState which icons to show and which buttons are active.
 * @param modifier extra layout tweaks.
 * @param onToggleShuffle shuffle tapped.
 * @param onSkipToPrevious previous tapped.
 * @param onTogglePlayPause play / pause tapped.
 * @param onSkipToNext next tapped.
 * @param onCycleRepeat repeat tapped (goes Off -> All -> One -> Off).
 * @author Phong-Kaster
 */
@Composable
fun NowPlayingControls(
    uiState: NowPlayingUiState,
    modifier: Modifier = Modifier,
    onToggleShuffle: () -> Unit = {},
    onSkipToPrevious: () -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onSkipToNext: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
) {
    val playPauseIcon = if (uiState.isPlaying) R.drawable.ic_player_pause else R.drawable.ic_player_play
    val playPauseDescription = if (uiState.isPlaying) stringResource(R.string.pause) else stringResource(R.string.play)
    val repeatIcon = if (uiState.repeatMode == RepeatMode.One) R.drawable.ic_player_repeat_one else R.drawable.ic_player_repeat
    val repeatDescription = when (uiState.repeatMode) {
        RepeatMode.Off -> stringResource(R.string.repeat_off)
        RepeatMode.All -> stringResource(R.string.repeat_all)
        RepeatMode.One -> stringResource(R.string.repeat_one)
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconToggleButton(
            checked = uiState.shuffleEnabled,
            onCheckedChange = { _ ->
                onToggleShuffle()
            },
            enabled = uiState.hasSong,
            colors = modeButtonColors(),
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_player_shuffle),
                contentDescription = stringResource(R.string.shuffle),
                modifier = Modifier.size(24.dp),
            )
        }

        IconButton(
            onClick = {
                onSkipToPrevious()
            },
            enabled = uiState.canSkipToPrevious,
            colors = transportButtonColors(),
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_player_previous),
                contentDescription = stringResource(R.string.previous),
                modifier = Modifier.size(32.dp),
            )
        }

        FilledIconButton(
            onClick = {
                onTogglePlayPause()
            },
            enabled = uiState.hasSong,
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            modifier = Modifier.size(72.dp),
        ) {
            Icon(
                painter = painterResource(id = playPauseIcon),
                contentDescription = playPauseDescription,
                modifier = Modifier.size(36.dp),
            )
        }

        IconButton(
            onClick = {
                onSkipToNext()
            },
            enabled = uiState.canSkipToNext,
            colors = transportButtonColors(),
            modifier = Modifier.size(56.dp),
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_player_next),
                contentDescription = stringResource(R.string.next),
                modifier = Modifier.size(32.dp),
            )
        }

        IconToggleButton(
            checked = uiState.isRepeatActive,
            onCheckedChange = { _ ->
                onCycleRepeat()
            },
            enabled = uiState.hasSong,
            colors = modeButtonColors(),
            modifier = Modifier.size(48.dp),
        ) {
            Icon(
                painter = painterResource(id = repeatIcon),
                contentDescription = repeatDescription,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

/**
 * Colours of the shuffle / repeat buttons: muted when off, filled theme-container circle when on.
 *
 * @author Phong-Kaster
 */
@Composable
private fun modeButtonColors() = IconButtonDefaults.iconToggleButtonColors(
    containerColor = Color.Transparent,
    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    disabledContainerColor = Color.Transparent,
    disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.38f),
    checkedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    checkedContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
)

/**
 * Colours of the previous / next buttons: bright on the dark ground, faded when disabled.
 *
 * @author Phong-Kaster
 */
@Composable
private fun transportButtonColors() = IconButtonDefaults.iconButtonColors(
    containerColor = Color.Transparent,
    contentColor = MaterialTheme.colorScheme.onBackground,
    disabledContainerColor = Color.Transparent,
    disabledContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.38f),
)

/** Fake song for previews only. */
private val previewSong = Song(
    id = 1L,
    title = "Blue Sky",
    artist = "The Band",
    album = "Summer",
    durationMs = 215_000L,
    contentUri = "content://media/external/audio/media/1",
    artworkUri = null,
)

@Preview(name = "Playing", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun NowPlayingControlsPlayingPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingControls(
                uiState = NowPlayingUiState(
                    playback = PlaybackState(currentSong = previewSong, isPlaying = true, currentIndex = 0, queueSize = 3),
                ),
            )
        }
    )
}

@Preview(name = "Paused", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun NowPlayingControlsPausedPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingControls(
                uiState = NowPlayingUiState(
                    playback = PlaybackState(currentSong = previewSong, isPlaying = false, currentIndex = 2, queueSize = 3),
                ),
            )
        }
    )
}

@Preview(name = "Shuffle + repeat one", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun NowPlayingControlsModesPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingControls(
                uiState = NowPlayingUiState(
                    playback = PlaybackState(
                        currentSong = previewSong,
                        isPlaying = true,
                        currentIndex = 1,
                        queueSize = 3,
                        shuffleEnabled = true,
                        repeatMode = RepeatMode.One,
                    ),
                ),
            )
        }
    )
}
