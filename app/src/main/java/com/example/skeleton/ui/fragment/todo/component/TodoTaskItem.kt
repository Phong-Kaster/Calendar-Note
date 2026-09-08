package com.example.skeleton.ui.fragment.todo.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.domain.model.Task
import com.example.skeleton.ui.theme.customizedTextStyle

/**
 * One row of the to-do list: the task's title and a read-only checkbox reflecting
 * [Task.isDone]. Toggling/deleting a task is not wired here yet — that comes later.
 *
 * @author Phong-Kaster
 */
@Composable
fun TodoTaskItem(task: Task) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Checkbox(
            checked = task.isDone,
            onCheckedChange = null,
        )

        Text(
            text = task.title,
            style = customizedTextStyle(
                fontSize = 16,
                fontWeight = 400,
                color = Color.White,
            ),
            maxLines = 1,
        )
    }
}

@Preview
@Composable
private fun TodoTaskItemPreview() {
    TodoTaskItem(
        task = Task(id = 1, title = "Buy groceries", isDone = false, createdAt = 0L),
    )
}
