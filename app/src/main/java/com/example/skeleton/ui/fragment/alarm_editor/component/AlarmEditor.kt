@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.skeleton.ui.fragment.alarm_editor.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.ui.theme.MyApplicationTheme
import com.example.skeleton.ui.theme.customizedTextStyle

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
 * @param onMessageChange called on every keystroke in the message field.
 * @param onTimeChange called whenever the picked time changes.
 * @param modifier applied to the scrolling container.
 * @author Phong-Kaster
 */
@Composable
fun AlarmEditor(
    message: String,
    hourOfDay: Int,
    minute: Int,
    onMessageChange: (String) -> Unit = {},
    onTimeChange: (hourOfDay: Int, minute: Int) -> Unit = { _, _ -> },
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
    }
}

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

@Preview(name = "Alarm editor - empty", widthDp = 360, heightDp = 320)
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

@Preview(name = "Alarm editor - written", widthDp = 360, heightDp = 320)
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
