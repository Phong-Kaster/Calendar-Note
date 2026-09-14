@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.skeleton.ui.fragment.alarm_editor.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.enums.AlarmRepeatMode
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle
import java.time.DayOfWeek

/*
 * --- Why the time control is handed its numbers exactly once (simple story) ---
 *
 * `rememberTimePickerState(initialHour = …, initialMinute = …)` means what it says: those are
 * *initial* values. The state is remembered across recompositions, so changing the parameters later
 * does not move the control — the user would see 08:00 on an alarm that is set for 21:30, and the
 * wrong time is what a save would then store.
 *
 * That is fine here, and it is fine only because of an arrangement made one layer up: the screen
 * draws a spinner instead of this editor while an existing alarm is being read, so the very first
 * composition already has the real numbers. See `AlarmEditorUiState.isLoading`.
 *
 * The other direction is a `LaunchedEffect` below, which reports every change the user makes back
 * out. The state is only ever *read* here, never written to — writing into it would be a second
 * source of truth for the same two numbers.
 */

/**
 * The editing surface of the alarm screen: what the alarm says, and when it goes off.
 *
 * **It scrolls, and `imePadding()` comes before the scroll on purpose.** This app is edge-to-edge
 * (`MainActivity.enableEdgeToEdge()`, and `CoreLayout` zeroes its own `contentWindowInsets`), so the
 * window is *not* resized when the keyboard appears — without that line the viewport stays
 * full-screen height, the keyboard covers the bottom of it, and no amount of scrolling can lift the
 * covered strip into view. It has to shrink the viewport, not the content inside it. Exactly the
 * same reasoning as `NoteEditor`, where the mistake was found.
 *
 * The message field is a `BasicTextField` rather than Material's `TextField`, matching the rest of
 * this app: Material's version brings its own container, label and indicator line, all coloured from
 * roles this dark theme would then have to fight.
 *
 * @param message the reminder text being edited.
 * @param hourOfDay the hour the alarm is set for, 0–23. Read at first composition; see the note
 *   above.
 * @param minute the minute past the hour, 0–59. Read at first composition.
 * @param repeatMode whether the alarm fires once, every day, or only on [repeatDays].
 * @param repeatDays which weekdays are ticked. Drawn only while [repeatMode] is
 *   [AlarmRepeatMode.CUSTOM].
 * @param onMessageChange called on every keystroke in the message field.
 * @param onTimeChange called whenever the picked time changes.
 * @param onRepeatModeChange called when the user picks a different repeat option.
 * @param onToggleRepeatDay called when the user taps one weekday in the Custom row.
 * @param modifier applied to the scrolling container.
 * @author Phong-Kaster
 */
@Composable
fun AlarmEditor(
    message: String,
    hourOfDay: Int,
    minute: Int,
    repeatMode: AlarmRepeatMode = AlarmRepeatMode.DAILY,
    repeatDays: Set<DayOfWeek> = emptySet(),
    onMessageChange: (String) -> Unit = {},
    onTimeChange: (hourOfDay: Int, minute: Int) -> Unit = { _, _ -> },
    onRepeatModeChange: (AlarmRepeatMode) -> Unit = {},
    onToggleRepeatDay: (DayOfWeek) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    // `is24Hour` is deliberately left at its default, which follows the *device's* clock setting.
    // That is the one piece of time presentation that belongs to the phone rather than to this
    // app's language picker: a user who reads German but keeps a 12-hour clock still wants a
    // 12-hour clock here.
    val timePickerState = rememberTimePickerState(
        initialHour = hourOfDay,
        initialMinute = minute,
    )

    LaunchedEffect(timePickerState.hour, timePickerState.minute) {
        onTimeChange(timePickerState.hour, timePickerState.minute)
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(state = rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AlarmMessageField(
            value = message,
            hint = stringResource(R.string.what_is_this_alarm_for),
            onValueChange = onMessageChange,
            textStyle = customizedTextStyle(fontSize = 18, fontWeight = 600),
        )

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // A label over the control, because a bare pair of number boxes does not say what it is
        // for on a screen that also has a text field.
        Text(
            text = stringResource(R.string.time),
            style = customizedTextStyle(
                fontSize = 12,
                fontWeight = 500,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        TimeInput(state = timePickerState)

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Text(
            text = stringResource(R.string.repeat),
            style = customizedTextStyle(
                fontSize = 12,
                fontWeight = 500,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )

        AlarmRepeatModeRow(
            repeatMode = repeatMode,
            onRepeatModeChange = onRepeatModeChange,
        )

        // Drawn only under Custom: the row of weekday toggles means nothing under Every day or
        // One-time, and showing it regardless would let a user tick days that are then silently
        // ignored — the same trap a disabled-but-visible control always is.
        if (repeatMode == AlarmRepeatMode.CUSTOM) {
            AlarmRepeatWeekdaysRow(
                repeatDays = repeatDays,
                onToggleDay = onToggleRepeatDay,
            )
        }
    }
}

/**
 * The three ways an alarm can repeat, drawn as one row of equally-sized pills.
 *
 * @param repeatMode which option is currently picked.
 * @param onRepeatModeChange the user tapped a different option.
 * @param modifier applied to the row.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmRepeatModeRow(
    repeatMode: AlarmRepeatMode,
    onRepeatModeChange: (AlarmRepeatMode) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        REPEAT_MODE_OPTIONS.forEach { (mode, labelId) ->
            val selected = mode == repeatMode
            val label = stringResource(labelId)
            val optionModifier = if (selected) {
                Modifier.background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp),
                )
            } else {
                Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = RoundedCornerShape(12.dp),
                )
            }

            Text(
                text = label,
                textAlign = TextAlign.Center,
                maxLines = 1,
                style = customizedTextStyle(
                    fontSize = 13,
                    fontWeight = 600,
                    color = if (selected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                ),
                modifier = Modifier
                    .weight(1f)
                    .then(optionModifier)
                    .clickable(
                        onClickLabel = label,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = true),
                        onClick = { onRepeatModeChange(mode) },
                    )
                    .padding(vertical = 10.dp),
            )
        }
    }
}

/**
 * The seven weekdays a `CUSTOM` alarm can repeat on, one round toggle each, Sunday first — the same
 * order the calendar grid's own column headers use.
 *
 * @param repeatDays which days are currently ticked.
 * @param onToggleDay the user tapped one day.
 * @param modifier applied to the row.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmRepeatWeekdaysRow(
    repeatDays: Set<DayOfWeek>,
    onToggleDay: (DayOfWeek) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        WEEKDAY_OPTIONS.forEach { (day, labelId) ->
            val selected = day in repeatDays
            val label = stringResource(labelId)
            val optionModifier = if (selected) {
                Modifier.background(color = MaterialTheme.colorScheme.primary, shape = CircleShape)
            } else {
                Modifier.border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape,
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .weight(1f)
                    .aspectRatio(1f)
                    .then(optionModifier)
                    .clickable(
                        onClickLabel = label,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = ripple(bounded = false),
                        onClick = { onToggleDay(day) },
                    ),
            ) {
                Text(
                    text = label,
                    maxLines = 1,
                    style = customizedTextStyle(
                        fontSize = 12,
                        fontWeight = 600,
                        color = if (selected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    ),
                )
            }
        }
    }
}

/** [AlarmRepeatMode] paired with the label each option shows, in the order they are drawn. */
private val REPEAT_MODE_OPTIONS: List<Pair<AlarmRepeatMode, Int>> = listOf(
    AlarmRepeatMode.DAILY to R.string.every_day,
    AlarmRepeatMode.ONE_TIME to R.string.one_time,
    AlarmRepeatMode.CUSTOM to R.string.custom,
)

/** Every [DayOfWeek] paired with its short label, Sunday first — see [AlarmRepeatWeekdaysRow]. */
private val WEEKDAY_OPTIONS: List<Pair<DayOfWeek, Int>> = listOf(
    DayOfWeek.SUNDAY to R.string.weekday_short_sun,
    DayOfWeek.MONDAY to R.string.weekday_short_mon,
    DayOfWeek.TUESDAY to R.string.weekday_short_tue,
    DayOfWeek.WEDNESDAY to R.string.weekday_short_wed,
    DayOfWeek.THURSDAY to R.string.weekday_short_thu,
    DayOfWeek.FRIDAY to R.string.weekday_short_fri,
    DayOfWeek.SATURDAY to R.string.weekday_short_sat,
)

/**
 * The one field of the editor: what this alarm is about.
 *
 * @param value the text to show.
 * @param hint what to draw while [value] is empty. Without it an empty field is an unlabelled blank
 *   area, and nobody can tell what it wants.
 * @param textStyle how the text itself is drawn.
 * @param onValueChange called on every keystroke.
 * @param modifier applied to the field.
 * @author Phong-Kaster
 */
@Composable
private fun AlarmMessageField(
    value: String,
    hint: String,
    textStyle: TextStyle,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        textStyle = textStyle,
        // Not single-line: a reminder is usually short, but one that runs past the width of the
        // screen should wrap onto a second line rather than scroll sideways out of sight.
        singleLine = false,
        cursorBrush = SolidColor(value = MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
        decorationBox = { innerTextField ->
            if (value.isEmpty()) {
                Text(
                    text = hint,
                    style = textStyle.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                )
            }
            innerTextField()
        },
    )
}

@Preview(name = "Alarm editor - empty", widthDp = 360, heightDp = 480)
@Composable
private fun AlarmEditorEmptyPreview() {
    MyApplicationTheme {
        AlarmEditor(
            message = "",
            hourOfDay = 8,
            minute = 0,
        )
    }
}

@Preview(name = "Alarm editor - written", widthDp = 360, heightDp = 480)
@Composable
private fun AlarmEditorWrittenPreview() {
    MyApplicationTheme {
        AlarmEditor(
            message = "Take the bread out of the freezer",
            hourOfDay = 21,
            minute = 30,
        )
    }
}

@Preview(name = "Alarm editor - custom repeat", widthDp = 360, heightDp = 520)
@Composable
private fun AlarmEditorCustomRepeatPreview() {
    MyApplicationTheme {
        AlarmEditor(
            message = "Take the bins out",
            hourOfDay = 7,
            minute = 0,
            repeatMode = AlarmRepeatMode.CUSTOM,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.THURSDAY),
        )
    }
}
