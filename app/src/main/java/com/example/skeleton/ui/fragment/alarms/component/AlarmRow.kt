package com.example.skeleton.ui.fragment.alarms.component

import android.text.format.DateFormat
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Alarm
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * One alarm as a list row: the time it goes off, what it says, whether it is armed, and a way to
 * remove it.
 *
 * **There are three tap targets in this one row, and they do different things.** The card itself
 * opens the alarm for editing; the switch arms or disarms it without opening anything; the bin
 * removes it — after a confirmation the row itself knows nothing about. Compose hands a touch to the
 * innermost thing that wants it, so the switch and the bin do not swallow the card's tap and the
 * card does not swallow theirs. Each of the three carries its own `MutableInteractionSource`, so
 * their ripples stay their own shapes rather than merging into one flash across the whole row.
 *
 * The message stops after two lines with an ellipsis rather than growing to fit. Without that, one
 * long reminder pushes every other alarm off the screen — and pushes the time, which is the thing
 * the user is scanning for, away from its neighbours.
 *
 * @param alarm the alarm to draw.
 * @param onClick the user tapped this row and wants the alarm opened.
 * @param onToggleEnabled the user moved the switch. True means "arm it".
 * @param onDelete the user tapped the bin. It only *asks* — nothing is removed until the
 *   confirmation this raises has been answered.
 * @param modifier applied to the card.
 * @author Phong-Kaster
 */
@Composable
fun AlarmRow(
    alarm: Alarm,
    onClick: () -> Unit,
    onToggleEnabled: (Boolean) -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // Written the way the clock on this device is written, because the time picker in the editor
    // already does exactly that (`rememberTimePickerState` defaults `is24Hour` to the device
    // setting). Hard-coding 24-hour here would mean a user on a 12-hour device sets "9:30 PM",
    // taps Save, and the row that appears reads "21:30" — the same alarm in two notations, one
    // screen apart, which reads like the app changed what they typed.
    //
    // Formatted through `java.time` against the app's own locale rather than `Locale.getDefault()`:
    // this app has its own language picker, so the device locale is not necessarily the one the
    // user chose to read the app in. `remember(...)` keyed on both inputs so the formatter is not
    // rebuilt on every recomposition, but is rebuilt when either actually changes.
    val locale = LocalConfiguration.current.locales[0]
    val is24Hour = DateFormat.is24HourFormat(LocalContext.current)
    val timeFormatter = remember(locale, is24Hour) {
        DateTimeFormatter.ofPattern(if (is24Hour) "HH:mm" else "h:mm a", locale)
    }
    val displayTime = timeFormatter.format(LocalTime.of(alarm.hourOfDay, alarm.minute))

    // A switched-off alarm reads as *quieter*, not just as a switch pointing the other way. The
    // switch is small and a user scanning a list of six alarms is reading the times, so the time
    // itself drops to the muted role when the alarm is off. Both colours come from the scheme —
    // `onSurface` is the ordinary text colour and `onSurfaceVariant` the deliberately dimmer one.
    val timeColor = if (alarm.enabled) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            // Clipped first so the ripple stops at the rounded corners.
            .clip(shape = RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(16.dp),
            )
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(16.dp),
            )
            .clickable(
                // Without the label a screen reader reads the row's text and gives no hint that it
                // is a door.
                onClickLabel = stringResource(R.string.open_alarm),
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onClick,
            )
            // After the click, so the padding is inside the tap target.
            .padding(start = 16.dp, top = 16.dp, end = 8.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // One deliberate deviation from the house rule that single-line text gets
            // `Modifier.basicMarquee(...)`: a marquee animates, and "08:00" cannot overflow a row
            // this wide, so the only thing a marquee could do here is move something that should be
            // still. `weight(1f)` pushes the two controls to the far end of the row.
            Text(
                text = displayTime,
                style = customizedTextStyle(fontSize = 24, fontWeight = 600, color = timeColor),
                maxLines = 1,
                modifier = Modifier.weight(1f),
            )

            AlarmEnabledSwitch(
                enabled = alarm.enabled,
                onToggleEnabled = onToggleEnabled,
            )

            AlarmDeleteAffordance(onDelete = onDelete)
        }

        Text(
            text = alarm.message,
            style = customizedTextStyle(
                fontSize = 14,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            // The end padding the card gives up so the bin's 48dp tap target can reach the edge —
            // put back here, so the message still stops where the time starts to look ragged.
            modifier = Modifier.padding(end = 8.dp),
        )
    }
}

/**
 * The switch that arms an alarm or puts it to sleep.
 *
 * **Persistence only.** Moving this switch stores the flag and nothing more — no alarm rings from
 * this switch alone, because nothing in the app schedules one yet. See `AlarmsViewModel.setEnabled`.
 *
 * **Every colour is named explicitly rather than left to `SwitchDefaults.colors()`**, and that is
 * not tidiness. Material's dark default paints the *off* thumb in `outline` on an
 * `surfaceContainerHighest` track: on this app's palette those two measure 2.14:1 against each
 * other, so the thumb all but disappears into the track and a switched-off alarm looks like a grey
 * smudge rather than like a control. The off thumb is `onSurfaceVariant` here instead (6.4:1
 * against the same track), and the off border is the same colour, so the switch's outline is
 * clearly findable against the row it sits on. The *on* pair is Material's — `onPrimary` on
 * `primary` — which already measures 6.8:1 and needs no help.
 *
 * The switch names itself for a screen reader; it announces its own on/off state for itself, so the
 * description says *which* switch this is rather than repeating the state.
 *
 * @param enabled whether the alarm is armed.
 * @param onToggleEnabled the user moved the switch.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmEnabledSwitch(
    enabled: Boolean,
    onToggleEnabled: (Boolean) -> Unit = {},
) {
    val toggleLabel = stringResource(R.string.turn_this_alarm_on_or_off)

    Switch(
        checked = enabled,
        onCheckedChange = onToggleEnabled,
        colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
            checkedTrackColor = MaterialTheme.colorScheme.primary,
            uncheckedThumbColor = MaterialTheme.colorScheme.onSurfaceVariant,
            uncheckedTrackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
            uncheckedBorderColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        // Its own source, so the switch's ripple is the switch's and does not merge with the
        // card-wide ripple underneath it.
        interactionSource = remember { MutableInteractionSource() },
        modifier = Modifier.semantics { contentDescription = toggleLabel },
    )
}

/**
 * The bin that starts a deletion.
 *
 * **A visible control and not a swipe.** A gesture with no affordance is a feature only the people
 * who built it know about, and the one destructive action in a list is the worst place to hide.
 *
 * It is 48dp square with the icon drawn 24dp inside it: the padding comes *after* the click, so the
 * whole 48dp is the tap target rather than dead space around a 24dp one. A destructive control that
 * is hard to hit is bad; one that is hard to hit *next to a row that opens an editor* is worse,
 * because every miss lands somewhere.
 *
 * Tinted `error`, which is the same colour role the confirming button in the sheet uses — so the
 * thing the user taps and the thing they are then asked about are visibly the same subject.
 *
 * @param onDelete the user tapped it. It asks; it does not delete.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmDeleteAffordance(
    onDelete: () -> Unit = {},
) {
    Icon(
        imageVector = Icons.Rounded.Delete,
        // Not null. Without it, the row's only destructive control is an unnamed button to anybody
        // using a screen reader, and "button" is not an instruction.
        //
        // Deliberately not `delete_this_alarm` — that string is the confirmation sheet's *question*
        // ("Delete this alarm?"), and a screen reader reading a question where an action name
        // belongs ("Delete this alarm?, button") reads like the control is confused about what it
        // does. This is the action; the sheet is where the question belongs.
        contentDescription = stringResource(R.string.delete_alarm),
        tint = MaterialTheme.colorScheme.error,
        modifier = Modifier
            .size(48.dp)
            // Clipped to a circle first, so the ripple is a neat disc inside the row rather than a
            // square flash fighting the card's own rounded ripple.
            .clip(shape = CircleShape)
            .clickable(
                onClickLabel = stringResource(R.string.delete_alarm),
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
                onClick = onDelete,
            )
            // After the click, so the padding is inside the tap target: 48dp of target, 24dp of icon.
            .padding(12.dp),
    )
}

@Preview(name = "Alarm row - on", widthDp = 360, heightDp = 160)
@Composable
private fun AlarmRowPreview() {
    MyApplicationTheme {
        AlarmRow(
            alarm = Alarm(
                id = 1L,
                message = "Take the bread out of the freezer",
                hourOfDay = 8,
                minute = 0,
                enabled = true,
                createdAt = 1_773_000_000_000L,
            ),
            onClick = {},
        )
    }
}

/**
 * The same row with the alarm switched off — the state the switch's colours exist for.
 *
 * Worth its own picture rather than being folded into the one above: "on" and "off" differ only in
 * the switch and in how bright the time is, and both of those are exactly the kind of difference
 * that can quietly stop being visible.
 */
@Preview(name = "Alarm row - off", widthDp = 360, heightDp = 160)
@Composable
private fun AlarmRowDisabledPreview() {
    MyApplicationTheme {
        AlarmRow(
            alarm = Alarm(
                id = 2L,
                message = "Leave for the dentist",
                hourOfDay = 14,
                minute = 5,
                enabled = false,
                createdAt = 1_772_950_000_000L,
            ),
            onClick = {},
        )
    }
}

/** The case the `maxLines` is there for: a message long enough to crowd the time out of the row. */
@Preview(name = "Alarm row - long message", widthDp = 360, heightDp = 180)
@Composable
private fun AlarmRowLongMessagePreview() {
    MyApplicationTheme {
        AlarmRow(
            alarm = Alarm(
                id = 3L,
                message = "Call the dentist about moving next Tuesday's appointment, and ask whether " +
                    "the other one in April can be moved to the same week so there is only one trip.",
                hourOfDay = 21,
                minute = 30,
                createdAt = 1_772_900_000_000L,
            ),
            onClick = {},
        )
    }
}
