package com.example.skeleton.ui.fragment.todo.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.domain.model.Task
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * One row of the to-do list: the task's title, a checkbox to toggle done/not-done,
 * and a delete button. Done tasks render with strike-through and reduced opacity.
 *
 * @param task The task to display.
 * @param onToggle Called when the checkbox is toggled; receives task id and new done state.
 * @param onEdit Called with the tapped task when its edit button is pressed.
 * @param onDelete Called when the delete button is pressed; receives task id.
 * @author Phong-Kaster
 */
@Composable
fun TodoTaskItem(
    task: Task,
    onToggle: (Long, Boolean) -> Unit = { _, _ -> },
    onEdit: (Task) -> Unit = {},
    onDelete: (Long) -> Unit = {},
) {
    val textColor = if (task.isDone) Color.White.copy(alpha = 0.5f) else Color.White
    val textDecoration = if (task.isDone) TextDecoration.LineThrough else TextDecoration.None

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
            maxLines = 1,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 8.dp),
        )

        IconButton(
            onClick = {
                onEdit(task)
            },
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.edit_task),
                tint = Color.White,
            )
        }

        IconButton(
            onClick = {
                onDelete(task.id)
            },
            modifier = Modifier.padding(end = 8.dp),
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = stringResource(R.string.delete_task),
                tint = Color.White,
            )
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
