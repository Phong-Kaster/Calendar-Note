package com.example.skeleton.ui.fragment.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.RepeatMode
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.nowplaying.component.NowPlayingControls
import com.example.skeleton.ui.fragment.nowplaying.component.NowPlayingEmptyState
import com.example.skeleton.ui.fragment.nowplaying.component.NowPlayingSeekBar
import com.example.skeleton.ui.fragment.nowplaying.component.NowPlayingSongInfo
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.util.NavigationUtil.safeNavigateUp
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The full-screen player: big cover, song title and artist, seek bar and all the buttons.
 * It shows the SAME player the notification controls, so both always agree.
 * When nothing is playing it explains that instead, and the back button still works.
 *
 * @author Phong-Kaster
 */
class NowPlayingFragment : CoreFragment() {

    private val viewModel: NowPlayingViewModel by viewModel()

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        NowPlayingLayout(
            uiState = uiState,
            onBack = {
                safeNavigateUp()
            },
            onToggleShuffle = {
                viewModel.toggleShuffle()
            },
            onSkipToPrevious = {
                viewModel.skipToPrevious()
            },
            onTogglePlayPause = {
                viewModel.togglePlayPause()
            },
            onSkipToNext = {
                viewModel.skipToNext()
            },
            onCycleRepeat = {
                viewModel.cycleRepeatMode()
            },
            onSeek = { fraction ->
                viewModel.seekTo(fraction = fraction)
            },
        )
    }
}

/**
 * Pure UI of the Now Playing screen: top bar with back, then either the player or the
 * "nothing is playing" message.
 *
 * @param uiState what to show.
 * @param onBack back button tapped.
 * @param onToggleShuffle shuffle tapped.
 * @param onSkipToPrevious previous tapped.
 * @param onTogglePlayPause play / pause tapped.
 * @param onSkipToNext next tapped.
 * @param onCycleRepeat repeat tapped.
 * @param onSeek user let go of the seek bar knob at this spot (0f..1f).
 * @author Phong-Kaster
 */
@Composable
private fun NowPlayingLayout(
    uiState: NowPlayingUiState,
    onBack: () -> Unit = {},
    onToggleShuffle: () -> Unit = {},
    onSkipToPrevious: () -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onSkipToNext: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
    onSeek: (Float) -> Unit = {},
) {
    CoreLayout(
        topBar = {
            CoreTopBar(
                title = stringResource(R.string.now_playing),
                leftIcon = R.drawable.ic_back,
                leftContentDescription = stringResource(R.string.back),
                onClickLeft = onBack,
            )
        },
        content = {
            if (uiState.hasSong) {
                NowPlayingPlayer(
                    uiState = uiState,
                    onToggleShuffle = onToggleShuffle,
                    onSkipToPrevious = onSkipToPrevious,
                    onTogglePlayPause = onTogglePlayPause,
                    onSkipToNext = onSkipToNext,
                    onCycleRepeat = onCycleRepeat,
                    onSeek = onSeek,
                )
            } else {
                NowPlayingEmptyState()
            }
        },
    )
}

/**
 * The player part: cover + names on top, seek bar and buttons below.
 * It scrolls on very short screens (landscape) so no button is ever cut off.
 *
 * @param uiState what to show.
 * @param onToggleShuffle shuffle tapped.
 * @param onSkipToPrevious previous tapped.
 * @param onTogglePlayPause play / pause tapped.
 * @param onSkipToNext next tapped.
 * @param onCycleRepeat repeat tapped.
 * @param onSeek user let go of the seek bar knob at this spot (0f..1f).
 * @author Phong-Kaster
 */
@Composable
private fun NowPlayingPlayer(
    uiState: NowPlayingUiState,
    onToggleShuffle: () -> Unit = {},
    onSkipToPrevious: () -> Unit = {},
    onTogglePlayPause: () -> Unit = {},
    onSkipToNext: () -> Unit = {},
    onCycleRepeat: () -> Unit = {},
    onSeek: (Float) -> Unit = {},
) {
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 24.dp + navigationBarBottom),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically),
    ) {
        NowPlayingSongInfo(
            title = uiState.title,
            artist = uiState.artist,
            isArtistUnknown = uiState.isArtistUnknown,
        )

        NowPlayingSeekBar(
            uiState = uiState,
            onSeek = onSeek,
        )

        NowPlayingControls(
            uiState = uiState,
            onToggleShuffle = onToggleShuffle,
            onSkipToPrevious = onSkipToPrevious,
            onTogglePlayPause = onTogglePlayPause,
            onSkipToNext = onSkipToNext,
            onCycleRepeat = onCycleRepeat,
        )
    }
}

/** Fake song for previews only. */
private val previewSong = Song(
    id = 1L,
    title = "A very long song title that keeps going and going",
    artist = "The Band",
    album = "Summer",
    durationMs = 215_000L,
    contentUri = "content://media/external/audio/media/1",
    artworkUri = null,
)

@Preview(name = "Playing")
@Composable
private fun NowPlayingLayoutPlayingPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingLayout(
                uiState = NowPlayingUiState(
                    playback = PlaybackState(
                        currentSong = previewSong,
                        isPlaying = true,
                        positionMs = 61_000L,
                        durationMs = 215_000L,
                        currentIndex = 0,
                        queueSize = 3,
                    ),
                ),
            )
        }
    )
}

@Preview(name = "Paused")
@Composable
private fun NowPlayingLayoutPausedPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingLayout(
                uiState = NowPlayingUiState(
                    playback = PlaybackState(
                        currentSong = previewSong,
                        isPlaying = false,
                        positionMs = 180_000L,
                        durationMs = 215_000L,
                        currentIndex = 2,
                        queueSize = 3,
                    ),
                ),
            )
        }
    )
}

@Preview(name = "Shuffle + repeat all")
@Composable
private fun NowPlayingLayoutModesPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingLayout(
                uiState = NowPlayingUiState(
                    playback = PlaybackState(
                        currentSong = previewSong,
                        isPlaying = true,
                        positionMs = 30_000L,
                        durationMs = 215_000L,
                        currentIndex = 1,
                        queueSize = 3,
                        shuffleEnabled = true,
                        repeatMode = RepeatMode.All,
                    ),
                ),
            )
        }
    )
}

@Preview(name = "Nothing playing")
@Composable
private fun NowPlayingLayoutEmptyPreview() {
    MyApplicationTheme(
        content = {
            NowPlayingLayout(uiState = NowPlayingUiState())
        }
    )
}
