package com.example.skeleton.ui.fragment.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.skeleton.R
import com.example.skeleton.core.CoreFragment
import com.example.skeleton.core.CoreLayout
import com.example.skeleton.domain.model.Task
import com.example.skeleton.ui.component.CoreBottomBar
import com.example.skeleton.ui.component.CoreConfirmDialog
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.todo.component.TodoAddTaskRow
import com.example.skeleton.ui.fragment.todo.component.TodoEditTaskDialog
import com.example.skeleton.ui.fragment.todo.component.TodoEmptyState
import com.example.skeleton.ui.fragment.todo.component.TodoTaskItem
import org.koin.androidx.viewmodel.ext.android.viewModel

/**
 * To-do screen: add a task and see it in a persisted list.
 *
 * @author Phong-Kaster
 */
class TodoFragment : CoreFragment() {
    private val viewModel: TodoViewModel by viewModel()

    @Composable
    override fun ComposeView() {
        super.ComposeView()

        val uiState by viewModel.uiState.collectAsState()

        TodoLayout(
            uiState = uiState,
            onAddTask = { title -> viewModel.addTask(title) },
            onToggleTask = { id, done -> viewModel.setDone(id, done) },
            onEditTask = { task -> viewModel.startEditingTask(task) },
            onDeleteTask = { id -> viewModel.requestDeleteTask(id) },
        )

        TodoEditTaskDialog(
            task = uiState.editingTask,
            onConfirm = { title -> viewModel.updateTaskTitle(title) },
            onDismiss = { viewModel.cancelEditingTask() },
        )

        CoreConfirmDialog(
            visible = uiState.pendingDeleteTaskId != null,
            title = stringResource(R.string.delete_task_confirm_title),
            onConfirm = { viewModel.confirmDeleteTask() },
            onDismiss = { viewModel.cancelDeleteTask() },
        )
    }
}

/**
 * To-do screen UI: an add-task row above the list of tasks from [TodoUiState], or an invitation
 * to add the first one when there are none.
 *
 * The empty state matters more than it looks: a brand-new install previously opened on a text
 * field above a large blank area, which reads as "something failed to load" rather than
 * "there is nothing here yet".
 */
@Composable
private fun TodoLayout(
    uiState: TodoUiState,
    onAddTask: (String) -> Unit = {},
    onToggleTask: (Long, Boolean) -> Unit = { _, _ -> },
    onEditTask: (Task) -> Unit = {},
    onDeleteTask: (Long) -> Unit = {},
) {
    CoreLayout(
        topBar = { CoreTopBar(title = stringResource(R.string.todo)) },
        bottomBar = { CoreBottomBar() },
        content = {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
            ) {
                TodoAddTaskRow(onAddTask = onAddTask)

                if (uiState.tasks.isEmpty()) {
                    TodoEmptyState(modifier = Modifier.fillMaxSize())
                    return@Column
                }

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp),
                ) {
                    items(
                        items = uiState.tasks,
                        key = { task -> task.id },
                    ) { task ->
                        TodoTaskItem(
                            task = task,
                            onToggle = onToggleTask,
                            onEdit = onEditTask,
                            onDelete = onDeleteTask,
                        )
                    }
                }
            }
        },
    )
}

@Preview
@Composable
private fun TodoLayoutPreview() {
    TodoLayout(
        uiState = TodoUiState(
            tasks = listOf(
                Task(id = 1, title = "Buy groceries", isDone = false, createdAt = 0L),
                Task(id = 2, title = "Walk the dog", isDone = true, createdAt = 0L),
            ),
        ),
    )
}

@Preview(name = "Empty")
@Composable
private fun TodoLayoutEmptyPreview() {
    TodoLayout(uiState = TodoUiState())
}
