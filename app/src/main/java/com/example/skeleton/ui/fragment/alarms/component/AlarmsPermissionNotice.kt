package com.example.skeleton.ui.fragment.alarms.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * The warning that tells the difference between an alarm that is armed and an alarm the operating
 * system will never deliver.
 *
 * **This is the quietest failure the alarms feature has.** An alarm the user wrote, saved, and can
 * see switched on in the list looks exactly the same whether it will ring or not — the row draws
 * what the store holds, and the store knows nothing about permissions. Only the permission state
 * can tell them apart, so it has to be said out loud, here, on the screen where the armed-looking
 * alarms are.
 *
 * **A banner and not a dialog.** A user who opened this tab only to look at their alarms must not
 * have to dismiss something first. It sits above the list, stays put, and asks for nothing.
 *
 * **Each problem gets its own line and its own button**, because the two are fixed on two different
 * system screens: notifications are turned back on in the app's own settings page, exact alarms in
 * Android's "Alarms & reminders" screen. One button offered for both would send half the users to a
 * screen where the thing they were told to fix is not present.
 *
 * The button sits **underneath** its sentence rather than beside it. Side by side looks tidier at
 * 360dp in English and stops fitting the moment the app is read in German, where both the sentence
 * and the button label are roughly half as long again — and a button squeezed off the end of a row
 * is a fix the user cannot reach.
 *
 * Colours come from the error roles of the scheme, as a verified pair: `errorContainer` behind,
 * `onErrorContainer` on top (10.0:1). The 1dp `error` outline is what separates the banner from the
 * ground — the container fill alone measures 1.62:1 against this app's true-black background, which
 * is a block the eye barely finds, while the outline measures 7.6:1 and draws the edge clearly.
 *
 * @param notificationsGranted whether the app is allowed to post notifications. False puts the
 *   first line on screen.
 * @param exactAlarmGranted whether the app is allowed to schedule exact alarms. False puts the
 *   second line on screen.
 * @param onOpenNotificationSettings the user wants the notification problem fixed — the caller
 *   opens the app's settings page.
 * @param onRequestExactAlarm the user wants the exact-alarm problem fixed — the caller opens
 *   Android's "Alarms & reminders" screen.
 * @param modifier applied to the banner itself, which is where the screen's 16dp side margin comes
 *   from so the banner lines up with the alarm rows below it.
 * @author Phong-Kaster
 */
@Composable
fun AlarmsPermissionNotice(
    notificationsGranted: Boolean,
    exactAlarmGranted: Boolean,
    onOpenNotificationSettings: () -> Unit = {},
    onRequestExactAlarm: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Nothing is wrong, so nothing at all is drawn — not an empty box, not a gap. A warning that
    // keeps its space when it has nothing to warn about leaves a hole above the list that looks
    // like a bug on every correctly-configured device, which is most of them.
    if (notificationsGranted && exactAlarmGranted) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            // Clipped first so the outline, the fill and the ripples inside all stop at the same
            // rounded corners. The 16dp radius is `AlarmRow`'s, so the banner reads as a sibling of
            // the rows rather than as a foreign object dropped on top of them.
            .clip(shape = RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.error,
                shape = RoundedCornerShape(16.dp),
            )
            .background(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // The consequence first, the causes after it. "Notifications are turned off" is a fact
        // about the phone; "your alarms will not go off" is the reason the user should care, and it
        // is the half they cannot work out for themselves.
        Text(
            text = stringResource(R.string.alarms_may_not_reach_you),
            style = customizedTextStyle(
                fontSize = 14,
                fontWeight = 600,
                lineHeight = 20,
                color = MaterialTheme.colorScheme.onErrorContainer,
            ),
        )

        if (!notificationsGranted) {
            AlarmsPermissionNoticeLine(
                message = stringResource(R.string.notifications_are_turned_off),
                action = stringResource(R.string.turn_on_notifications),
                onClick = onOpenNotificationSettings,
            )
        }

        if (!exactAlarmGranted) {
            AlarmsPermissionNoticeLine(
                message = stringResource(R.string.exact_alarms_are_not_allowed),
                action = stringResource(R.string.allow_exact_alarms),
                onClick = onRequestExactAlarm,
            )
        }
    }
}

/**
 * One problem and the one button that fixes it.
 *
 * Both lines go through the same composable so the pair cannot drift apart in padding, radius or
 * weight — once two warnings in the same banner look slightly different, the difference starts
 * reading as *meaning* rather than as an accident.
 *
 * The button's vertical padding is 14dp so that the label plus its padding clears the 48dp minimum
 * touch target (21dp of text + 28dp of padding = 49dp), the same arithmetic the delete
 * confirmation's controls use. It carries the full action as its label — "Turn on notifications",
 * not "Turn on" — so a screen reader announces which of the two buttons it has landed on, and so
 * does the eye of somebody who skipped the sentence above it.
 *
 * The message is deliberately **not** given `maxLines = 1` and a marquee, unlike most single-line
 * text in this project: it may wrap in a longer language, and a warning that is cut off or that
 * slides sideways is a warning nobody finishes reading.
 *
 * @param message what is wrong, in one sentence.
 * @param action what the button does, written out in full.
 * @param onClick the user tapped the button.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmsPermissionNoticeLine(
    message: String,
    action: String,
    onClick: () -> Unit = {},
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = message,
            style = customizedTextStyle(
                fontSize = 14,
                fontWeight = 400,
                lineHeight = 20,
                color = MaterialTheme.colorScheme.onErrorContainer,
            ),
        )

        Text(
            text = action,
            style = customizedTextStyle(
                fontSize = 14,
                fontWeight = 600,
                color = MaterialTheme.colorScheme.onErrorContainer,
            ),
            modifier = Modifier
                // Clipped first so the ripple stops at the rounded corners.
                .clip(shape = RoundedCornerShape(12.dp))
                // Outlined rather than filled. On this deep-red container a second block of colour
                // would fight the banner itself; an outline in the same colour as the label is
                // enough to say "this is a control" and keeps the warning's one loud element — the
                // banner's own edge — loud.
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    shape = RoundedCornerShape(12.dp),
                )
                .clickable(
                    onClickLabel = action,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = ripple(bounded = true),
                    onClick = onClick,
                )
                // After the click, so the padding is inside the tap target.
                .padding(vertical = 14.dp, horizontal = 20.dp),
        )
    }
}

/**
 * Both permissions missing — the state the banner exists for, and the tallest it ever gets.
 *
 * 260dp of height for a banner that measures 222dp, because a preview smaller than the thing it
 * renders does not record as clipped, it records as empty.
 */
@Preview(name = "Alarms permission notice - both missing", widthDp = 360, heightDp = 260)
@Composable
private fun AlarmsPermissionNoticeBothMissingPreview() {
    // On the app's real ground colour rather than Studio's default white: the banner is a coloured
    // block with an outline, and both of those only mean anything against the black this app
    // actually paints behind them.
    MyApplicationTheme(
        content = {
            AlarmsPermissionNotice(
                notificationsGranted = false,
                exactAlarmGranted = false,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        },
    )
}

/** Only notifications missing — one line, one button, and no gap where the other one would be. */
@Preview(name = "Alarms permission notice - notifications off", widthDp = 360, heightDp = 180)
@Composable
private fun AlarmsPermissionNoticeNotificationsPreview() {
    MyApplicationTheme(
        content = {
            AlarmsPermissionNotice(
                notificationsGranted = false,
                exactAlarmGranted = true,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        },
    )
}

/**
 * Only exact alarms missing.
 *
 * Worth its own picture rather than being assumed from the one above: this is the common case on
 * Android 12 and newer, where notifications are usually allowed and "Alarms & reminders" is the
 * switch nobody knows exists.
 */
@Preview(name = "Alarms permission notice - exact alarms blocked", widthDp = 360, heightDp = 180)
@Composable
private fun AlarmsPermissionNoticeExactAlarmPreview() {
    MyApplicationTheme(
        content = {
            AlarmsPermissionNotice(
                notificationsGranted = true,
                exactAlarmGranted = false,
                modifier = Modifier.padding(horizontal = 16.dp),
            )
        },
    )
}
