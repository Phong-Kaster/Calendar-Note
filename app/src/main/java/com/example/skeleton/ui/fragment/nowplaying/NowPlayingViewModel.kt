package com.example.skeleton.ui.fragment.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.repository.PlayerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Brain of the Now Playing screen. It copies the player's picture from [PlayerRepository.state]
 * into [uiState] and forwards every button tap to the player.
 * Player commands are plain calls on purpose (no `Dispatchers.IO`): the repository moves them
 * to the main thread by itself. Because the notification drives the same player, the screen
 * and the notification always agree.
 *
 * Example (from the Fragment):
 * ```kotlin
 * viewModel.togglePlayPause()
 * viewModel.seekTo(fraction = 0.5f) // jump to the middle of the song
 * ```
 *
 * @param playerRepository the remote control of the music player.
 * @author Phong-Kaster
 */
class NowPlayingViewModel(
    private val playerRepository: PlayerRepository,
) : ViewModel() {

    private val TAG = "NowPlayingViewModel"

    // Start from the player's current picture so the screen does not flash "nothing is playing".
    private val _uiState = MutableStateFlow(NowPlayingUiState(playback = playerRepository.state.value))
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    init {
        collectPlayback()
    }

    /**
     * Keeps [uiState] in step with the player (song, position, modes).
     *
     * @author Phong-Kaster
     */
    private fun collectPlayback() {
        viewModelScope.launch {
            playerRepository.state.collectLatest { playback ->
                _uiState.value = _uiState.value.copy(playback = playback)
            }
        }
    }

    /**
     * Plays when paused, pauses when playing.
     *
     * @author Phong-Kaster
     */
    fun togglePlayPause() {
        playerRepository.togglePlayPause()
    }

    /**
     * Goes to the next song.
     *
     * @author Phong-Kaster
     */
    fun skipToNext() {
        playerRepository.skipToNext()
    }

    /**
     * Goes to the previous song (or back to the start of this one).
     *
     * @author Phong-Kaster
     */
    fun skipToPrevious() {
        playerRepository.skipToPrevious()
    }

    /**
     * Jumps to the spot where the user let go of the seek bar knob.
     *
     * Example:
     * ```kotlin
     * viewModel.seekTo(fraction = 0.25f) // a quarter of the way into the song
     * ```
     *
     * @param fraction knob position from 0f (start) to 1f (end).
     * @author Phong-Kaster
     */
    fun seekTo(fraction: Float) {
        val currentState = _uiState.value
        if (currentState.canSeek.not()) return
        playerRepository.seekTo(positionMs = currentState.positionForFraction(fraction = fraction))
    }

    /**
     * Turns shuffle on when it is off, and off when it is on.
     *
     * @author Phong-Kaster
     */
    fun toggleShuffle() {
        playerRepository.setShuffleEnabled(enabled = _uiState.value.shuffleEnabled.not())
    }

    /**
     * Moves repeat to its next mode: Off -> All -> One -> Off.
     *
     * @author Phong-Kaster
     */
    fun cycleRepeatMode() {
        playerRepository.setRepeatMode(mode = _uiState.value.repeatMode.next())
    }
}
