package com.example.skeleton.ui.fragment.music

import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.NowPlaying
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.music.component.MusicEmptyState
import com.example.skeleton.ui.fragment.music.component.MusicNotificationPermissionRequest
import com.example.skeleton.ui.fragment.music.component.MusicPermissionDenied
import com.example.skeleton.ui.fragment.music.component.MusicPermissionRequest
import com.example.skeleton.ui.fragment.music.component.NowPlayingBar
import com.example.skeleton.ui.fragment.music.component.SongItem
import com.example.skeleton.ui.fragment.music.model.MusicScreenContent
import com.example.skeleton.ui.fragment.music.model.audioPermissionFor
import com.example.skeleton.ui.util.NavigationUtil.safeNavigate
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Music tab: lists every song stored on the phone.
 *
 * The Fragment owns the ViewModel and the permission helper; the drawing is done by
 * [MusicLayout]. Each time the screen comes back (for example after the user switched the
 * permission on in Settings), it re-checks the permission so the list appears without a restart.
 *
 * @author Phong-Kaster
 */
class MusicFragment : CoreFragment() {
    private val viewModel: MusicViewModel by viewModel()

    /** Goes up by one every time the user taps "Grant permission". */
    private var triggerRequestPermission by mutableIntStateOf(0)

    /** Goes up by one on every song tap; the helper decides whether to ask for notifications. */
    private var triggerNotificationPermission by mutableIntStateOf(0)

    override fun onResume() {
        super.onResume()
        viewModel.onPermissionResult(granted = isAudioPermissionGranted())
    }

    /**
     * Asks Android right now whether the audio permission is granted.
     *
     * @author Phong-Kaster
     */
    private fun isAudioPermissionGranted(): Boolean {
        val permission = audioPermissionFor(sdkInt = Build.VERSION.SDK_INT)
        return ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED
    }

    @Composable
    override fun ComposeView() {
        super.ComposeView()
        val uiState by viewModel.uiState.collectAsState()

        MusicLayout(
            uiState = uiState,
            onGrantPermission = {
                triggerRequestPermission++
            },
            onSongClick = { song ->
                // Play right away; the notification question never blocks playback.
                viewModel.onSongClick(song = song)
                triggerNotificationPermission++
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
            onNowPlayingClick = {
                safeNavigate(R.id.toNowPlaying)
            },
        )

        // Invisible helper: asks for the audio permission and reports the answer.
        MusicPermissionRequest(
            requestTrigger = triggerRequestPermission,
            onPermissionResult = { granted ->
                viewModel.onPermissionResult(granted = granted)
            },
        )

        // Invisible helper: on Android 13+ asks once per visit to show the music notification.
        MusicNotificationPermissionRequest(requestTrigger = triggerNotificationPermission)
    }
}

/**
 * Pure UI of the Music screen. It shows exactly one body picked by
 * [MusicUiState.screenContent]: spinner, permission message, "no songs" or the list.
 * While a song is loaded, the now-playing bar sits just above the bottom bar.
 *
 * @param uiState Current screen state.
 * @param onGrantPermission Called when the user taps the grant button.
 * @param onSongClick Called when the user taps a song row.
 * @param onPreviousClick Called when the user taps previous in the now-playing bar.
 * @param onPlayPauseClick Called when the user taps play / pause in the now-playing bar.
 * @param onNextClick Called when the user taps next in the now-playing bar.
 * @param onNowPlayingClick Called when the user taps the now-playing bar itself.
 * @author Phong-Kaster
 */
@Composable
private fun MusicLayout(
    uiState: MusicUiState,
    onGrantPermission: () -> Unit = {},
    onSongClick: (Song) -> Unit = {},
    onPreviousClick: () -> Unit = {},
    onPlayPauseClick: () -> Unit = {},
    onNextClick: () -> Unit = {},
    onNowPlayingClick: () -> Unit = {},
) {
    val screenContent = uiState.screenContent
    val currentSong = uiState.nowPlaying.currentSong

    CoreLayout(
        showLoading = screenContent == MusicScreenContent.Loading,
        topBar = {
            CoreTopBar(title = stringResource(R.string.music))
        },
        bottomBar = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                content = {
                    if (currentSong != null) {
                        NowPlayingBar(
                            song = currentSong,
                            isPlaying = uiState.nowPlaying.isPlaying,
                            onClick = onNowPlayingClick,
                            onPreviousClick = onPreviousClick,
                            onPlayPauseClick = onPlayPauseClick,
                            onNextClick = onNextClick,
                        )
                    }
                    CoreBottomBar()
                },
            )
        },
        content = {
            when (screenContent) {
                // CoreLayout already draws the spinner when showLoading is true.
                MusicScreenContent.Loading -> Unit
                MusicScreenContent.PermissionDenied -> MusicPermissionDenied(onGrantPermission = onGrantPermission)
                MusicScreenContent.Empty -> MusicEmptyState()
                MusicScreenContent.Songs -> MusicSongList(
                    songs = uiState.songs,
                    currentSongId = uiState.currentSongId,
                    onSongClick = onSongClick,
                )
            }
        },
    )
}

/**
 * Scrollable list of songs, one [SongItem] per song, keyed by song id.
 *
 * @param songs Songs to show, already sorted by title.
 * @param currentSongId Id of the song loaded in the player; its row is highlighted.
 * @param onSongClick Called with the tapped song.
 * @author Phong-Kaster
 */
@Composable
private fun MusicSongList(
    songs: List<Song>,
    currentSongId: Long?,
    onSongClick: (Song) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        content = {
            items(
                items = songs,
                key = { song -> song.id },
                itemContent = { song ->
                    SongItem(
                        song = song,
                        modifier = Modifier.padding(horizontal = 16.dp),
                        isPlaying = song.id == currentSongId,
                        onClick = {
                            onSongClick(song)
                        },
                    )
                },
            )
        },
    )
}

@Preview(name = "Loading")
@Composable
private fun MusicLayoutLoadingPreview() {
    MusicLayout(
        uiState = MusicUiState(isPermissionGranted = true, isLoading = true),
    )
}

@Preview(name = "Permission denied")
@Composable
private fun MusicLayoutPermissionDeniedPreview() {
    MusicLayout(
        uiState = MusicUiState(isPermissionGranted = false),
    )
}

@Preview(name = "Empty")
@Composable
private fun MusicLayoutEmptyPreview() {
    MusicLayout(
        uiState = MusicUiState(isPermissionGranted = true, songs = emptyList()),
    )
}

@Preview(name = "Now playing")
@Composable
private fun MusicLayoutNowPlayingPreview() {
    val song = Song(
        id = 1L,
        title = "Yesterday",
        artist = "The Beatles",
        album = "Help!",
        durationMs = 125_000L,
        contentUri = "content://media/external/audio/media/1",
    )
    MusicLayout(
        uiState = MusicUiState(
            isPermissionGranted = true,
            songs = listOf(song),
            nowPlaying = NowPlaying(currentSong = song, isPlaying = true, currentIndex = 0, queueSize = 1),
        ),
    )
}

@Preview(name = "Songs")
@Composable
private fun MusicLayoutSongsPreview() {
    MusicLayout(
        uiState = MusicUiState(
            isPermissionGranted = true,
            songs = listOf(
                Song(
                    id = 1L,
                    title = "Yesterday",
                    artist = "The Beatles",
                    album = "Help!",
                    durationMs = 125_000L,
                    contentUri = "content://media/external/audio/media/1",
                ),
                Song(
                    id = 2L,
                    title = "Unnamed recording",
                    artist = null,
                    album = null,
                    durationMs = 60_000L,
                    contentUri = "content://media/external/audio/media/2",
                ),
            ),
        ),
    )
}
