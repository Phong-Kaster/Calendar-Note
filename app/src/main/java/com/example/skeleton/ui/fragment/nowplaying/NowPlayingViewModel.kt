package com.example.skeleton.ui.fragment.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.model.NowPlaying
import com.example.skeleton.domain.repository.MusicPlayerRepository
import com.example.skeleton.ui.fragment.nowplaying.model.shouldTickPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * Brain of the Now Playing screen: watches the player, keeps the song position fresh while
 * music is playing, and forwards play / pause / skip / seek taps to the player.
 *
 * It holds its own share of the player connection: [MusicPlayerRepository.connect] when it is
 * created and [MusicPlayerRepository.release] in [onCleared], so closing this screen never cuts
 * off the Music screen (and the other way round).
 *
 * All work runs on viewModelScope (main thread), which is where the player must be used.
 *
 * Example:
 * ```kotlin
 * viewModel.onPlayPauseClick()
 * viewModel.onSeek(positionMs = 30_000L) // jump to 0:30
 * ```
 *
 * @param musicPlayerRepository The remote control of the background music player.
 * @author Phong-Kaster
 */
class NowPlayingViewModel(
    private val musicPlayerRepository: MusicPlayerRepository,
) : ViewModel() {

    private val TAG = "NowPlayingViewModel"

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    /** Remembers whether this screen has ever seen a song, so "no song" at start does not close it. */
    private var hasSeenSong = false

    init {
        connectMusicPlayer()
        collectNowPlaying()
        collectPositionTicks()
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
     * Copies every player change into the screen state, reads a fresh position (a skip or a
     * pause moves it too), and marks the song as gone when the player lost it.
     *
     * @author Phong-Kaster
     */
    private fun collectNowPlaying() {
        viewModelScope.launch {
            musicPlayerRepository.state.collectLatest { nowPlaying ->
                val hasSong = nowPlaying.currentSong != null
                val isSongGone = hasSeenSong && !hasSong
                if (hasSong) hasSeenSong = true

                _uiState.value = _uiState.value.copy(
                    nowPlaying = nowPlaying,
                    positionMs = musicPlayerRepository.currentPositionMs(),
                    isSongGone = isSongGone,
                )
            }
        }
    }

    /**
     * While music is playing, asks the player for its position about every half second.
     * When it pauses (or the song goes away) the loop stops; it starts again on the next play.
     *
     * @author Phong-Kaster
     */
    private fun collectPositionTicks() {
        viewModelScope.launch {
            musicPlayerRepository.state
                .map { nowPlaying -> isTicking(nowPlaying = nowPlaying) }
                .distinctUntilChanged()
                .collectLatest { shouldTick ->
                    if (!shouldTick) return@collectLatest
                    // Runs until collectLatest cancels it (music paused); delay() is where it stops.
                    while (true) {
                        refreshPosition()
                        delay(POSITION_TICK_MS)
                    }
                }
        }
    }

    /**
     * Answers whether the position must be refreshed for this player snapshot.
     *
     * @author Phong-Kaster
     */
    private fun isTicking(nowPlaying: NowPlaying): Boolean = shouldTickPosition(
        isPlaying = nowPlaying.isPlaying,
        hasSong = nowPlaying.currentSong != null,
    )

    /**
     * Reads the newest position from the player into the state.
     *
     * @author Phong-Kaster
     */
    private fun refreshPosition() {
        _uiState.value = _uiState.value.copy(positionMs = musicPlayerRepository.currentPositionMs())
    }

    /**
     * The user tapped play / pause.
     *
     * @author Phong-Kaster
     */
    fun onPlayPauseClick() {
        musicPlayerRepository.togglePlayPause()
    }

    /**
     * The user tapped next.
     *
     * @author Phong-Kaster
     */
    fun onNextClick() {
        musicPlayerRepository.skipToNext()
    }

    /**
     * The user tapped previous.
     *
     * @author Phong-Kaster
     */
    fun onPreviousClick() {
        musicPlayerRepository.skipToPrevious()
    }

    /**
     * The user let go of the slider: jump there. The position is shown right away so the
     * thumb does not spring back while the player catches up.
     *
     * @param positionMs Where to jump, in milliseconds.
     * @author Phong-Kaster
     */
    fun onSeek(positionMs: Long) {
        musicPlayerRepository.seekTo(positionMs = positionMs)
        _uiState.value = _uiState.value.copy(positionMs = positionMs)
    }

    /**
     * Gives back this screen's share of the player connection. The music keeps playing.
     *
     * @author Phong-Kaster
     */
    override fun onCleared() {
        musicPlayerRepository.release()
        super.onCleared()
    }

    companion object {
        /** How often the position is refreshed while playing, in milliseconds. */
        private const val POSITION_TICK_MS = 500L
    }
}
