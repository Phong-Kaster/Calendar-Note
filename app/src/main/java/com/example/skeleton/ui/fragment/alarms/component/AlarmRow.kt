package com.example.skeleton.ui.fragment.alarms.component

import android.text.format.DateFormat
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
 * One alarm as a list row: the time it goes off, and what it says.
 *
 * **The time is built by hand rather than formatted**, and that is a deliberate simplification. A
 * `DateTimeFormatter` would need a locale, and the right locale here is the *app's* one
 * (`LocalConfiguration.current.locales[0]`) rather than the device's — that is a live trap in this
 * codebase, because `Locale.getDefault()` answers for the phone and this app has its own language
 * picker in Settings. Two digits, a colon, two digits needs no locale at all, so the trap has
 * nothing to catch. It also means this row renders identically on every machine, which is what lets
 * a screenshot test photograph it. The cost is that a 12-hour user reads 21:30 rather than 9:30 PM;
 * the task that adds the enabled switch is the place to revisit that, with the locale done properly.
 *
 * The message stops after two lines with an ellipsis rather than growing to fit. Without that, one
 * long reminder pushes every other alarm off the screen — and pushes the time, which is the thing
 * the user is scanning for, away from its neighbours.
 *
 * The whole card is the tap target, not the text inside it. A row is what the user sees as one
 * thing, and a tap that only counts when it lands on the text is a row that seems to ignore half the
 * taps aimed at it.
 *
 * @param alarm the alarm to draw.
 * @param onClick the user tapped this row.
 * @param modifier applied to the card.
 * @author Phong-Kaster
 */
@Composable
fun AlarmRow(
    alarm: Alarm,
    onClick: () -> Unit,
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
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // One deliberate deviation from the house rule that single-line text gets
        // `Modifier.basicMarquee(...)`: a marquee animates, and "08:00" cannot overflow a row this
        // wide, so the only thing a marquee could do here is move something that should be still.
        Text(
            text = displayTime,
            style = customizedTextStyle(fontSize = 24, fontWeight = 600),
            maxLines = 1,
        )

        Text(
            text = alarm.message,
            style = customizedTextStyle(
                fontSize = 14,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Preview(name = "Alarm row - short message", widthDp = 360)
@Composable
private fun AlarmRowPreview() {
    MyApplicationTheme {
        AlarmRow(
            alarm = Alarm(
                id = 1L,
                message = "Take the bread out of the freezer",
                hourOfDay = 8,
                minute = 0,
                createdAt = 1_773_000_000_000L,
            ),
            onClick = {},
        )
    }
}

/** The case the `maxLines` is there for: a message long enough to crowd the time out of the row. */
@Preview(name = "Alarm row - long message", widthDp = 360)
@Composable
private fun AlarmRowLongMessagePreview() {
    MyApplicationTheme {
        AlarmRow(
            alarm = Alarm(
                id = 2L,
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
