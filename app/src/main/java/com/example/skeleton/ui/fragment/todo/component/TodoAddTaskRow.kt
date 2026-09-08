package com.example.skeleton.ui.fragment.todo.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import com.example.skeleton.R

/**
 * A text field plus an add button. Typing a title and tapping the button (or the field's
 * "done" IME action) calls [onAddTask] with the typed title, then clears the field — unless
 * the title is blank/whitespace-only, in which case nothing is cleared or submitted, so the
 * user's half-typed input isn't silently wiped.
 *
 * @author Phong-Kaster
 */
@Composable
fun TodoAddTaskRow(
    onAddTask: (String) -> Unit = {},
) {
    var text by remember { mutableStateOf("") }

    fun submit() {
        if (text.isBlank()) return
        onAddTask(text)
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
            modifier = Modifier.weight(1f),
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Sentences,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(onDone = { submit() }),
        )

        Button(
            onClick = { submit() },
        ) {
            Text(text = stringResource(R.string.add))
        }
    }
}

@Preview
@Composable
private fun TodoAddTaskRowPreview() {
    TodoAddTaskRow()
}
