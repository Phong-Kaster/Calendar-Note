package com.example.skeleton.ui.fragment.music

import com.example.skeleton.domain.model.Song

/**
 * Whether the app may read the device's music.
 * - [Unknown]: not checked yet (first frame before onResume).
 * - [Granted]: the user allowed it.
 * - [Denied]: the user has not allowed it (yet).
 * @author Phong-Kaster
 */
enum class MusicPermissionState { Unknown, Granted, Denied }

/**
 * Which one of the Music screen's views should be shown right now.
 * @author Phong-Kaster
 */
enum class MusicScreenContent { Loading, PermissionNeeded, Empty, SongList }

/**
 * UI state for the Music screen.
 *
 * @param permissionState Whether we may read the music library.
 * @param isLoading True while the song list is being read for the first time after permission is granted.
 * @param songs Songs from the repository, in repository order (sorted by title).
 * @author Phong-Kaster
 */
data class MusicUiState(
    val permissionState: MusicPermissionState = MusicPermissionState.Unknown,
    val isLoading: Boolean = false,
    val songs: List<Song> = emptyList(),
) {
    /** The one view the screen shows, decided from the fields above. */
    val content: MusicScreenContent = when {
        permissionState == MusicPermissionState.Unknown -> MusicScreenContent.Loading
        permissionState == MusicPermissionState.Denied -> MusicScreenContent.PermissionNeeded
        isLoading -> MusicScreenContent.Loading
        songs.isEmpty() -> MusicScreenContent.Empty
        else -> MusicScreenContent.SongList
    }
}
