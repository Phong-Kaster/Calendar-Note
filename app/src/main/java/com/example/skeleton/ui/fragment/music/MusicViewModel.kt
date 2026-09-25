package com.example.skeleton.ui.fragment.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Brain of the Music screen: remembers whether the audio permission is granted and
 * loads the phone's songs once it is.
 *
 * Example:
 * ```kotlin
 * viewModel.onPermissionResult(granted = true) // starts loading songs
 * ```
 *
 * @param songRepository Where the songs on the phone come from.
 * @author Phong-Kaster
 */
class MusicViewModel(
    private val songRepository: SongRepository,
) : ViewModel() {

    private val TAG = "MusicViewModel"

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    /**
     * Called by the Fragment every time it knows the latest permission answer
     * (on resume, or after the system dialog closes).
     *
     * Songs load only when the permission turns from "no" to "yes", so coming back
     * to the screen does not reload the list again and again.
     *
     * @param granted True when the audio permission is granted.
     * @author Phong-Kaster
     */
    fun onPermissionResult(granted: Boolean) {
        val wasGranted = _uiState.value.isPermissionGranted
        _uiState.value = _uiState.value.copy(isPermissionGranted = granted)

        if (!granted) return
        if (wasGranted) return
        loadSongs()
    }

    /**
     * Reads the songs from the phone and puts them in the state.
     * `isLoading` is always turned off at the end, even if something fails.
     *
     * @author Phong-Kaster
     */
    private fun loadSongs() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val songs = songRepository.getDeviceSongs()
                _uiState.value = _uiState.value.copy(songs = songs)
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}
