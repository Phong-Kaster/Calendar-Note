package com.example.skeleton.ui.fragment.todo.component

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
import com.example.skeleton.domain.model.Task

/**
 * A dialog for renaming an existing task. Shows nothing when [task] is null; otherwise
 * shows a Material3 [AlertDialog] with a text field pre-filled with the task's current
 * title, and Save/Cancel buttons.
 *
 * A blank or whitespace-only title is not saved — the same rule the rest of the to-do
 * screen already uses for adding a task.
 *
 * @param task The task being edited; null hides the dialog.
 * @param onConfirm Called with the new title when the user taps Save.
 * @param onDismiss Called when the user taps Cancel or dismisses the dialog.
 * @author Phong-Kaster
 */
@Composable
fun TodoEditTaskDialog(
    task: Task?,
    onConfirm: (String) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    if (task == null) return

    var text by remember(task.id) { mutableStateOf(task.title) }

    fun submit() {
        if (text.isBlank()) return
        onConfirm(text)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = stringResource(R.string.edit_task))
        },
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
                onClick = { submit() },
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
private fun TodoEditTaskDialogPreview() {
    TodoEditTaskDialog(
        task = Task(id = 1, title = "Buy groceries", isDone = false, createdAt = 0L),
    )
}
