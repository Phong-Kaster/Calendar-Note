package com.example.skeleton.data.service

import androidx.annotation.OptIn
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

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

        mediaSession = MediaSession.Builder(this, PreviousSongForwardingPlayer(player = exoPlayer)).build()
    }

    /**
     * Hands our one session to every controller (the app, the notification, Bluetooth, ...).
     * @author Phong-Kaster
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    // T-004: onTaskRemoved goes here.

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
