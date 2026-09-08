package com.example.skeleton.ui.fragment.calendar.component

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import com.example.skeleton.R
import com.example.skeleton.domain.model.Note

/**
 * A confirmation dialog to rename [note]. Renders nothing when [note] is null — the caller
 * decides when a note is being edited by passing a non-null value.
 *
 * @param note The note being edited; null hides the dialog.
 * @param onConfirm Called with the new (unsaved) title when the user taps "Save".
 * @param onDismiss Called when the user taps "Cancel" or dismisses the dialog.
 * @author Phong-Kaster
 */
@Composable
fun CalendarEditNoteDialog(
    note: Note?,
    onConfirm: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    if (note == null) return

    var text by remember(note.id) { mutableStateOf(note.title) }

    fun submit() {
        if (text.isBlank()) return
        onConfirm(text)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.edit_note)) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { newValue -> text = newValue },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(onDone = { submit() }),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    submit()
                },
            ) {
                Text(text = stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(text = stringResource(R.string.cancel))
            }
        },
    )
}

@Preview
@Composable
private fun CalendarEditNoteDialogPreview() {
    CalendarEditNoteDialog(
        note = Note(id = 1, epochDay = 0, title = "Dentist appointment", createdAt = 0),
    )
}
