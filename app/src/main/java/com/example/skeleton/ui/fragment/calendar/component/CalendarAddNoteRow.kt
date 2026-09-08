package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R

/**
 * Title input + add button for creating a note on the selected date.
 *
 * Typing a title and tapping the button (or the field's "done" IME action) calls [onAddNote]
 * with the typed title, then clears the field — unless the title is blank/whitespace-only, in
 * which case nothing is cleared or submitted. Both the field and the button are disabled while
 * [enabled] is false (i.e. no date selected yet).
 *
 * @param enabled True when a date is selected and adding a note is allowed.
 * @param onAddNote Called with the typed (untrimmed) title when the user submits.
 * @author Phong-Kaster
 */
@Composable
fun CalendarAddNoteRow(
    enabled: Boolean = true,
    onAddNote: (String) -> Unit = {},
) {
    var text by remember { mutableStateOf("") }

    fun submit() {
        if (text.isBlank()) return
        onAddNote(text)
        text = ""
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = { newValue -> text = newValue },
            enabled = enabled,
            label = { Text(text = stringResource(R.string.note_title)) },
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )

        Button(
            enabled = enabled,
            onClick = { submit() },
        ) {
            Text(text = stringResource(R.string.add))
        }
    }
}

@Preview(name = "Enabled")
@Composable
private fun CalendarAddNoteRowEnabledPreview() {
    CalendarAddNoteRow(enabled = true)
}

@Preview(name = "Disabled")
@Composable
private fun CalendarAddNoteRowDisabledPreview() {
    CalendarAddNoteRow(enabled = false)
}
