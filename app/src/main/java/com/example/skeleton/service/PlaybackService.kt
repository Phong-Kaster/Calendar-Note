package com.example.skeleton.service

import android.app.PendingIntent
import android.content.Intent
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.skeleton.MainActivity
import com.example.skeleton.data.mapper.withPlayableUri
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * The background "music box" of the app. It owns the one real player (ExoPlayer) and the song
 * queue inside it, and shares them through a [MediaSession]. Because the session lives here
 * (not in a screen), music keeps playing when the app is closed, and the media notification,
 * lock screen, headset and Bluetooth buttons all control the same queue.
 *
 * - The notification is drawn by Media3's default `DefaultMediaNotificationProvider`
 *   (previous / play-pause / next, title, artist and cover from `MediaMetadata`).
 * - Tapping the notification opens [MainActivity].
 * - The player pauses itself when headphones are unplugged and respects audio focus
 *   (for example it pauses for a phone call).
 *
 * Nobody creates this class by hand. The app talks to it through `PlayerRepositoryImpl`,
 * which connects a `MediaController` to it:
 * ```kotlin
 * val token = SessionToken(context, ComponentName(context, PlaybackService::class.java))
 * MediaController.Builder(context, token).buildAsync()
 * ```
 *
 * @author Phong-Kaster
 */
class PlaybackService : MediaSessionService() {

    /** The shared session; null before [onCreate] and after [onDestroy]. */
    private var mediaSession: MediaSession? = null

    /**
     * Builds the player and the session when the service starts.
     *
     * @author Phong-Kaster
     */
    override fun onCreate() {
        super.onCreate()
        mediaSession = MediaSession.Builder(this, buildPlayer())
            .setCallback(PlaybackSessionCallback())
            .setSessionActivity(buildOpenAppPendingIntent())
            .build()
    }

    /**
     * Hands the session to any controller that asks (our app, the system UI, Bluetooth, ...).
     *
     * @author Phong-Kaster
     */
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    /**
     * Called when the user swipes the app away from recent apps.
     * If music is playing, we keep going (that is the whole point of a music player).
     * If not, we pause everything and stop the service so it does not linger.
     *
     * @author Phong-Kaster
     */
    @androidx.annotation.OptIn(UnstableApi::class)
    override fun onTaskRemoved(rootIntent: Intent?) {
        val isPlaybackOngoing = mediaSession?.player?.let { player ->
            val isFinishedOrBroken = player.playbackState == Player.STATE_ENDED ||
                player.playbackState == Player.STATE_IDLE
            player.playWhenReady && player.mediaItemCount > 0 && !isFinishedOrBroken
        } ?: false
        if (isPlaybackOngoing) return
        pauseAllPlayersAndStopSelf()
    }

    /**
     * Frees the player and the session when the service dies.
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

    /**
     * Makes the ExoPlayer: music audio attributes, automatic audio focus handling (`true`),
     * "pause when headphones are unplugged", and a wake lock so the screen-off phone keeps playing.
     *
     * @author Phong-Kaster
     */
    private fun buildPlayer(): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .build()
        return ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()
    }

    /**
     * The "open the app" action used when the notification is tapped.
     * `MainActivity` is `singleTop`, so this reuses the open screen instead of stacking a new one.
     *
     * @author Phong-Kaster
     */
    private fun buildOpenAppPendingIntent(): PendingIntent {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        return PendingIntent.getActivity(
            this,
            OPEN_APP_REQUEST_CODE,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )
    }

    /**
     * Listens to requests coming into the session.
     * When songs arrive from a controller, their play address is missing (Media3 drops it on the
     * way), so we put it back from `RequestMetadata.mediaUri` before the player gets them.
     *
     * @author Phong-Kaster
     */
    private class PlaybackSessionCallback : MediaSession.Callback {
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

    companion object {
        private const val OPEN_APP_REQUEST_CODE = 0
    }
}
