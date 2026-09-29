package com.example.skeleton.ui.fragment.library.component

import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.component.SongArtwork
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * The small "now playing" strip at the bottom of the Library screen.
 * From top to bottom: a thin progress line, then a row with cover, title, artist and a
 * play/pause button. Tapping the row (not the button) opens the Now Playing screen.
 * It pads itself above the system navigation bar, so it is never hidden behind it.
 * It draws nothing when [playback] has no current song.
 *
 * Example:
 * ```kotlin
 * CoreLayout(bottomBar = { MiniPlayerBar(playback = uiState.playback, onOpenNowPlaying = { ... }) }, ...)
 * ```
 *
 * @param playback what the player is doing right now.
 * @param modifier extra layout tweaks.
 * @param onPlayPauseClick called when the play/pause button is tapped.
 * @param onOpenNowPlaying called when the rest of the bar is tapped.
 * @author Phong-Kaster
 */
@Composable
fun MiniPlayerBar(
    playback: PlaybackState,
    modifier: Modifier = Modifier,
    onPlayPauseClick: () -> Unit = {},
    onOpenNowPlaying: () -> Unit = {},
) {
    val song = playback.currentSong ?: return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(color = MaterialTheme.colorScheme.surfaceVariant)
            .navigationBarsPadding(),
    ) {
        MiniPlayerProgressLine(progressFraction = playback.progressFraction)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            MiniPlayerSongInfo(
                song = song,
                modifier = Modifier.weight(1f),
                onClick = onOpenNowPlaying,
            )

            MiniPlayerPlayPauseButton(
                isPlaying = playback.isPlaying,
                onClick = onPlayPauseClick,
            )
        }
    }
}

/**
 * A 2dp line across the whole bar. The coloured part grows from left to right as the song plays.
 *
 * @param progressFraction how much of the song is done, 0f..1f.
 * @author Phong-Kaster
 */
@Composable
private fun MiniPlayerProgressLine(
    progressFraction: Float,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.24f)),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction = progressFraction.coerceIn(0f, 1f))
                .fillMaxHeight()
                .background(color = MaterialTheme.colorScheme.primary),
        )
    }
}

/**
 * The tappable left part of the bar: cover, title and artist. Long texts scroll (marquee).
 *
 * @param song the song that is loaded in the player.
 * @param modifier extra layout tweaks (the caller gives it the free width).
 * @param onClick opens the Now Playing screen.
 * @author Phong-Kaster
 */
@Composable
private fun MiniPlayerSongInfo(
    song: Song,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    Row(
        modifier = modifier
            .clickable(
                onClickLabel = stringResource(R.string.now_playing),
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SongArtwork(size = 40.dp)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = song.title,
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
                text = artistDisplayText(artist = song.artist),
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
    }
}

/**
 * Round play/pause button. Shows "pause" while music plays, "play" while it is paused,
 * and tells screen readers the same word.
 *
 * @param isPlaying true while sound is coming out.
 * @param onClick toggles play/pause.
 * @author Phong-Kaster
 */
@Composable
private fun MiniPlayerPlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit = {},
) {
    val iconRes = if (isPlaying) R.drawable.ic_player_pause else R.drawable.ic_player_play
    val labelRes = if (isPlaying) R.string.pause else R.string.play

    IconButton(
        onClick = onClick,
        content = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = stringResource(labelRes),
                tint = MaterialTheme.colorScheme.onSurface,
            )
        },
    )
}

/** Fake song for previews only. */
private val previewSong = Song(
    id = 1L,
    title = "A very long song title that should scroll instead of wrapping",
    artist = "The Band",
    album = "Summer",
    durationMs = 215_000L,
    contentUri = "content://media/external/audio/media/1",
    artworkUri = null,
)

@Preview(name = "Playing")
@Composable
private fun MiniPlayerBarPlayingPreview() {
    MyApplicationTheme(
        content = {
            MiniPlayerBar(
                playback = PlaybackState(
                    currentSong = previewSong,
                    isPlaying = true,
                    positionMs = 80_000L,
                    durationMs = 215_000L,
                    currentIndex = 0,
                    queueSize = 1,
                ),
            )
        }
    )
}

@Preview(name = "Paused, unknown artist")
@Composable
private fun MiniPlayerBarPausedPreview() {
    MyApplicationTheme(
        content = {
            MiniPlayerBar(
                playback = PlaybackState(
                    currentSong = previewSong.copy(title = "Voice memo", artist = "<unknown>"),
                    isPlaying = false,
                    positionMs = 20_000L,
                    durationMs = 59_000L,
                    currentIndex = 0,
                    queueSize = 1,
                ),
            )
        }
    )
}
