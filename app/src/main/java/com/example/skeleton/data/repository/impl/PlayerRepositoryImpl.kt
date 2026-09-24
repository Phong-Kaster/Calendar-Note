package com.example.skeleton.data.repository.impl

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.Player
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.skeleton.data.mapper.toMediaItem
import com.example.skeleton.data.mapper.toPlaybackState
import com.example.skeleton.data.service.MusicPlaybackService
import com.example.skeleton.domain.model.PlaybackState
import com.example.skeleton.domain.model.Song
import com.example.skeleton.domain.repository.PlayerRepository
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Talks to [MusicPlaybackService] through a Media3 [MediaController] and mirrors the player
 * into [playbackState].
 *
 * It is shared by every screen (one Koin `single`), so connections are reference-counted:
 * each [connect] must be paired with one [release], and the controller is only closed when
 * the last user releases. When the connection is closed or dropped, [playbackState] goes back
 * to [PlaybackState.Idle].
 *
 * Connecting is asynchronous. A command sent while still connecting (or after the service
 * dropped the connection) is not lost: the latest one is remembered and run once a controller
 * is ready.
 * All calls are expected on the main thread (the controller lives there).
 *
 * Example: `PlayerRepositoryImpl(context = androidContext())`
 * @param context Used to reach the playback service; the application context is kept.
 * @author Phong-Kaster
 */
class PlayerRepositoryImpl(
    context: Context,
) : PlayerRepository {

    private val appContext: Context = context.applicationContext

    private val _playbackState = MutableStateFlow(PlaybackState.Idle)
    override val playbackState: StateFlow<PlaybackState> = _playbackState.asStateFlow()

    /** The connection in progress (or done). Null means "not connected, not connecting". */
    private var controllerFuture: ListenableFuture<MediaController>? = null

    /** The ready controller, or null while connecting / after the connection was closed or lost. */
    private var controller: MediaController? = null

    /** The last command sent before the controller was ready. */
    private var pendingCommand: ((MediaController) -> Unit)? = null

    /**
     * How many screens are using this shared connection right now ([connect] adds one,
     * [release] takes one away). The connection is only closed when it drops back to 0, so an
     * old screen that goes away cannot cut off a newer screen that is still showing.
     */
    private var clientCount: Int = 0

    /** Re-reads the player every time something about it changes. */
    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            refreshState(player = player)
        }
    }

    /**
     * Hears when the service drops the connection (for example the service was killed).
     * The dead controller is forgotten so the next command or [connect] builds a new one.
     */
    private val controllerListener = object : MediaController.Listener {
        override fun onDisconnected(controller: MediaController) {
            // A controller we already let go of (release / replaced) says goodbye late: ignore it.
            if (this@PlayerRepositoryImpl.controller !== controller) return
            Log.w(TAG, "MusicPlaybackService disconnected; will reconnect on next use")
            dropConnection()
        }
    }

    /**
     * Registers one more user of the player and makes sure a connection is on its way.
     * Must be paired with exactly one [release].
     * @author Phong-Kaster
     */
    override fun connect() {
        clientCount++
        ensureConnected()
    }

    /**
     * Starts connecting to the service unless a live connection exists or one is on its way.
     * A controller the service already dropped counts as "not connected" and is replaced.
     * @author Phong-Kaster
     */
    private fun ensureConnected() {
        val currentController = controller
        if (currentController != null && !currentController.isConnected) dropConnection()
        if (controllerFuture != null) return
        try {
            val sessionToken = SessionToken(
                appContext,
                ComponentName(appContext, MusicPlaybackService::class.java),
            )
            val future = MediaController.Builder(appContext, sessionToken)
                .setListener(controllerListener)
                .buildAsync()
            controllerFuture = future
            future.addListener(
                Runnable {
                    onControllerReady(future = future)
                },
                ContextCompat.getMainExecutor(appContext),
            )
        } catch (e: Exception) {
            Log.e(TAG, "ensureConnected() failed", e)
            controllerFuture = null
        }
    }

    /**
     * Called when the connection finishes: keeps the controller, starts listening and runs the
     * waiting command. Ignores a connection that was released in the meantime.
     * @param future The connection that just finished.
     * @author Phong-Kaster
     */
    private fun onControllerReady(future: ListenableFuture<MediaController>) {
        if (controllerFuture !== future) return
        try {
            val readyController = future.get()
            controller = readyController
            readyController.addListener(playerListener)
            refreshState(player = readyController)

            val command = pendingCommand
            pendingCommand = null
            if (command != null) runSafely(command = command, target = readyController)
        } catch (e: Exception) {
            Log.e(TAG, "Connecting to MusicPlaybackService failed", e)
            controllerFuture = null
            controller = null
            _playbackState.value = PlaybackState.Idle
        }
    }

    /**
     * One user is done with the player. Only when the last user leaves is the connection
     * closed and any waiting command forgotten. Extra calls (count already 0) do nothing.
     * Music keeps playing in the service either way.
     * @author Phong-Kaster
     */
    override fun release() {
        if (clientCount == 0) {
            Log.w(TAG, "release() called without a matching connect(); ignored")
            return
        }
        clientCount--
        if (clientCount > 0) return
        pendingCommand = null
        dropConnection()
    }

    /**
     * Forgets the current controller / connection attempt, closes it, and shows the idle state
     * so no stale song stays on screen. Does not touch [clientCount] or [pendingCommand].
     * @author Phong-Kaster
     */
    private fun dropConnection() {
        val oldController = controller
        val oldFuture = controllerFuture
        // Forget first, so a late onDisconnected for this controller is recognised as stale.
        controller = null
        controllerFuture = null
        _playbackState.value = PlaybackState.Idle
        try {
            oldController?.removeListener(playerListener)
            oldFuture?.let { future -> MediaController.releaseFuture(future) }
        } catch (e: Exception) {
            Log.e(TAG, "dropConnection() failed", e)
        }
    }

    /**
     * Loads [songs] as the queue and plays from [startIndex]. A bad index is ignored.
     * @author Phong-Kaster
     */
    override fun playQueue(songs: List<Song>, startIndex: Int) {
        if (songs.isEmpty()) return
        if (startIndex !in songs.indices) {
            Log.w(TAG, "playQueue() ignored: startIndex $startIndex out of ${songs.size}")
            return
        }
        val mediaItems = songs.map { song -> song.toMediaItem() }
        runOrQueue(
            command = { target ->
                target.setMediaItems(mediaItems, startIndex, 0L)
                target.prepare()
                target.play()
            },
        )
    }

    /**
     * Pauses when playing, otherwise resumes.
     * @author Phong-Kaster
     */
    override fun togglePlayPause() {
        runOrQueue(
            command = { target ->
                if (isPlayingForUi(player = target)) {
                    target.pause()
                } else {
                    resume(player = target)
                }
            },
        )
    }

    /**
     * Next song; repeat-all makes the last song wrap to the first.
     * @author Phong-Kaster
     */
    override fun next() {
        runOrQueue(
            command = { target ->
                target.seekToNextMediaItem()
            },
        )
    }

    /**
     * Previous song; repeat-all makes the first song wrap to the last.
     * @author Phong-Kaster
     */
    override fun previous() {
        // The service's ForwardingPlayer turns this into "previous song", never "restart song".
        runOrQueue(
            command = { target ->
                target.seekToPrevious()
            },
        )
    }

    /**
     * Runs [command] now when connected; otherwise (no controller yet, or the service dropped
     * it) remembers it, replacing an older waiting one, and makes sure a connection is on its
     * way. The command then runs as soon as the connection is ready.
     * @author Phong-Kaster
     */
    private fun runOrQueue(command: (MediaController) -> Unit) {
        val readyController = controller
        if (readyController == null || !readyController.isConnected) {
            pendingCommand = command
            ensureConnected()
            return
        }
        runSafely(command = command, target = readyController)
    }

    /**
     * Runs [command] on [target] and logs instead of throwing.
     * @author Phong-Kaster
     */
    private fun runSafely(command: (MediaController) -> Unit, target: MediaController) {
        try {
            command(target)
        } catch (e: Exception) {
            Log.e(TAG, "Player command failed", e)
        }
    }

    /**
     * Starts playing again. A stopped player is prepared first; a finished one goes back to
     * the start of its current song.
     * @author Phong-Kaster
     */
    private fun resume(player: Player) {
        if (player.playbackState == Player.STATE_IDLE) player.prepare()
        if (player.playbackState == Player.STATE_ENDED) player.seekToDefaultPosition()
        player.play()
    }

    /**
     * "Should the button show Pause?" — true when the player wants to play, even while it is
     * still buffering, so the icon does not flicker between songs.
     * @author Phong-Kaster
     */
    private fun isPlayingForUi(player: Player): Boolean {
        return player.playWhenReady &&
            player.playbackState != Player.STATE_IDLE &&
            player.playbackState != Player.STATE_ENDED &&
            player.playbackSuppressionReason == Player.PLAYBACK_SUPPRESSION_REASON_NONE
    }

    /**
     * Copies the player's current song and play flags into [playbackState].
     * @author Phong-Kaster
     */
    private fun refreshState(player: Player) {
        try {
            val hasQueue = player.mediaItemCount > 0
            val isPlaying = isPlayingForUi(player = player)
            val currentItem = player.currentMediaItem
            _playbackState.value = currentItem?.toPlaybackState(isPlaying = isPlaying, hasQueue = hasQueue)
                ?: PlaybackState.Idle.copy(isPlaying = isPlaying, hasQueue = hasQueue)
        } catch (e: Exception) {
            Log.e(TAG, "refreshState() failed", e)
        }
    }

    companion object {
        private const val TAG = "PlayerRepositoryImpl"
    }
}
