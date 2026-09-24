package com.example.skeleton.data.service

import android.app.PendingIntent
import android.content.Intent
import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.skeleton.MainActivity

/**
 * Foreground service that owns the music player, so music keeps playing when the app is in
 * the background. Media3 shows the media notification for us while something plays.
 *
 * The app (and the notification, lock screen, headset buttons) talk to the player through
 * the [MediaSession] returned by [onGetSession].
 *
 * Example: `PlayerRepositoryImpl` connects with
 * `SessionToken(context, ComponentName(context, MusicPlaybackService::class.java))`.
 * @author Phong-Kaster
 */
class MusicPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null

    /**
     * Builds the ExoPlayer (music audio focus, pause on unplugged headphones, repeat the whole
     * queue) and wraps it in a [MediaSession].
     * @author Phong-Kaster
     */
    @OptIn(UnstableApi::class)
    override fun onCreate() {
        super.onCreate()
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()

        val exoPlayer = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .build()
        exoPlayer.repeatMode = Player.REPEAT_MODE_ALL

        mediaSession = MediaSession.Builder(this, PreviousSongForwardingPlayer(player = exoPlayer))
            .setSessionActivity(buildOpenAppPendingIntent())
            .build()
    }

    /**
     * Builds the "open the app" ticket used when the user taps the media notification.
     *
     * The flags NEW_TASK + CLEAR_TOP + SINGLE_TOP mean: if the app is already open, reuse that
     * same [MainActivity] (it receives `onNewIntent`) instead of stacking a second copy. The
     * extra [MainActivity.EXTRA_OPEN_MUSIC] tells [MainActivity] to jump to the Music screen,
     * even if the user left the app on Home or Setting, so one Back then leaves the app.
     *
     * Example: `MediaSession.Builder(...).setSessionActivity(buildOpenAppPendingIntent())`.
     * @author Phong-Kaster
     */
    private fun buildOpenAppPendingIntent(): PendingIntent {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_OPEN_MUSIC, true)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            OPEN_APP_REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    /**
     * Hands our one session to every controller (the app, the notification, Bluetooth, ...).
     * @author Phong-Kaster
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /**
     * Called when the user swipes the app away from the recent-apps screen.
     *
     * If music is really playing we keep the service alive, so the song goes on. Otherwise
     * (no player, paused, empty queue, the queue finished, or the player failed and went idle)
     * nobody needs us: [pauseAllPlayersAndStopSelf] pauses the player, removes the
     * notification and stops the service, even while the app's controller is still connected.
     * @param rootIntent The intent that started the task that was removed.
     * @author Phong-Kaster
     */
    @OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        val isActuallyPlaying = player != null &&
            player.playWhenReady &&
            player.mediaItemCount > 0 &&
            player.playbackState != Player.STATE_IDLE &&
            player.playbackState != Player.STATE_ENDED
        if (isActuallyPlaying) return
        pauseAllPlayersAndStopSelf()
    }

    /**
     * Frees the player and the session when the service goes away.
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
        /**
         * Request code of the "open the app" [PendingIntent] behind the notification.
         * @author Phong-Kaster
         */
        private const val OPEN_APP_REQUEST_CODE = 0
    }
}

/**
 * A player wrapper that makes "previous" always go to the previous song.
 *
 * A normal player restarts the current song when "previous" is pressed a few seconds into it.
 * Here both the in-app button and the notification button jump straight to the previous
 * song; with repeat-all, pressing it on the first song wraps to the last one.
 * @param player The real player that does the work.
 * @author Phong-Kaster
 */
@OptIn(UnstableApi::class)
private class PreviousSongForwardingPlayer(player: Player) : ForwardingPlayer(player) {

    override fun seekToPrevious() {
        seekToPreviousMediaItem()
    }
}
