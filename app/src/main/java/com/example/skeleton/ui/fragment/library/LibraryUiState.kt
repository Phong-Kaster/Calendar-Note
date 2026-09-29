package com.example.skeleton.ui.fragment.library

import com.example.skeleton.domain.model.Song

/**
 * UI state for the Library screen (the list of songs on the phone).
 *
 * Example:
 * ```kotlin
 * LibraryUiState(hasPermission = true, isPermissionChecked = true, songs = listOf(song))
 * ```
 *
 * @param isLoading true while songs are being read the first time.
 * @param songs every song found on the phone, sorted by title.
 * @param hasPermission true when the app may read audio files.
 * @param isPermissionChecked false until we have asked the system once; avoids flashing the "allow" screen.
 * @param isPermissionPermanentlyDenied true when the user said "no" and the system won't ask again,
 * so the button must open app Settings instead of showing the system dialog.
 * @author Phong-Kaster
 */
data class LibraryUiState(
    val isLoading: Boolean = false,

    // --- Domain data ---
    val songs: List<Song> = emptyList(),

    // --- Permission ---
    val hasPermission: Boolean = false,
    val isPermissionChecked: Boolean = false,
    val isPermissionPermanentlyDenied: Boolean = false,
) {
    /** True when we are allowed to look, finished looking, and found no songs. */
    val showEmptyState: Boolean = hasPermission && !isLoading && songs.isEmpty()
}
