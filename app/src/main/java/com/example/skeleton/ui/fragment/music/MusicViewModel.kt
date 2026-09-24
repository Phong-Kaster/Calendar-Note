package com.example.skeleton.ui.fragment.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.repository.SongRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Drives the Music screen: remembers whether we may read music and loads the device's songs.
 *
 * The Fragment checks the permission (on every resume and after the system dialog) and
 * reports it with [onPermissionResult]. This class never touches Android APIs, so it can be
 * unit-tested on the JVM.
 *
 * Example: `viewModel.onPermissionResult(granted = true)` → loading → song list or empty state.
 * @param songRepository Where the songs come from.
 * @author Phong-Kaster
 */
class MusicViewModel(
    private val songRepository: SongRepository,
) : ViewModel() {

    private val TAG = "MusicViewModel"

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    /** The running song load, so a newer load can replace an older one. */
    private var loadSongsJob: Job? = null

    /**
     * Tells the screen whether the audio permission is granted.
     * - Denied → the "allow access" notice is shown and nothing is loaded.
     * - Granted → the songs are (re)loaded. The loading spinner only shows when there was no
     *   list yet; a later resume refreshes quietly so the list does not flicker.
     * @param granted True when the user allowed reading music.
     * @author Phong-Kaster
     */
    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            val wasAlreadyGranted = _uiState.value.permissionState == MusicPermissionState.Granted
            _uiState.value = _uiState.value.copy(permissionState = MusicPermissionState.Granted)
            loadSongs(showLoading = !wasAlreadyGranted)
        } else {
            loadSongsJob?.cancel()
            _uiState.value = _uiState.value.copy(
                permissionState = MusicPermissionState.Denied,
                isLoading = false,
                songs = emptyList(),
            )
        }
    }

    /**
     * Reads the songs from [songRepository]. Success keeps the repository order; an error
     * leaves an empty list (the screen then shows the "no songs" state).
     * @param showLoading True to show the loading spinner while reading.
     * @author Phong-Kaster
     */
    private fun loadSongs(showLoading: Boolean) {
        loadSongsJob?.cancel()
        loadSongsJob = viewModelScope.launch {
            if (showLoading) {
                _uiState.value = _uiState.value.copy(isLoading = true)
            }
            when (val outcome = songRepository.getSongs()) {
                is Outcome.Success -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, songs = outcome.data)
                }
                is Outcome.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, songs = emptyList())
                }
                is Outcome.Loading -> {
                    // getSongs() never returns Loading; nothing to do.
                }
            }
        }
    }
}
