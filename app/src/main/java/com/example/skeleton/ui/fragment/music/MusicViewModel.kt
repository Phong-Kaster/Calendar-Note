package com.example.skeleton.ui.fragment.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.common.Outcome
import com.example.skeleton.domain.model.Song
import com.example.skeleton.domain.repository.PlayerRepository
import com.example.skeleton.domain.repository.SongRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Drives the Music screen: remembers whether we may read music, loads the device's songs and
 * forwards play / pause / next / previous to the player.
 *
 * The Fragment checks the permission (on every resume and after the system dialog) and
 * reports it with [onPermissionResult]. This class never touches Android APIs, so it can be
 * unit-tested on the JVM.
 *
 * Example: `viewModel.onPermissionResult(granted = true)` → loading → song list or empty state;
 * `viewModel.onSongClick(song)` → the list plays from that song.
 * @param songRepository Where the songs come from.
 * @param playerRepository Remote control of the background music player.
 * @author Phong-Kaster
 */
class MusicViewModel(
    private val songRepository: SongRepository,
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val TAG = "MusicViewModel"

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    /** The running song load, so a newer load can replace an older one. */
    private var loadSongsJob: Job? = null

    init {
        connectPlayer()
        collectPlaybackState()
    }

    /**
     * Connects to the background player so commands and state updates can flow.
     * @author Phong-Kaster
     */
    private fun connectPlayer() {
        playerRepository.connect()
    }

    /**
     * Mirrors every player update into [uiState].
     * @author Phong-Kaster
     */
    private fun collectPlaybackState() {
        viewModelScope.launch {
            playerRepository.playbackState.collectLatest { playback ->
                _uiState.value = _uiState.value.copy(playback = playback)
            }
        }
    }

    /**
     * Plays the whole shown list, starting with the tapped [song].
     * A song that is not in the list (stale tap) is ignored.
     * @param song The tapped song.
     * @author Phong-Kaster
     */
    fun onSongClick(song: Song) {
        val songs = _uiState.value.songs
        val index = songs.indexOf(song)
        if (index < 0) return
        playerRepository.playQueue(songs = songs, startIndex = index)
    }

    /**
     * Play when paused, pause when playing.
     * @author Phong-Kaster
     */
    fun onTogglePlayPause() {
        playerRepository.togglePlayPause()
    }

    /**
     * Skip to the next song.
     * @author Phong-Kaster
     */
    fun onNext() {
        playerRepository.next()
    }

    /**
     * Go back to the previous song.
     * @author Phong-Kaster
     */
    fun onPrevious() {
        playerRepository.previous()
    }

    /**
     * Disconnects from the player when the screen is gone for good. Music keeps playing.
     * @author Phong-Kaster
     */
    override fun onCleared() {
        playerRepository.release()
        super.onCleared()
    }

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
