package com.example.skeleton.ui.fragment.nowplaying.component

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.example.skeleton.R
import com.example.skeleton.ui.fragment.nowplaying.model.formatPlaybackTime
import com.example.skeleton.ui.fragment.nowplaying.model.fractionForPosition
import com.example.skeleton.ui.fragment.nowplaying.model.positionForFraction
import com.example.skeleton.ui.theme.customizedTextStyle

/*
 * --- Dragging the slider (simple story) ---
 * While the finger is on the slider we remember where it is in `dragFraction` and show THAT
 * (thumb and elapsed time), ignoring the player's ticking position, so the thumb never jumps
 * back under the finger. When the finger lets go we ask for one seek to that spot and forget
 * `dragFraction`, so the slider follows the player again.
 */

/**
 * The song's time line: a slider plus "elapsed" on the left and "total" on the right.
 *
 * Example:
 * ```kotlin
 * NowPlayingSeekBar(positionMs = 30_000L, durationMs = 180_000L, onSeek = { ms -> viewModel.onSeek(ms) })
 * ```
 *
 * @param positionMs Where the player is, in milliseconds.
 * @param durationMs Length of the song in milliseconds; 0 when unknown (the slider is then disabled).
 * @param modifier Size / position from the caller.
 * @param onSeek Called once with the chosen time when the user lets go of the slider.
 * @author Phong-Kaster
 */
@Composable
fun NowPlayingSeekBar(
    positionMs: Long,
    durationMs: Long,
    modifier: Modifier = Modifier,
    onSeek: (Long) -> Unit = {},
) {
    var dragFraction by remember { mutableStateOf<Float?>(null) }
    val sliderFraction = dragFraction ?: fractionForPosition(positionMs = positionMs, durationMs = durationMs)
    val shownPositionMs = dragFraction
        ?.let { fraction -> positionForFraction(fraction = fraction, durationMs = durationMs) }
        ?: positionMs
    val sliderDescription = stringResource(R.string.song_position)

    Column(
        modifier = modifier.fillMaxWidth(),
        content = {
            Slider(
                value = sliderFraction,
                onValueChange = { fraction ->
                    dragFraction = fraction
                },
                onValueChangeFinished = {
                    val finalFraction = dragFraction
                    dragFraction = null
                    if (finalFraction != null) {
                        onSeek(positionForFraction(fraction = finalFraction, durationMs = durationMs))
                    }
                },
                enabled = durationMs > 0L,
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics { contentDescription = sliderDescription },
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    PlaybackTimeLabel(text = formatPlaybackTime(ms = shownPositionMs))
                    Spacer(modifier = Modifier.weight(1f))
                    PlaybackTimeLabel(text = formatPlaybackTime(ms = durationMs))
                },
            )
        },
    )
}

/**
 * One small time text under the slider, e.g. "1:05".
 *
 * @param text The time to show.
 * @author Phong-Kaster
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PlaybackTimeLabel(text: String) {
    Text(
        text = text,
        style = customizedTextStyle(
            fontSize = 13,
            fontWeight = 500,
            lineHeight = 18,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        maxLines = 1,
        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
    )
}

@Preview(name = "Halfway")
@Composable
private fun NowPlayingSeekBarPreview() {
    NowPlayingSeekBar(positionMs = 62_000L, durationMs = 125_000L)
}

@Preview(name = "Unknown length")
@Composable
private fun NowPlayingSeekBarUnknownPreview() {
    NowPlayingSeekBar(positionMs = 0L, durationMs = 0L)
}
