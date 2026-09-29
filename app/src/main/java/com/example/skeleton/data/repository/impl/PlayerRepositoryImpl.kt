package com.example.skeleton.data.repository.impl

import android.content.ComponentName
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.skeleton.data.mapper.toMediaItem
import com.example.skeleton.data.mapper.toPlaybackState
import com.example.skeleton.data.mapper.toPlayerRepeatMode
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.RepeatMode
import com.example.skeleton.domain.model.Song
import com.example.skeleton.domain.repository.PlayerRepository
import com.example.skeleton.service.PlaybackService
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Talks to [PlaybackService] through a Media3 [MediaController] (a remote control).
 * The queue itself lives in the service's player as `MediaItem`s; this class only sends
 * commands and publishes what the player reports into [state].
 *
 * Threading, explained simply: a `MediaController` may only be touched on the main thread.
 * So every public command is posted to [mainHandler], and every private field below is only
 * read or written on the main thread. Callers may call the commands from anywhere.
 *
 * If a command arrives before the controller is connected, it waits in [pendingCommands]
 * and runs as soon as the connection is ready. If connecting fails, waiting commands are
 * dropped (logged) instead of crashing. If the service goes away, the next command reconnects.
 *
 * Example:
 * ```kotlin
 * val repository: PlayerRepository = PlayerRepositoryImpl(context = androidContext())
 * repository.playQueue(songs = songs, startIndex = 0)
 * ```
 *
 * @param context any context; only the application context is kept.
 * @param mainHandler where commands run; must be on the main looper.
 * @author Phong-Kaster
 */
class PlayerRepositoryImpl(
    context: Context,
    private val mainHandler: Handler = Handler(Looper.getMainLooper()),
) : PlayerRepository {

    private val appContext: Context = context.applicationContext

    private val _state = MutableStateFlow(PlaybackState())
    override val state: StateFlow<PlaybackState> = _state.asStateFlow()

    /** The connected remote control, or null while (re)connecting. Main thread only. */
    private var controller: MediaController? = null

    /** The connection that is in progress, or null when none is. Main thread only. */
    private var controllerFuture: ListenableFuture<MediaController>? = null

    /** Commands that came in before the controller was ready. Main thread only. */
    private val pendingCommands = mutableListOf<(MediaController) -> Unit>()

    /** Any change in the player (song, play/pause, queue, modes) -> publish a new picture. */
    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            publishState()
        }
    }

    /** Told when the service goes away, so we can reconnect on the next command. */
    private val controllerListener = object : MediaController.Listener {
        override fun onDisconnected(controller: MediaController) {
            onControllerDisconnected()
        }
    }

    /** Re-publishes the state so the position moves forward; only scheduled while playing. */
    private val positionTicker = Runnable {
        publishState()
    }

    init {
        mainHandler.post {
            connectIfNeeded()
        }
    }

    // ---------- Commands ----------

    override fun playQueue(songs: List<Song>, startIndex: Int) {
        if (songs.isEmpty()) return
        val mediaItems = songs.map { song -> song.toMediaItem() }
        val safeStartIndex = startIndex.coerceIn(0, songs.lastIndex)
        runOnController(
            command = { player ->
                player.setMediaItems(mediaItems, safeStartIndex, 0L)
                player.prepare()
                player.play()
            }
        )
    }

    override fun togglePlayPause() {
        runOnController(
            command = { player ->
                if (isPlayRequested(player = player)) {
                    player.pause()
                } else {
                    resume(player = player)
                }
            }
        )
    }

    override fun skipToNext() {
        runOnController(
            command = { player ->
                player.seekToNext()
            }
        )
    }

    override fun skipToPrevious() {
        runOnController(
            command = { player ->
                player.seekToPrevious()
            }
        )
    }

    override fun seekTo(positionMs: Long) {
        val safePositionMs = positionMs.coerceAtLeast(0L)
        runOnController(
            command = { player ->
                player.seekTo(safePositionMs)
                publishState()
            }
        )
    }

    override fun setShuffleEnabled(enabled: Boolean) {
        runOnController(
            command = { player ->
                player.shuffleModeEnabled = enabled
            }
        )
    }

    override fun setRepeatMode(mode: RepeatMode) {
        runOnController(
            command = { player ->
                player.repeatMode = mode.toPlayerRepeatMode()
            }
        )
    }

    // ---------- Play / pause helpers ----------

    /**
     * True when the player is playing or trying to (for example while buffering),
     * so the button should pause. A finished or empty player is not "playing".
     *
     * @author Phong-Kaster
     */
    private fun isPlayRequested(player: MediaController): Boolean {
        val isStopped = player.playbackState == Player.STATE_ENDED || player.playbackState == Player.STATE_IDLE
        return player.playWhenReady && isStopped.not()
    }

    /**
     * Starts playing again. A stopped player is prepared first; a finished one starts the
     * current song from the beginning.
     *
     * @author Phong-Kaster
     */
    private fun resume(player: MediaController) {
        if (player.mediaItemCount == 0) return
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        if (player.playbackState == Player.STATE_ENDED) player.seekToDefaultPosition()
        player.play()
    }

    // ---------- Connection ----------

    /**
     * Runs [command] on the main thread with the connected controller, or keeps it for later
     * when we are not connected yet (and starts connecting).
     *
     * @author Phong-Kaster
     */
    private fun runOnController(command: (MediaController) -> Unit) {
        mainHandler.post {
            val readyController = controller
            if (readyController == null) {
                pendingCommands.add(command)
                connectIfNeeded()
                return@post
            }
            runSafely(player = readyController, command = command)
        }
    }

    /**
     * Starts connecting to [PlaybackService] unless we are connected or already connecting.
     * Main thread only.
     *
     * @author Phong-Kaster
     */
    private fun connectIfNeeded() {
        if (controller != null || controllerFuture != null) return
        try {
            val token = SessionToken(appContext, ComponentName(appContext, PlaybackService::class.java))
            val future = MediaController.Builder(appContext, token)
                .setListener(controllerListener)
                .buildAsync()
            controllerFuture = future
            future.addListener(
                Runnable {
                    onControllerFutureDone(future = future)
                },
                ContextCompat.getMainExecutor(appContext),
            )
        } catch (e: Exception) {
            Log.e(TAG, "connectIfNeeded failed", e)
            controllerFuture = null
            pendingCommands.clear()
        }
    }

    /**
     * Called on the main thread when connecting finished (well or badly).
     * On success: listen to the player, run waiting commands, publish the first picture.
     * On failure: drop waiting commands (fail-soft).
     *
     * @author Phong-Kaster
     */
    private fun onControllerFutureDone(future: ListenableFuture<MediaController>) {
        controllerFuture = null
        val connectedController = try {
            future.get()
        } catch (e: Exception) {
            Log.e(TAG, "MediaController connection failed", e)
            pendingCommands.clear()
            return
        }

        connectedController.addListener(playerListener)
        controller = connectedController

        val commandsToRun = pendingCommands.toList()
        pendingCommands.clear()
        commandsToRun.forEach { command ->
            runSafely(player = connectedController, command = command)
        }
        publishState()
    }

    /**
     * The service went away (for example it was stopped after the app was swiped away).
     * Forget the old controller; the next command connects again.
     *
     * @author Phong-Kaster
     */
    private fun onControllerDisconnected() {
        controller?.removeListener(playerListener)
        controller = null
        mainHandler.removeCallbacks(positionTicker)
        _state.value = PlaybackState()
    }

    /**
     * Runs one command and logs (instead of crashing) if the player refuses it.
     *
     * @author Phong-Kaster
     */
    private fun runSafely(player: MediaController, command: (MediaController) -> Unit) {
        try {
            command(player)
        } catch (e: Exception) {
            Log.e(TAG, "player command failed", e)
        }
    }

    // ---------- State ----------

    /**
     * Copies what the player is doing into [state], and keeps the position ticker running
     * only while music is playing (no wasted work while paused). Main thread only.
     *
     * @author Phong-Kaster
     */
    private fun publishState() {
        val activeController = controller ?: return
        val newState = activeController.toPlaybackState()
        _state.value = newState

        mainHandler.removeCallbacks(positionTicker)
        if (newState.isPlaying) mainHandler.postDelayed(positionTicker, POSITION_TICK_MS)
    }

    companion object {
        private const val TAG = "PlayerRepositoryImpl"

        /** How often the position is refreshed while playing. */
        private const val POSITION_TICK_MS = 500L
    }
}
