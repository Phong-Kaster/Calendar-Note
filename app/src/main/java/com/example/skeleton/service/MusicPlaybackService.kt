package com.example.skeleton.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.skeleton.MainActivity
import com.example.skeleton.data.mapper.withPlayableUri
import com.example.skeleton.domain.model.PlaybackQueuePolicy
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * The background music service. It owns the real ExoPlayer and a MediaSession, so music keeps
 * playing when the user leaves the screen, and Media3 shows its default media notification
 * (title, artist, previous / play-pause / next) while music plays.
 *
 * The player is set up for music: music audio attributes, it pauses for phone calls and other
 * apps (audio focus), it pauses when headphones are unplugged, and the queue repeats forever.
 *
 * Both the in-app bar and the notification talk to [QueuePolicyPlayer], which uses
 * [PlaybackQueuePolicy] for next / previous.
 *
 * Tapping the notification opens the app on the Music tab. Swiping the app away from Recents
 * keeps music going while it plays, but stops the service when it is paused
 * (see [PlaybackStopPolicy]).
 *
 * @author Phong-Kaster
 */
@OptIn(UnstableApi::class)
class MusicPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    /**
     * Builds the player and the session once, when Android creates the service.
     *
     * @author Phong-Kaster
     */
    override fun onCreate() {
        super.onCreate()
        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
                    .setUsage(C.USAGE_MEDIA)
                    .build(),
                /* handleAudioFocus = */ true,
            )
            .setHandleAudioBecomingNoisy(true)
            .build()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL

        mediaSession = MediaSession.Builder(this, QueuePolicyPlayer(player = exoPlayer))
            .setCallback(PlayableItemsCallback())
            .setSessionActivity(buildOpenMusicPendingIntent())
            .build()
    }

    /**
     * Builds the "ticket" Android uses when the user taps the media notification: it opens
     * [MainActivity] (reusing the running one when possible) and asks it to show the Music tab.
     *
     * @author Phong-Kaster
     */
    private fun buildOpenMusicPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_OPEN_MUSIC, true)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        return PendingIntent.getActivity(
            this,
            OPEN_MUSIC_REQUEST_CODE,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    /**
     * Called when the user swipes the app away from Recents. If music is paused (or there is
     * nothing to play) we stop the service so the notification disappears; if music is playing
     * we keep it going. [PlaybackStopPolicy] makes the decision.
     *
     * @author Phong-Kaster
     */
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        val shouldStop = PlaybackStopPolicy.shouldStopOnTaskRemoved(
            isPlaybackOngoing = isPlaybackOngoing(player = player),
            mediaItemCount = player?.mediaItemCount ?: 0,
        )
        if (!shouldStop) return
        // Never bare stopSelf(): the app's own MediaController may still be bound (C-09).
        pauseAllPlayersAndStopSelf()
    }

    /**
     * Answers: does the player still mean to play? True while playing or buffering, and also
     * during a phone call (audio focus lost for a moment). False when paused, after an error
     * (idle) or at the end of the queue.
     *
     * @author Phong-Kaster
     */
    private fun isPlaybackOngoing(player: Player?): Boolean {
        if (player == null) return false
        if (player.playbackState == Player.STATE_IDLE) return false
        if (player.playbackState == Player.STATE_ENDED) return false
        return player.playWhenReady
    }

    /**
     * Hands our session to any controller that asks (the app itself and the notification).
     *
     * @author Phong-Kaster
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /**
     * Frees the player and the session when the service dies, so no sound or memory is left behind.
     *
     * @author Phong-Kaster
     */
    override fun onDestroy() {
        mediaSession?.let { session ->
            session.player.release()
            session.release()
        }
        mediaSession = null
        super.onDestroy()
    }

    companion object {
        /** Request code of the notification's "open the app" ticket. */
        private const val OPEN_MUSIC_REQUEST_CODE = 1001
    }
}

/**
 * Session callback that makes sure every song added by a controller still has its audio address.
 *
 * @author Phong-Kaster
 */
@OptIn(UnstableApi::class)
private class PlayableItemsCallback : MediaSession.Callback {

    /**
     * Called when a controller adds or sets songs. We copy back the address that may have been
     * removed on the way (see [withPlayableUri]).
     *
     * @author Phong-Kaster
     */
    override fun onAddMediaItems(
        mediaSession: MediaSession,
        controller: MediaSession.ControllerInfo,
        mediaItems: MutableList<MediaItem>,
    ): ListenableFuture<MutableList<MediaItem>> {
        val playableItems = mediaItems
            .map { item -> item.withPlayableUri() }
            .toMutableList()
        return Futures.immediateFuture(playableItems)
    }
}

/*
 * --- Why a ForwardingPlayer (simple story) ---
 * ExoPlayer's own "previous" restarts the current song when it has played for a few seconds.
 * We want "previous" to ALWAYS go to the song before, and both ends to wrap around.
 * So we wrap ExoPlayer in a thin "middle-man" that answers next / previous itself, using
 * PlaybackQueuePolicy, and passes every other call straight to ExoPlayer.
 * We also say "yes, next and previous are available" whenever there is at least one song,
 * so the app bar and the notification always show working buttons.
 */

/**
 * ExoPlayer wrapped so next / previous always follow [PlaybackQueuePolicy]; every other call
 * goes straight to the real player.
 *
 * Example:
 * ```kotlin
 * MediaSession.Builder(context, QueuePolicyPlayer(player = exoPlayer)).build()
 * ```
 *
 * @param player The real ExoPlayer.
 * @author Phong-Kaster
 */
@OptIn(UnstableApi::class)
private class QueuePolicyPlayer(player: Player) : ForwardingPlayer(player) {

    /** Next song (wraps to the first). @author Phong-Kaster */
    override fun seekToNext() {
        seekToQueueIndex(targetIndex = PlaybackQueuePolicy.nextIndex(current = currentMediaItemIndex, size = mediaItemCount))
    }

    /** Same as [seekToNext]. @author Phong-Kaster */
    override fun seekToNextMediaItem() {
        seekToQueueIndex(targetIndex = PlaybackQueuePolicy.nextIndex(current = currentMediaItemIndex, size = mediaItemCount))
    }

    /** Previous song, even mid-song (wraps to the last). @author Phong-Kaster */
    override fun seekToPrevious() {
        seekToQueueIndex(targetIndex = PlaybackQueuePolicy.previousIndex(current = currentMediaItemIndex, size = mediaItemCount))
    }

    /** Same as [seekToPrevious]. @author Phong-Kaster */
    override fun seekToPreviousMediaItem() {
        seekToQueueIndex(targetIndex = PlaybackQueuePolicy.previousIndex(current = currentMediaItemIndex, size = mediaItemCount))
    }

    /** There is always a next song while the queue has one (it wraps). @author Phong-Kaster */
    override fun hasNextMediaItem(): Boolean {
        if (mediaItemCount >= 1) return true
        return super.hasNextMediaItem()
    }

    /** There is always a previous song while the queue has one (it wraps). @author Phong-Kaster */
    override fun hasPreviousMediaItem(): Boolean {
        if (mediaItemCount >= 1) return true
        return super.hasPreviousMediaItem()
    }

    /** The real commands plus the four skip commands while the queue has songs. @author Phong-Kaster */
    override fun getAvailableCommands(): Player.Commands {
        val commands = super.getAvailableCommands()
        if (mediaItemCount < 1) return commands
        return commands.buildUpon()
            .addAll(*QUEUE_COMMANDS)
            .build()
    }

    /** Matches [getAvailableCommands] for a single command. @author Phong-Kaster */
    override fun isCommandAvailable(command: Int): Boolean {
        if (mediaItemCount >= 1 && command in QUEUE_COMMANDS) return true
        return super.isCommandAvailable(command)
    }

    /**
     * Jumps to the start (position 0) of the song at [targetIndex]; does nothing when it is null
     * (empty queue).
     *
     * @author Phong-Kaster
     */
    private fun seekToQueueIndex(targetIndex: Int?) {
        if (targetIndex == null) return
        seekTo(targetIndex, 0L)
    }

    companion object {
        /** The four skip commands we always allow while the queue has songs. */
        private val QUEUE_COMMANDS = intArrayOf(
            Player.COMMAND_SEEK_TO_NEXT,
            Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM,
            Player.COMMAND_SEEK_TO_PREVIOUS,
            Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM,
        )
    }
}
