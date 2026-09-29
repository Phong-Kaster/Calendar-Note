package com.example.skeleton.ui.fragment.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.repository.MusicRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Brain of the Library screen. It remembers whether we may read music, and when we may,
 * it asks [MusicRepository] for the songs and puts them into [uiState].
 *
 * Example (from the Fragment):
 * ```kotlin
 * viewModel.onPermissionChecked(isGranted = PermissionUtil.isAudioPermissionGranted(requireContext()))
 * ```
 *
 * @param musicRepository where the songs come from.
 * @author Phong-Kaster
 */
class LibraryViewModel(
    private val musicRepository: MusicRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LibraryUiState())
    val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

    /** The song-reading job that is running now, so a newer reload can replace it. */
    private var loadSongsJob: Job? = null

    /**
     * Called when the screen checks the permission by itself (first open, or coming back
     * from Settings). If allowed, songs are (re)loaded.
     *
     * @param isGranted true when the audio permission is granted right now.
     * @author Phong-Kaster
     */
    fun onPermissionChecked(isGranted: Boolean) {
        val permanentlyDenied = if (isGranted) false else _uiState.value.isPermissionPermanentlyDenied
        _uiState.value = _uiState.value.copy(
            hasPermission = isGranted,
            isPermissionChecked = true,
            isPermissionPermanentlyDenied = permanentlyDenied,
        )
        if (isGranted) loadSongs()
    }

    /**
     * Called with the answer of the system permission dialog.
     * "Permanently denied" is decided HERE from the real answer: denied AND the system
     * says it will not show the reason again.
     *
     * @param isGranted true if the user tapped Allow.
     * @param shouldShowRationale the system's `shouldShowRequestPermissionRationale` right after the answer.
     * @author Phong-Kaster
     */
    fun onPermissionResult(isGranted: Boolean, shouldShowRationale: Boolean) {
        val permanentlyDenied = !isGranted && !shouldShowRationale
        _uiState.value = _uiState.value.copy(
            hasPermission = isGranted,
            isPermissionChecked = true,
            isPermissionPermanentlyDenied = permanentlyDenied,
        )
        if (isGranted) loadSongs()
    }

    /**
     * Reads songs from the repository. The spinner only shows when the list is still empty,
     * so coming back to the screen does not blink the whole list.
     *
     * @author Phong-Kaster
     */
    private fun loadSongs() {
        loadSongsJob?.cancel()
        val thisJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = _uiState.value.songs.isEmpty())
            try {
                val songs = musicRepository.getSongs()
                _uiState.value = _uiState.value.copy(songs = songs)
            } finally {
                // A newer load may have replaced this one; only the newest may hide the spinner.
                if (loadSongsJob == coroutineContext[Job]) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
        loadSongsJob = thisJob
    }
}
