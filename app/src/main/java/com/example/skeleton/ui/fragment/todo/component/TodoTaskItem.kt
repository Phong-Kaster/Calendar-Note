package com.example.skeleton.ui.fragment.todo.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Task
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * One row of the to-do list: a checkbox, the task's title, and edit/delete buttons, on a card so
 * consecutive tasks are visually separable.
 *
 * Done tasks render struck through and dimmed. The colours come from the theme rather than
 * hardcoded white: the row used to paint `Color.White` regardless of scheme, which only ever
 * worked because `CoreLayout` happens to paint a black ground — the moment anything drew this row
 * on a lighter surface (a card, a dialog, a preview) the text disappeared.
 *
 * @param task The task to display.
 * @param onToggle Called when the checkbox is toggled; receives task id and new done state.
 * @param onEdit Called with the tapped task when its edit button is pressed.
 * @param onDelete Called when the delete button is pressed; receives task id. The caller is
 * expected to confirm before destroying anything.
 * @author Phong-Kaster
 */
@Composable
fun TodoTaskItem(
    task: Task,
    onToggle: (Long, Boolean) -> Unit = { _, _ -> },
    onEdit: (Task) -> Unit = {},
    onDelete: (Long) -> Unit = {},
) {
    val baseColor = MaterialTheme.colorScheme.onSurface
    val textColor = if (task.isDone) baseColor.copy(alpha = 0.5f) else baseColor
    val textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(
                checked = task.isDone,
                onCheckedChange = { newState ->
                    onToggle(task.id, newState)
                },
            )

            Text(
                text = task.title,
                style = customizedTextStyle(
                    fontSize = 16,
                    fontWeight = 400,
                    color = textColor,
                ),
                textDecoration = textDecoration,
                // Two lines, not one: a task title long enough to wrap is common, and truncating
                // at one line left no way at all to read the rest.
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .padding(vertical = 12.dp),
            )

            IconButton(
                onClick = {
                    onEdit(task)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.edit_task),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = {
                    onDelete(task.id)
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = stringResource(R.string.delete_task),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(name = "Not Done")
@Composable
private fun TodoTaskItemNotDonePreview() {
    TodoTaskItem(
        task = Task(id = 1, title = "Buy groceries", isDone = false, createdAt = 0L),
    )
}

@Preview(name = "Done")
@Composable
private fun TodoTaskItemDonePreview() {
    TodoTaskItem(
        task = Task(id = 1, title = "Buy groceries", isDone = true, createdAt = 0L),
    )
}

@Preview(name = "Long title")
@Composable
private fun TodoTaskItemLongTitlePreview() {
    TodoTaskItem(
        task = Task(
            id = 1,
            title = "Call the dentist about rescheduling next week's appointment to the afternoon",
            isDone = false,
            createdAt = 0L,
        ),
    )
}
