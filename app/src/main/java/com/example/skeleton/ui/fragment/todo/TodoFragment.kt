package com.example.skeleton.ui.fragment.todo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.skeleton.ui.component.CoreTopBar
import com.example.skeleton.ui.fragment.todo.component.TodoAddTaskRow
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
        )
    }
}

/**
 * To-do screen UI: an add-task row above a list of tasks from [TodoUiState].
 */
@Composable
private fun TodoLayout(
    uiState: TodoUiState,
    onAddTask: (String) -> Unit = {},
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

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(
                        items = uiState.tasks,
                        key = { task -> task.id },
                    ) { task ->
                        TodoTaskItem(task = task)
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
