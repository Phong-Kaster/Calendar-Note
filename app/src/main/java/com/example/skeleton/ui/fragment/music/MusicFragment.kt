package com.example.skeleton.ui.fragment.music

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.Song
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.music.component.MusicPermissionNotice
import com.example.skeleton.ui.fragment.music.component.SongRow
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * Music tab: asks for the audio permission and lists the songs already on the device.
 *
 * The permission is re-checked in [onResume], so granting it from the system settings page
 * loads the list as soon as the user comes back — no restart needed.
 * @author Phong-Kaster
 */
class MusicFragment : CoreFragment() {

    private val viewModel: MusicViewModel by viewModel()

    /** The permission this device needs to read music (depends on the Android version). */
    private val audioPermission: String = audioPermissionFor(sdkInt = Build.VERSION.SDK_INT)

    /**
     * True after the user denied with "don't ask again": the system dialog will not show any
     * more, so the button opens the app settings page instead.
     */
    private var shouldOpenSettings by mutableStateOf(false)

    /** When the last permission dialog was asked for (ms since boot), to spot a blocked dialog. */
    private var permissionRequestedAtMs: Long = 0L

    override fun onResume() {
        super.onResume()
        checkPermission()
    }

    /**
     * Reads the current grant state and forwards it to the ViewModel.
     * @author Phong-Kaster
     */
    private fun checkPermission() {
        val granted = ContextCompat.checkSelfPermission(requireContext(), audioPermission) ==
            PackageManager.PERMISSION_GRANTED
        viewModel.onPermissionResult(granted = granted)
    }

    /**
     * Called with the answer of the system permission dialog.
     * If the user denied and Android says no rationale is needed any more, the dialog is
     * blocked for good ("don't ask again") → next tap opens settings.
     * @author Phong-Kaster
     */
    private fun handlePermissionResult(granted: Boolean) {
        viewModel.onPermissionResult(granted = granted)
        if (granted) {
            shouldOpenSettings = false
            return
        }

        // A "no" that comes back almost instantly means Android did not even show the dialog:
        // it is blocked for good. Then this very tap goes to settings, so it is never a dead tap
        // (also right after an app restart, when [shouldOpenSettings] was forgotten).
        // A slower "no" came from a real dialog, which the user may just have dismissed.
        val answeredInstantly = SystemClock.elapsedRealtime() - permissionRequestedAtMs < BLOCKED_DIALOG_THRESHOLD_MS
        val dialogBlocked = !shouldShowRequestPermissionRationale(audioPermission) && answeredInstantly
        shouldOpenSettings = dialogBlocked
        if (dialogBlocked) openAppSettings()
    }

    /**
     * Shows the system permission dialog and remembers when it was asked for.
     * @param launcher The launcher that shows the dialog.
     * @author Phong-Kaster
     */
    private fun requestPermission(launcher: ActivityResultLauncher<String>) {
        permissionRequestedAtMs = SystemClock.elapsedRealtime()
        launcher.launch(audioPermission)
    }

    /**
     * Opens this app's page in the system settings so the user can allow the permission there.
     * @author Phong-Kaster
     */
    private fun openAppSettings() {
        val intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", requireContext().packageName, null),
        )
        startActivity(intent)
    }

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { granted ->
                handlePermissionResult(granted = granted)
            },
        )

        MusicLayout(
            uiState = uiState,
            shouldOpenSettings = shouldOpenSettings,
            onRequestPermission = {
                requestPermission(launcher = permissionLauncher)
            },
            onOpenSettings = {
                openAppSettings()
            },
            onSongClick = { song ->
                // T-003 wires playback.
            },
        )
    }

    companion object {
        /** A permission answer faster than this (ms) means no dialog was shown. */
        private const val BLOCKED_DIALOG_THRESHOLD_MS = 350L
    }
}

/**
 * Music screen UI: top bar, bottom bar and one of loading / permission notice / empty / list.
 * @param uiState What to show.
 * @param shouldOpenSettings True when the notice button must open app settings.
 * @param onRequestPermission Show the system permission dialog.
 * @param onOpenSettings Open this app's settings page.
 * @param onSongClick A song row was tapped.
 * @author Phong-Kaster
 */
@Composable
private fun MusicLayout(
    uiState: MusicUiState,
    shouldOpenSettings: Boolean = false,
    onRequestPermission: () -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onSongClick: (Song) -> Unit = {},
) {
    CoreLayout(
        modifier = Modifier,
        showLoading = uiState.content == MusicScreenContent.Loading,
        topBar = { CoreTopBar(title = stringResource(R.string.music)) },
        bottomBar = { CoreBottomBar() },
        content = {
            when (uiState.content) {
                MusicScreenContent.Loading -> {
                    // CoreLayout already shows the spinner through showLoading.
                }
                MusicScreenContent.PermissionNeeded -> {
                    MusicPermissionNotice(
                        shouldOpenSettings = shouldOpenSettings,
                        onRequestPermission = onRequestPermission,
                        onOpenSettings = onOpenSettings,
                    )
                }
                MusicScreenContent.Empty -> {
                    Text(
                        text = stringResource(R.string.no_songs_found),
                        style = customizedTextStyle(
                            fontSize = 16,
                            fontWeight = 400,
                            lineHeight = 24,
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                    )
                }
                MusicScreenContent.SongList -> {
                    MusicSongList(
                        songs = uiState.songs,
                        onSongClick = onSongClick,
                    )
                }
            }
        },
    )
}

/**
 * Scrollable list of songs, one [SongRow] each.
 * @param songs Songs in display order.
 * @param onSongClick A song row was tapped.
 * @author Phong-Kaster
 */
@Composable
private fun MusicSongList(
    songs: List<Song>,
    onSongClick: (Song) -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp),
    ) {
        items(
            items = songs,
            key = { song -> "Song_${song.id}" },
            itemContent = { song ->
                SongRow(
                    song = song,
                    onClick = {
                        onSongClick(song)
                    },
                )
            },
        )
    }
}

@Preview(name = "Song list")
@Composable
private fun MusicLayoutListPreview() {
    MyApplicationTheme(
        content = {
            MusicLayout(
                uiState = MusicUiState(
                    permissionState = MusicPermissionState.Granted,
                    songs = listOf(
                        Song(1, "Yesterday", "The Beatles", 125_000, "content://media/external/audio/media/1"),
                        Song(2, "A very long song title that does not fit on a single line at all", null, 245_000, "content://media/external/audio/media/2"),
                    ),
                ),
            )
        }
    )
}

@Preview(name = "Permission needed")
@Composable
private fun MusicLayoutPermissionPreview() {
    MyApplicationTheme(
        content = {
            MusicLayout(uiState = MusicUiState(permissionState = MusicPermissionState.Denied))
        }
    )
}

@Preview(name = "Empty")
@Composable
private fun MusicLayoutEmptyPreview() {
    MyApplicationTheme(
        content = {
            MusicLayout(uiState = MusicUiState(permissionState = MusicPermissionState.Granted))
        }
    )
}
