package com.example.skeleton.ui.fragment.nowplaying.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.fragment.nowplaying.NowPlayingUiState
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/*
 * --- Seek bar (simple story) ---
 * The player tells us its position about twice a second. If the knob followed that while the
 * user drags, it would jump back under the finger. So while dragging we remember our OWN knob
 * spot (dragFraction) and show it, together with its time text. When the finger lets go we
 * send ONE seek to the player and forget our spot, so the knob follows the player again.
 */

/**
 * Seek bar with the elapsed time on the left and the song length on the right.
 *
 * Example:
 * ```kotlin
 * NowPlayingSeekBar(uiState = uiState, onSeek = { fraction -> viewModel.seekTo(fraction = fraction) })
 * ```
 *
 * @param uiState where the song is now and how long it is.
 * @param modifier extra layout tweaks.
 * @param onSeek called once, when the user lets go, with the knob spot from 0f to 1f.
 * @author Phong-Kaster
 */
@Composable
fun NowPlayingSeekBar(
    uiState: NowPlayingUiState,
    modifier: Modifier = Modifier,
    onSeek: (Float) -> Unit = {},
) {
    /** dragFraction is the knob spot under the finger; null when the user is not dragging. */
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val shownFraction = dragFraction ?: uiState.seekFraction
    val shownElapsedText = dragFraction?.let { fraction -> uiState.elapsedTextForFraction(fraction = fraction) }
        ?: uiState.elapsedText

    Column(
        modifier = modifier.fillMaxWidth(),
    ) {
        Slider(
            value = shownFraction,
            onValueChange = { fraction ->
                dragFraction = fraction
            },
            onValueChangeFinished = {
                val releasedFraction = dragFraction
                dragFraction = null
                if (releasedFraction == null) return@Slider
                onSeek(releasedFraction)
            },
            enabled = uiState.canSeek,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                disabledThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledActiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant,
                disabledInactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant,
            ),
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    stateDescription = "$shownElapsedText / ${uiState.totalText}"
                },
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = shownElapsedText,
                style = customizedTextStyle(
                    fontSize = 13,
                    fontWeight = 400,
                    lineHeight = 18,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                maxLines = 1,
            )
            Text(
                text = uiState.totalText,
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
}

@Preview(name = "Halfway", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun NowPlayingSeekBarPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingSeekBar(
                uiState = NowPlayingUiState(
                    playback = PlaybackState(
                        currentSong = Song(
                            id = 1L,
                            title = "Blue Sky",
                            artist = "The Band",
                            album = "Summer",
                            durationMs = 215_000L,
                            contentUri = "content://media/external/audio/media/1",
                            artworkUri = null,
                        ),
                        positionMs = 107_000L,
                        durationMs = 215_000L,
                    ),
                ),
            )
        }
    )
}

@Preview(name = "Length unknown", showBackground = true, backgroundColor = 0xFF111318)
@Composable
private fun NowPlayingSeekBarDisabledPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingSeekBar(uiState = NowPlayingUiState())
        }
    )
}
