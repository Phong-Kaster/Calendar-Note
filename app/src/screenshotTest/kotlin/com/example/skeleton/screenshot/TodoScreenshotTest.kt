package com.example.skeleton.screenshot

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.android.tools.screenshot.PreviewTest
import com.example.skeleton.domain.model.Task
import com.example.skeleton.ui.component.CoreConfirmDialog
import com.example.skeleton.ui.fragment.todo.component.TodoAddTaskRow
import com.example.skeleton.ui.fragment.todo.component.TodoEmptyState
import com.example.skeleton.ui.fragment.todo.component.TodoTaskItem

/**
 * Screenshot coverage for the To-do screen's components.
 *
 * The task row previously painted `Color.White` unconditionally. It looked correct in Studio's
 * preview pane and in the app, for the same accidental reason — a black ground underneath — and
 * would have gone unreadable the moment a row was drawn on anything lighter. Rendering through
 * [ScreenshotScaffold] pins the row against the theme instead of against luck.
 *
 * @author Phong-Kaster
 */

private const val LONG_TITLE =
    "Call the dentist about rescheduling next week's appointment to the afternoon"

@PreviewTest
@Preview(name = "Task item - not done", widthDp = 360, heightDp = 100)
@Composable
private fun TaskItemNotDone() {
    ScreenshotScaffold {
        TodoTaskItem(task = Task(id = 1, title = "Buy groceries", isDone = false, createdAt = 0L))
    }
}

/** Done rows are struck through and dimmed; the dimming must not erase them. */
@PreviewTest
@Preview(name = "Task item - done", widthDp = 360, heightDp = 100)
@Composable
private fun TaskItemDone() {
    ScreenshotScaffold {
        TodoTaskItem(task = Task(id = 1, title = "Buy groceries", isDone = true, createdAt = 0L))
    }
}

/** A title long enough to wrap used to truncate at one line with no way to read the rest. */
@PreviewTest
@Preview(name = "Task item - long title", widthDp = 360, heightDp = 140)
@Composable
private fun TaskItemLongTitle() {
    ScreenshotScaffold {
        TodoTaskItem(task = Task(id = 1, title = LONG_TITLE, isDone = false, createdAt = 0L))
    }
}

@PreviewTest
@Preview(name = "Add task row", widthDp = 360, heightDp = 120)
@Composable
private fun AddTaskRow() {
    ScreenshotScaffold {
        TodoAddTaskRow()
    }
}

/** A fresh install used to open on a bare field above a blank area. */
@PreviewTest
@Preview(name = "Empty state", widthDp = 360, heightDp = 200)
@Composable
private fun EmptyState() {
    ScreenshotScaffold {
        TodoEmptyState()
    }
}

/** The confirm button is error-tinted so the two buttons do not look equally safe to tap. */
@PreviewTest
@Preview(name = "Delete confirmation", widthDp = 360, heightDp = 260)
@Composable
private fun DeleteConfirmation() {
    ScreenshotScaffold {
        CoreConfirmDialog(
            visible = true,
            title = "Delete this task?",
        )
    }
}
