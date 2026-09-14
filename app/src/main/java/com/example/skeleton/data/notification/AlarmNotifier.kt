package com.example.skeleton.data.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.skeleton.MainActivity
import com.example.skeleton.R
import com.example.skeleton.domain.model.Alarm

/**
 * Puts an alarm on screen when its time comes.
 *
 * One job, one class: build the notification and post it. It knows nothing about *when* — that is
 * `AlarmScheduler`'s half — and nothing about the database.
 *
 * **What "the alarm pops up" means here.** A heads-up notification: the banner that slides down over
 * whatever the user is looking at, with a sound and a buzz. It is deliberately **not** a full-screen
 * alarm-clock takeover — no `setFullScreenIntent`, no do-not-disturb override. This app reminds
 * people about notes; hijacking the whole screen (and talking over a silenced phone) is a different
 * product, and both of those are permissions a user has to be asked for separately.
 *
 * **Getting a heads-up needs two completely separate things to be right, on two different eras of
 * Android, and only one of them is visible on the phone in front of you.**
 *
 * - **API 26 and up** decides from the *channel's* importance. [CHANNEL_IMPORTANCE] must be
 *   `IMPORTANCE_HIGH`, and here is the trap: **a channel's importance is frozen the first time it is
 *   created.** Creating one at `IMPORTANCE_DEFAULT` and "raising" it in a later release does exactly
 *   nothing on any phone that already ran the app — `createNotificationChannel` with an id that
 *   already exists is a silent no-op. There is no second chance short of changing [CHANNEL_ID],
 *   which throws away whatever the user had customised. So it is high from the very first install.
 * - **API 24 and 25** have no channels at all and read [NOTIFICATION_PRIORITY] off the builder —
 *   and `PRIORITY_HIGH` **alone is not enough**: a heads-up on those versions needs priority *plus*
 *   a sound or a vibration. Both are set below. Dropping either one silently downgrades the alarm to
 *   a quiet line in the shade on exactly the phones nobody tests on.
 *
 * Neither path can be checked by a test here — `NotificationChannel` and `Notification` are stubs on
 * this toolchain whose getters answer `0` — so what *is* checked, in `AlarmNotifierConstantsTest`,
 * are the five constants below. That is why they are constants and not values typed inline.
 *
 * @param context used to read strings, build the tap target and reach the notification service.
 *   The application context is the right one to hand over — a notification outlives any screen.
 * @author Phong-Kaster
 */
class AlarmNotifier(private val context: Context) {

    /**
     * Shows this alarm's notification now.
     *
     * @param alarm the alarm that has just gone off. Its `message` is the body — that message is the
     *   entire reason the user set the alarm, so it is what the banner says.
     */
    fun notifyAlarm(alarm: Alarm) {
        createChannelIfNeeded()

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            // Never `@mipmap/ic_launcher`. Android re-tints a status-bar icon to a flat white
            // silhouette, so a full-colour launcher icon arrives as an unreadable white blob.
            // `ic_notification_alarm` is drawn solid white for exactly this reason.
            .setSmallIcon(R.drawable.ic_notification_alarm)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(alarm.message)
            // A long message is truncated to one line in the banner. `BigTextStyle` is what lets the
            // user pull the shade down and read the rest — an alarm whose point is its wording.
            .setStyle(NotificationCompat.BigTextStyle().bigText(alarm.message))
            .setPriority(NOTIFICATION_PRIORITY)
            .setCategory(NOTIFICATION_CATEGORY)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .setVibrate(VIBRATION_PATTERN)
            .setAutoCancel(true)
            .setContentIntent(openAppIntent())
            .build()

        try {
            // On Android 13+ this quietly posts nothing when POST_NOTIFICATIONS was never granted.
            // Telling the user about that is the permission notice's job, not this class's — all
            // that matters here is that it does not throw inside a broadcast receiver.
            NotificationManagerCompat.from(context).notify(notificationIdFor(alarmId = alarm.id), notification)
        } catch (e: SecurityException) {
            Log.w(TAG, "notification for alarm ${alarm.id} was not posted; permission is missing", e)
        }
    }

    /**
     * Creates the alarms channel the first time, and does nothing on every later call.
     *
     * Safe to call before every notification — that is the documented way to use it, because the
     * channel is gone again after the user clears the app's data. Also called once from
     * `MainApplication.onCreate`, so the channel (and the choices a user makes about it in system
     * settings — sound, vibration, whether it may pop up at all) exists from the first launch rather
     * than only appearing the first time an alarm actually fires.
     */
    fun createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            // Reuses the existing `exact_alarm` string — "Alarms & reminders" — because that is
            // exactly the right wording for what the user sees in system settings, and it is
            // already translated. A second string saying the same words would only be a second
            // thing to keep in step.
            context.getString(R.string.exact_alarm),
            CHANNEL_IMPORTANCE,
        )
        channel.enableVibration(true)
        channel.vibrationPattern = VIBRATION_PATTERN
        channel.setSound(
            RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
            AudioAttributes.Builder()
                // `USAGE_NOTIFICATION`, not `USAGE_ALARM`. Alarm usage plays through the alarm
                // stream, which stays loud when the phone is silenced — right for a wake-up clock,
                // wrong for a note reminder, and not something this app asked the user about.
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (manager == null) {
            Log.e(TAG, "no NotificationManager; the alarms channel was not created")
            return
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * Where a tap on the notification lands: the app's one activity, on the Alarms list.
     *
     * `CLEAR_TOP or SINGLE_TOP` so that tapping it brings the app the user already has open to the
     * front instead of stacking a second copy of it behind the first. [ACTION_OPEN_ALARMS] is what
     * tells `MainActivity` a launch is this rather than an ordinary one — the action, not an extra,
     * because `MainActivity` has to make the same decision whether it is created fresh (`onCreate`) or
     * already on top (`onNewIntent`), and both read `Intent.action` the same way.
     */
    private fun openAppIntent(): PendingIntent = PendingIntent.getActivity(
        context,
        // One request code for every alarm on purpose: this intent is identical whichever alarm was
        // tapped — it opens the app. It is **not** the alarm-addressing request code used by
        // `AlarmManagerAlarmScheduler`, which is the alarm's own row id and is never zero.
        OPEN_APP_REQUEST_CODE,
        Intent(context, MainActivity::class.java).apply {
            action = ACTION_OPEN_ALARMS
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        },
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )

    companion object {

        private const val TAG = "AlarmNotifier"

        /**
         * The id of the one channel every alarm is posted to.
         *
         * **Treat this string as permanent.** It is the name under which the user's own choices
         * about this app's alarms — sound, vibration, whether they are allowed to pop up at all —
         * are filed on their phone. Changing it does not move those settings: it silently creates a
         * *second*, freshly-defaulted channel and abandons the first, which then sits in the app's
         * settings screen forever with nothing posting to it.
         */
        const val CHANNEL_ID = "alarms"

        /**
         * How loudly the channel is allowed to interrupt, on API 26 and up: `IMPORTANCE_HIGH`,
         * which is the lowest setting that still produces a heads-up banner.
         *
         * Frozen at first creation — see the class KDoc. This is the one number in the feature that
         * cannot be corrected in a later release.
         */
        val CHANNEL_IMPORTANCE: Int = NotificationManager.IMPORTANCE_HIGH

        /**
         * The same idea for API 24 and 25, which have no channels: `PRIORITY_HIGH` on the builder.
         *
         * Still set on newer versions too, where it is simply ignored — one builder, no branching,
         * nothing to forget.
         */
        val NOTIFICATION_PRIORITY: Int = NotificationCompat.PRIORITY_HIGH

        /**
         * What kind of notification this is: `CATEGORY_ALARM`.
         *
         * The system uses it to rank and group notifications, and to decide what survives a
         * low-power or driving mode. It is a label, not a permission — it does not let the app past
         * do-not-disturb on its own.
         */
        val NOTIFICATION_CATEGORY: String = NotificationCompat.CATEGORY_ALARM

        /**
         * Buzz, pause, buzz. The **second** half of what makes a heads-up on API 24 and 25, where
         * `PRIORITY_HIGH` by itself is not enough. Milliseconds: wait, buzz, wait, buzz.
         */
        val VIBRATION_PATTERN: LongArray = longArrayOf(0L, 400L, 200L, 400L)

        /** The request code of the "open the app" tap target. See [openAppIntent]. */
        private const val OPEN_APP_REQUEST_CODE = 0

        /**
         * Marks an intent as "opened from an alarm notification", so `MainActivity` can navigate to
         * the Alarms list rather than wherever it would ordinarily start.
         *
         * A distinct action rather than an extra: `MainActivity` reads it from both `onCreate` (a
         * fresh launch) and `onNewIntent` (the app was already open), and both need to ask the exact
         * same question of the intent they were handed.
         */
        const val ACTION_OPEN_ALARMS = "com.example.skeleton.action.OPEN_ALARMS"

        /**
         * Which notification an alarm replaces when it goes off again.
         *
         * The alarm's own row id, so every alarm owns one line in the shade: two different alarms
         * never overwrite each other, and the same alarm going off tomorrow replaces its own
         * yesterday's banner instead of stacking a pile of identical ones.
         *
         * @param alarmId the alarm's row id.
         */
        fun notificationIdFor(alarmId: Long): Int = alarmId.toInt()
    }
}
