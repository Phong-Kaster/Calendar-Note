package com.example.skeleton.ui.fragment.nowplaying

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.model.NowPlaying
import com.example.skeleton.domain.repository.MusicPlayerRepository
import com.example.skeleton.ui.fragment.nowplaying.model.shouldCloseNowPlaying
import com.example.skeleton.ui.fragment.nowplaying.model.shouldTickPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
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

    private val _uiState = MutableStateFlow(NowPlayingUiState())
    val uiState: StateFlow<NowPlayingUiState> = _uiState.asStateFlow()

    /** Remembers whether this screen has ever seen a song, so a song going away closes it. */
    private var hasSeenSong = false

    /** True while the screen is at least STARTED (visible); the position only ticks then. */
    private val isScreenStarted = MutableStateFlow(false)

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
     * Copies every player change into the screen state, reads a fresh position (a skip, a pause
     * or a seek from the lock screen moves it too — [NowPlaying.positionChangeCount] makes sure
     * such a seek arrives here even while paused), and decides whether the screen must close.
     * Once "close" is decided it stays decided, so the screen goes back exactly once.
     *
     * @author Phong-Kaster
     */
    private fun collectNowPlaying() {
        viewModelScope.launch {
            musicPlayerRepository.state.collectLatest { nowPlaying ->
                val hasSong = nowPlaying.currentSong != null
                val shouldClose = shouldCloseNowPlaying(
                    hasSeenSong = hasSeenSong,
                    hasSong = hasSong,
                    isConnectAttemptFinished = nowPlaying.isConnectAttemptFinished,
                )
                if (hasSong) hasSeenSong = true

                _uiState.value = _uiState.value.copy(
                    nowPlaying = nowPlaying,
                    positionMs = musicPlayerRepository.currentPositionMs(),
                    isSongGone = _uiState.value.isSongGone || shouldClose,
                )
            }
        }
    }

    /**
     * While music is playing AND the screen is visible, asks the player for its position about
     * every half second. When music pauses, the song goes away, or the screen is hidden, the
     * loop stops; it starts again (reading the position at once) when all are true again.
     *
     * @author Phong-Kaster
     */
    private fun collectPositionTicks() {
        viewModelScope.launch {
            combine(
                musicPlayerRepository.state.map { nowPlaying -> isTicking(nowPlaying = nowPlaying) },
                isScreenStarted,
            ) { isMusicMoving, isVisible ->
                isMusicMoving && isVisible
            }
                .distinctUntilChanged()
                .collectLatest { shouldTick ->
                    if (!shouldTick) return@collectLatest
                    // Runs until collectLatest cancels it (paused or hidden); delay() is where it stops.
                    while (true) {
                        refreshPosition()
                        delay(POSITION_TICK_MS)
                    }
                }
        }
    }

    /**
     * The screen became visible (Fragment onStart): read the position right away (it may have
     * moved while hidden) and let the half-second refresh run again if music is playing.
     *
     * @author Phong-Kaster
     */
    fun onScreenStarted() {
        refreshPosition()
        isScreenStarted.value = true
    }

    /**
     * The screen is hidden (Fragment onStop): stop the half-second refresh so nothing ticks
     * off-screen.
     *
     * @author Phong-Kaster
     */
    fun onScreenStopped() {
        isScreenStarted.value = false
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
