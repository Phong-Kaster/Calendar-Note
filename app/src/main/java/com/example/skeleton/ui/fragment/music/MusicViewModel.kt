package com.example.skeleton.ui.fragment.music

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.model.Song
import com.example.skeleton.domain.repository.MusicPlayerRepository
import com.example.skeleton.domain.repository.SongRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Brain of the Music screen: remembers whether the audio permission is granted, loads the
 * phone's songs once it is, and forwards play / pause / skip taps to the music player.
 *
 * It holds one share of the player connection: [MusicPlayerRepository.connect] when it is
 * created and [MusicPlayerRepository.release] when it is cleared.
 *
 * Example:
 * ```kotlin
 * viewModel.onPermissionResult(granted = true) // starts loading songs
 * viewModel.onSongClick(song = song)           // plays the list from this song
 * ```
 *
 * @param songRepository Where the songs on the phone come from.
 * @param musicPlayerRepository The remote control of the background music player.
 * @author Phong-Kaster
 */
class MusicViewModel(
    private val songRepository: SongRepository,
    private val musicPlayerRepository: MusicPlayerRepository,
) : ViewModel() {

    private val TAG = "MusicViewModel"

    private val _uiState = MutableStateFlow(MusicUiState())
    val uiState: StateFlow<MusicUiState> = _uiState.asStateFlow()

    init {
        connectMusicPlayer()
        collectNowPlaying()
    }

    /**
     * Takes one share of the player connection. The matching release is in [onCleared].
     *
     * @author Phong-Kaster
     */
    private fun connectMusicPlayer() {
        musicPlayerRepository.connect()
    }

    /**
     * Copies every player change (song, play/pause, position in queue) into the screen state.
     *
     * @author Phong-Kaster
     */
    private fun collectNowPlaying() {
        viewModelScope.launch {
            musicPlayerRepository.state.collectLatest { nowPlaying ->
                _uiState.value = _uiState.value.copy(nowPlaying = nowPlaying)
            }
        }
    }

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
     * The user tapped a song: play the whole list in order, starting from that song.
     *
     * @param song The tapped song.
     * @author Phong-Kaster
     */
    fun onSongClick(song: Song) {
        val songs = _uiState.value.songs
        val startIndex = songs.indexOfFirst { item -> item.id == song.id }
        if (startIndex < 0) return
        musicPlayerRepository.playSongs(songs = songs, startIndex = startIndex)
    }

    /**
     * The user tapped play / pause in the now-playing bar.
     *
     * @author Phong-Kaster
     */
    fun onPlayPauseClick() {
        musicPlayerRepository.togglePlayPause()
    }

    /**
     * The user tapped next in the now-playing bar.
     *
     * @author Phong-Kaster
     */
    fun onNextClick() {
        musicPlayerRepository.skipToNext()
    }

    /**
     * The user tapped previous in the now-playing bar.
     *
     * @author Phong-Kaster
     */
    fun onPreviousClick() {
        musicPlayerRepository.skipToPrevious()
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

    /**
     * Gives back this screen's share of the player connection. The music keeps playing
     * in the service; other screens holding the connection are not affected.
     *
     * @author Phong-Kaster
     */
    override fun onCleared() {
        musicPlayerRepository.release()
        super.onCleared()
    }
}
