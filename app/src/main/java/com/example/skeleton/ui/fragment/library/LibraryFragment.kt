package com.example.skeleton.ui.fragment.library

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.fragment.library.component.LibraryEmptyState
import com.example.skeleton.ui.fragment.library.component.LibraryPermissionRequired
import com.example.skeleton.ui.fragment.library.component.LibraryTopBar
import com.example.skeleton.ui.fragment.library.component.SongItem
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.util.PermissionUtil
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * The first screen of the app: the list of songs on the phone.
 * It owns the audio-permission flow:
 * 1. On first open, if we may not read music yet, it shows the system permission dialog.
 * 2. The dialog's answer goes to [LibraryViewModel.onPermissionResult], which decides
 *    "permanently denied" from the real answer (denied + no rationale).
 * 3. Every time the screen comes back (RESUMED, e.g. back from Settings) it re-checks the
 *    permission and reloads songs.
 * 4. Tapping a song plays the whole list from that song. On the first tap (Android 13+) it also
 *    asks once for the notification permission, so the media notification can show.
 *
 * @author Phong-Kaster
 */
class LibraryFragment : CoreFragment() {

    private val viewModel: LibraryViewModel by viewModel()

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    /**
     * Asks the system if we may read music right now and tells the ViewModel.
     * If yes, the ViewModel (re)loads songs.
     *
     * @author Phong-Kaster
     */
    private fun checkPermission() {
        val isGranted = PermissionUtil.isAudioPermissionGranted(context = requireContext())
        viewModel.onPermissionChecked(isGranted = isGranted)
    }

    /**
     * Opens this app's page in system Settings so the user can switch the permission on by hand.
     *
     * @author Phong-Kaster
     */
    private fun openAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", requireContext().packageName, null)
        }
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "openAppSettings failed", e)
        }
    }

    @OptIn(ExperimentalPermissionsApi::class)
    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()
        val audioPermissionState = rememberPermissionState(
            permission = PermissionUtil.audioPermission(),
            onPermissionResult = { isGranted ->
                viewModel.onPermissionResult(
                    isGranted = isGranted,
                    shouldShowRationale = shouldShowRequestPermissionRationale(PermissionUtil.audioPermission()),
                )
            },
        )

        /*
         * Notification permission (Android 13+): without it the media notification is hidden.
         * We ask once per screen, on the first song tap. Playback never waits for the answer,
         * so the answer callback does nothing.
         */
        val notificationPermissionState = rememberPermissionState(
            permission = Manifest.permission.POST_NOTIFICATIONS,
        )
        var hasAskedNotificationPermission by rememberSaveable { mutableStateOf(false) }

        /** Only prompts automatically once per screen (survives rotation); later prompts come from the button. */
        var hasAutoRequested by rememberSaveable { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            if (hasAutoRequested) return@LaunchedEffect
            hasAutoRequested = true
            if (PermissionUtil.isAudioPermissionGranted(context = requireContext())) return@LaunchedEffect
            audioPermissionState.launchPermissionRequest()
        }

        LibraryLayout(
            uiState = uiState,
            onRequestPermission = {
                audioPermissionState.launchPermissionRequest()
            },
            onOpenSettings = {
                openAppSettings()
            },
            onSongClick = { index ->
                viewModel.onSongClick(index = index)

                if (hasAskedNotificationPermission) return@LibraryLayout
                hasAskedNotificationPermission = true
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return@LibraryLayout
                if (notificationPermissionState.status.isGranted) return@LibraryLayout
                notificationPermissionState.launchPermissionRequest()
            },
        )
    }
}

/**
 * Pure UI of the Library screen. Picks one of: loading spinner, empty message,
 * song list, or the "please allow access" message.
 *
 * @param uiState what to show.
 * @param onRequestPermission shows the system permission dialog again.
 * @param onOpenSettings opens app Settings (used after "permanently denied").
 * @param onSongClick called with the tapped song's position in [LibraryUiState.songs].
 * @author Phong-Kaster
 */
@Composable
private fun LibraryLayout(
    uiState: LibraryUiState,
    onRequestPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSongClick: (Int) -> Unit = {},
) {
    CoreLayout(
        topBar = {
            LibraryTopBar()
        },
        content = {
            when {
                uiState.isLoading -> CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                uiState.showEmptyState -> LibraryEmptyState()
                uiState.hasPermission -> LibrarySongList(
                    songs = uiState.songs,
                    onSongClick = onSongClick,
                )
                uiState.isPermissionChecked -> LibraryPermissionRequired(
                    isPermanentlyDenied = uiState.isPermissionPermanentlyDenied,
                    onRequestPermission = onRequestPermission,
                    onOpenSettings = onOpenSettings,
                )
            }
        },
    )
}

/**
 * The scrolling list of songs. Bottom padding includes the navigation bar height,
 * so the last song is never hidden behind the system buttons.
 *
 * @param songs songs to list.
 * @param onSongClick called with the tapped song's position.
 * @author Phong-Kaster
 */
@Composable
private fun LibrarySongList(
    songs: List<Song>,
    onSongClick: (Int) -> Unit = {},
) {
    val navigationBarBottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp + navigationBarBottom),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        itemsIndexed(
            items = songs,
            key = { _, song -> song.id },
        ) { index, song ->
            SongItem(
                song = song,
                onClick = {
                    onSongClick(index)
                },
            )
        }
    }
}

/** Fake songs for previews only. */
private val previewSongs = listOf(
    Song(
        id = 1L,
        title = "Blue Sky",
        artist = "The Band",
        album = "Summer",
        durationMs = 215_000L,
        contentUri = "content://media/external/audio/media/1",
        artworkUri = null,
    ),
    Song(
        id = 2L,
        title = "A very long song title that keeps going and going",
        artist = "<unknown>",
        album = "",
        durationMs = 3_723_000L,
        contentUri = "content://media/external/audio/media/2",
        artworkUri = null,
    ),
)

@Preview(name = "Songs")
@Composable
private fun LibraryLayoutSongsPreview() {
    MyApplicationTheme(
        content = {
            LibraryLayout(
                uiState = LibraryUiState(
                    songs = previewSongs,
                    hasPermission = true,
                    isPermissionChecked = true,
                ),
            )
        }
    )
}

@Preview(name = "Loading")
@Composable
private fun LibraryLayoutLoadingPreview() {
    MyApplicationTheme(
        content = {
            LibraryLayout(
                uiState = LibraryUiState(
                    isLoading = true,
                    hasPermission = true,
                    isPermissionChecked = true,
                ),
            )
        }
    )
}

@Preview(name = "Empty")
@Composable
private fun LibraryLayoutEmptyPreview() {
    MyApplicationTheme(
        content = {
            LibraryLayout(
                uiState = LibraryUiState(
                    hasPermission = true,
                    isPermissionChecked = true,
                ),
            )
        }
    )
}

@Preview(name = "Permission denied")
@Composable
private fun LibraryLayoutDeniedPreview() {
    MyApplicationTheme(
        content = {
            LibraryLayout(
                uiState = LibraryUiState(
                    isPermissionChecked = true,
                ),
            )
        }
    )
}

@Preview(name = "Permission permanently denied")
@Composable
private fun LibraryLayoutPermanentlyDeniedPreview() {
    MyApplicationTheme(
        content = {
            LibraryLayout(
                uiState = LibraryUiState(
                    isPermissionChecked = true,
                    isPermissionPermanentlyDenied = true,
                ),
            )
        }
    )
}
