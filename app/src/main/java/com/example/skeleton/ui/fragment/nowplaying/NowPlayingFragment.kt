package com.example.skeleton.ui.fragment.nowplaying

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.NowPlaying
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.fragment.nowplaying.component.NowPlayingArtwork
import com.example.skeleton.ui.fragment.nowplaying.component.NowPlayingControls
import com.example.skeleton.ui.fragment.nowplaying.component.NowPlayingSeekBar
import com.example.skeleton.ui.fragment.nowplaying.component.NowPlayingSongInfo
import com.example.skeleton.ui.fragment.nowplaying.component.NowPlayingTopBar
import com.example.skeleton.ui.util.NavigationUtil.safeNavigateUp
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Full-screen player opened by tapping the mini bar on the Music tab.
 *
 * The Fragment owns the ViewModel and the navigation; [NowPlayingLayout] does the drawing.
 * When the player has no song anymore (for example the music was stopped from the
 * notification), the screen closes itself and goes back.
 *
 * @author Phong-Kaster
 */
class NowPlayingFragment : CoreFragment() {
    private val viewModel: NowPlayingViewModel by viewModel()

    @Composable
    override fun ComposeView() {
        super.ComposeView()
        val uiState by viewModel.uiState.collectAsState()

        // Nothing left to show: go back to where the user came from.
        LaunchedEffect(uiState.isSongGone) {
            if (uiState.isSongGone) safeNavigateUp()
        }

        NowPlayingLayout(
            uiState = uiState,
            onBackClick = {
                safeNavigateUp()
            },
            onPreviousClick = {
                viewModel.onPreviousClick()
            },
            onPlayPauseClick = {
                viewModel.onPlayPauseClick()
            },
            onNextClick = {
                viewModel.onNextClick()
            },
            onSeek = { positionMs ->
                viewModel.onSeek(positionMs = positionMs)
            },
        )
    }
}

/**
 * Pure UI of the Now Playing screen: top bar with back, then artwork, title and artist,
 * the time line, and the big player buttons. Shows only the top bar while no song is loaded.
 *
 * @param uiState Current screen state.
 * @param onBackClick Called when the user taps back.
 * @param onPreviousClick Called when the user taps previous.
 * @param onPlayPauseClick Called when the user taps play / pause.
 * @param onNextClick Called when the user taps next.
 * @param onSeek Called with the chosen time (ms) when the user lets go of the slider.
 * @author Phong-Kaster
 */
@Composable
private fun NowPlayingLayout(
    uiState: NowPlayingUiState,
    onBackClick: () -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onPlayPauseClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onSeek: (Long) -> Unit = {},
) {
    val currentSong = uiState.nowPlaying.currentSong

    CoreLayout(
        topBar = {
            NowPlayingTopBar(onBackClick = onBackClick)
        },
        content = {
            if (currentSong != null) {
                NowPlayingContent(
                    song = currentSong,
                    uiState = uiState,
                    onPreviousClick = onPreviousClick,
                    onPlayPauseClick = onPlayPauseClick,
                    onNextClick = onNextClick,
                    onSeek = onSeek,
                )
            }
        },
    )
}

/**
 * The body of the screen for a loaded [song]. The artwork takes whatever height is left,
 * so the buttons always stay on screen.
 *
 * @param song The song loaded in the player.
 * @param uiState Current screen state (play/pause, position, length).
 * @param onPreviousClick Called when the user taps previous.
 * @param onPlayPauseClick Called when the user taps play / pause.
 * @param onNextClick Called when the user taps next.
 * @param onSeek Called with the chosen time (ms) when the user lets go of the slider.
 * @author Phong-Kaster
 */
@Composable
private fun NowPlayingContent(
    song: Song,
    uiState: NowPlayingUiState,
    onPreviousClick: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onNextClick: () -> Unit,
    onSeek: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding()
            .padding(start = 24.dp, end = 24.dp, top = 16.dp, bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 24.dp, alignment = Alignment.CenterVertically),
        content = {
            NowPlayingArtwork(
                albumArtUri = song.albumArtUri,
                modifier = Modifier.weight(weight = 1f, fill = false),
            )

            NowPlayingSongInfo(song = song)

            NowPlayingSeekBar(
                positionMs = uiState.positionMs,
                durationMs = uiState.durationMs,
                onSeek = onSeek,
            )

            NowPlayingControls(
                isPlaying = uiState.nowPlaying.isPlaying,
                onPreviousClick = onPreviousClick,
                onPlayPauseClick = onPlayPauseClick,
                onNextClick = onNextClick,
            )
        },
    )
}

@Preview(name = "Playing")
@Composable
private fun NowPlayingLayoutPlayingPreview() {
    val song = Song(
        id = 1L,
        title = "Yesterday",
        artist = "The Beatles",
        album = "Help!",
        durationMs = 125_000L,
        contentUri = "content://media/external/audio/media/1",
    )
    NowPlayingLayout(
        uiState = NowPlayingUiState(
            nowPlaying = NowPlaying(currentSong = song, isPlaying = true, currentIndex = 0, queueSize = 3, durationMs = 125_000L),
            positionMs = 47_000L,
        ),
    )
}

@Preview(name = "Paused")
@Composable
private fun NowPlayingLayoutPausedPreview() {
    val song = Song(
        id = 2L,
        title = "voice_memo_final_mix",
        artist = null,
        album = null,
        durationMs = 60_000L,
        contentUri = "content://media/external/audio/media/2",
    )
    NowPlayingLayout(
        uiState = NowPlayingUiState(
            nowPlaying = NowPlaying(currentSong = song, isPlaying = false, currentIndex = 1, queueSize = 3, durationMs = 60_000L),
            positionMs = 12_000L,
        ),
    )
}

@Preview(name = "Long title")
@Composable
private fun NowPlayingLayoutLongTitlePreview() {
    val song = Song(
        id = 3L,
        title = "A Very Long Song Title That Keeps Going And Going Far Past The Edge Of The Screen",
        artist = "An Orchestra With An Equally Long Name And Several Featured Guest Soloists",
        album = "Live Recordings",
        durationMs = 3_725_000L,
        contentUri = "content://media/external/audio/media/3",
    )
    NowPlayingLayout(
        uiState = NowPlayingUiState(
            nowPlaying = NowPlaying(currentSong = song, isPlaying = true, currentIndex = 2, queueSize = 3, durationMs = 3_725_000L),
            positionMs = 1_800_000L,
        ),
    )
}
