package com.example.skeleton.data.repository.impl

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.annotation.OptIn
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.example.skeleton.data.mapper.toMediaItem
import com.example.skeleton.data.mapper.toSongOrNull
import com.example.skeleton.domain.model.NowPlaying
import com.example.skeleton.domain.model.Song
import com.example.skeleton.domain.repository.MusicPlayerRepository
import com.example.skeleton.service.MusicPlaybackService
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Talks to [MusicPlaybackService] through a Media3 [MediaController] and turns what the player
 * does into a [NowPlaying] stream.
 *
 * Simple story:
 * - Every screen calls [connect] once and [release] once. We count them; the controller is
 *   closed only when the count drops back to 0, so one screen closing never cuts off another.
 * - Building the controller takes a moment. Commands sent before it is ready wait in a small
 *   queue and run as soon as it connects.
 * - If the service drops the connection, we forget the controller and build a new one the
 *   next time it is needed.
 *
 * Threading: a MediaController may only be used on the main thread, so every function here must
 * be called on the main thread, and the controller callbacks run on the main executor. That is
 * why there is no `ioDispatcher` here (an explicit exception to the repository dispatcher rule).
 *
 * @param context Application context, used to reach the service.
 * @author Phong-Kaster
 */
@OptIn(UnstableApi::class)
class MusicPlayerRepositoryImpl(private val context: Context) : MusicPlayerRepository {

    private val _state = MutableStateFlow(NowPlaying())
    override val state: StateFlow<NowPlaying> = _state.asStateFlow()

    /** How many screens are holding the connection right now. */
    private var connectionCount = 0

    /** The controller while it is being built (and after, until it is released). */
    private var controllerFuture: ListenableFuture<MediaController>? = null

    /** The ready-to-use controller; null until the connection finishes. */
    private var controller: MediaController? = null

    /** Commands that arrived before the controller was ready; they run once it connects. */
    private val pendingCommands = mutableListOf<PendingCommand>()

    /** The songs of the last [playSongs] call, used to map a MediaItem back to its full Song. */
    private var queuedSongs: List<Song> = emptyList()

    /** Copies every player change into [state]. */
    private val playerListener = object : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            updateState(player = player)
        }
    }

    /**
     * Forgets the controller when the service goes away, so the next use rebuilds it.
     * A late "disconnected" from an OLD controller (one we already released) is ignored, so it
     * can never tear down the new connection another screen just opened.
     */
    private val controllerListener = object : MediaController.Listener {
        override fun onDisconnected(controller: MediaController) {
            if (controller !== this@MusicPlayerRepositoryImpl.controller) return
            Log.w(TAG, "Controller disconnected; it will be rebuilt on next use")
            dropController()
            // The old song and queue are no longer true; the next connection publishes fresh ones.
            _state.value = NowPlaying()
        }
    }

    override fun connect() {
        connectionCount++
        ensureController()
    }

    override fun release() {
        if (connectionCount == 0) {
            Log.w(TAG, "release() called without a matching connect()")
            return
        }
        connectionCount--
        if (connectionCount > 0) return
        pendingCommands.clear()
        dropController()
    }

    override fun playSongs(songs: List<Song>, startIndex: Int) {
        if (songs.isEmpty()) return
        if (startIndex !in songs.indices) {
            Log.w(TAG, "playSongs: startIndex $startIndex is outside 0..${songs.lastIndex}")
            return
        }
        queuedSongs = songs
        val mediaItems = songs.map { song -> song.toMediaItem() }

        runOnController(
            commandName = "playSongs",
            command = { readyController ->
                readyController.setMediaItems(mediaItems, startIndex, 0L)
                readyController.prepare()
                readyController.play()
            },
        )
    }

    override fun togglePlayPause() {
        runOnController(
            commandName = "togglePlayPause",
            command = { readyController ->
                // Media3's own button rule: it also knows "loading" and "held back by a phone call"
                // mean the player still wants to play, and it prepares an idle player first.
                Util.handlePlayPauseButtonAction(readyController)
            },
        )
    }

    override fun skipToNext() {
        runOnController(
            commandName = "skipToNext",
            command = { readyController -> readyController.seekToNext() },
        )
    }

    override fun skipToPrevious() {
        runOnController(
            commandName = "skipToPrevious",
            command = { readyController -> readyController.seekToPrevious() },
        )
    }

    // ---------- Controller lifecycle ----------

    /**
     * Makes sure a controller exists or is being built. A controller that lost its connection
     * is thrown away first.
     *
     * @author Phong-Kaster
     */
    private fun ensureController() {
        val existing = controller
        if (existing != null && existing.isConnected) return
        if (existing != null) dropController()
        if (controllerFuture != null) return
        buildController()
    }

    /**
     * Starts building a controller in the background of Media3 and listens for the result on the
     * main thread. It never blocks the main thread.
     *
     * @author Phong-Kaster
     */
    private fun buildController() {
        try {
            val token = SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))
            val future = MediaController.Builder(context, token)
                .setListener(controllerListener)
                .buildAsync()
            controllerFuture = future
            future.addListener(
                { onControllerFutureDone(future = future) },
                ContextCompat.getMainExecutor(context),
            )
        } catch (e: Exception) {
            Log.e(TAG, "buildController failed", e)
            controllerFuture = null
            pendingCommands.clear()
        }
    }

    /**
     * Runs when the controller finished building. `future.get()` is safe here because the future
     * is already complete. Stale futures (released in the meantime) are ignored.
     *
     * @author Phong-Kaster
     */
    private fun onControllerFutureDone(future: ListenableFuture<MediaController>) {
        if (controllerFuture !== future) return

        val builtController = try {
            future.get()
        } catch (e: Exception) {
            Log.e(TAG, "Controller connection failed", e)
            controllerFuture = null
            pendingCommands.clear()
            return
        }

        controller = builtController
        builtController.addListener(playerListener)
        updateState(player = builtController)
        runPendingCommands(readyController = builtController)
    }

    /**
     * Forgets the current controller and tells Media3 to release it.
     *
     * @author Phong-Kaster
     */
    private fun dropController() {
        controller?.removeListener(playerListener)
        controllerFuture?.let { future -> MediaController.releaseFuture(future) }
        controller = null
        controllerFuture = null
    }

    // ---------- Commands ----------

    /**
     * Runs [command] now when the controller is ready, otherwise keeps it until it connects.
     * Errors are logged and never thrown to the caller.
     *
     * @author Phong-Kaster
     */
    private fun runOnController(commandName: String, command: (MediaController) -> Unit) {
        if (connectionCount == 0) {
            Log.w(TAG, "$commandName ignored: connect() was not called")
            return
        }
        val readyController = controller
        if (readyController != null && readyController.isConnected) {
            safeRun(pendingCommand = PendingCommand(name = commandName, action = command), readyController = readyController)
            return
        }
        pendingCommands.add(PendingCommand(name = commandName, action = command))
        ensureController()
    }

    /**
     * Runs every waiting command in the order it arrived, then empties the queue.
     *
     * @author Phong-Kaster
     */
    private fun runPendingCommands(readyController: MediaController) {
        val commands = pendingCommands.toList()
        pendingCommands.clear()
        commands.forEach { pendingCommand ->
            safeRun(pendingCommand = pendingCommand, readyController = readyController)
        }
    }

    /**
     * Runs one command and logs (never throws) when it fails.
     *
     * @author Phong-Kaster
     */
    private fun safeRun(pendingCommand: PendingCommand, readyController: MediaController) {
        try {
            pendingCommand.action(readyController)
        } catch (e: Exception) {
            Log.e(TAG, "${pendingCommand.name} failed", e)
        }
    }

    // ---------- State ----------

    /**
     * Reads the player and publishes a fresh [NowPlaying].
     *
     * @author Phong-Kaster
     */
    private fun updateState(player: Player) {
        val currentItem = player.currentMediaItem
        _state.value = NowPlaying(
            currentSong = currentItem?.let { item -> findSong(item = item) },
            // "Wants to play" (not only "sound right now"), so the bar shows Pause while loading.
            isPlaying = !Util.shouldShowPlayButton(player),
            currentIndex = if (currentItem == null) NO_INDEX else player.currentMediaItemIndex,
            queueSize = player.mediaItemCount,
        )
    }

    /**
     * Finds the full [Song] for a [MediaItem]: first in the songs we queued, else rebuilt from the
     * item itself (for example after the app restarted while the service kept playing).
     *
     * @author Phong-Kaster
     */
    private fun findSong(item: MediaItem): Song? {
        val queued = queuedSongs.firstOrNull { song -> song.id.toString() == item.mediaId }
        return queued ?: item.toSongOrNull()
    }

    /**
     * A command waiting for the controller.
     *
     * @param name Short name used in logs.
     * @param action What to do with the ready controller.
     * @author Phong-Kaster
     */
    private class PendingCommand(
        val name: String,
        val action: (MediaController) -> Unit,
    )

    companion object {
        private const val TAG = "MusicPlayerRepositoryImpl"
        private const val NO_INDEX = -1
    }
}
