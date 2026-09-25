package com.example.skeleton.service

import android.content.Context
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.CommandButton
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaNotification
import androidx.media3.session.MediaSession
import com.example.skeleton.R
import com.google.common.collect.ImmutableList

/**
 * Builds the music notification in the app's Material 3 look, while keeping Media3's standard
 * media notification (no custom layouts).
 *
 * Simple story: Media3 already knows how to draw a great music notification
 * ([DefaultMediaNotificationProvider]). We let it do all the work, and only change two things:
 * - the small icon in the status bar becomes our white music note ([R.drawable.ic_notification_music]);
 * - the accent colour becomes the app's primary colour ([R.color.notification_accent]).
 *
 * Media3 may build the notification more than once for one song (for example, again when the
 * album art has finished loading), so every version gets the same accent colour.
 *
 * Example:
 * ```kotlin
 * setMediaNotificationProvider(MusicNotificationProvider(context = this))
 * ```
 *
 * @param context Used to read the icon and colour resources.
 * @author Phong-Kaster
 */
@OptIn(UnstableApi::class)
class MusicNotificationProvider(
    private val context: Context,
) : MediaNotification.Provider {

    /** Media3's own notification builder that does the real drawing. */
    private val defaultProvider: DefaultMediaNotificationProvider =
        DefaultMediaNotificationProvider.Builder(context).build().apply {
            setSmallIcon(R.drawable.ic_notification_music)
        }

    /**
     * Asks Media3 for its standard notification and paints it with our accent colour.
     * If Media3 later sends a newer version (for example with album art), that one is painted too.
     *
     * @author Phong-Kaster
     */
    override fun createNotification(
        mediaSession: MediaSession,
        mediaButtonPreferences: ImmutableList<CommandButton>,
        actionFactory: MediaNotification.ActionFactory,
        onNotificationChangedCallback: MediaNotification.Provider.Callback,
    ): MediaNotification {
        val tintingCallback = MediaNotification.Provider.Callback { changedNotification ->
            onNotificationChangedCallback.onNotificationChanged(applyAccentColor(mediaNotification = changedNotification))
        }
        val mediaNotification = defaultProvider.createNotification(
            mediaSession,
            mediaButtonPreferences,
            actionFactory,
            tintingCallback,
        )
        return applyAccentColor(mediaNotification = mediaNotification)
    }

    /**
     * Custom notification buttons are handled by Media3's default provider, exactly as before.
     *
     * @author Phong-Kaster
     */
    override fun handleCustomCommand(
        session: MediaSession,
        action: String,
        extras: Bundle,
    ): Boolean = defaultProvider.handleCustomCommand(session, action, extras)

    /**
     * Paints the notification with the app's accent colour and gives it back.
     *
     * @author Phong-Kaster
     */
    private fun applyAccentColor(mediaNotification: MediaNotification): MediaNotification {
        val notification = mediaNotification.notification
        notification.color = ContextCompat.getColor(context, R.color.notification_accent)
        return MediaNotification(mediaNotification.notificationId, notification)
    }
}
