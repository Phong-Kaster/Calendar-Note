package com.example.skeleton.ui.fragment.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skeleton.domain.model.Task
import com.example.skeleton.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Backs the To-do screen: keeps [uiState] in sync with [TaskRepository] and forwards
 * user actions (adding a task) back to it.
 *
 * @author Phong-Kaster
 */
class TodoViewModel(
    private val taskRepository: TaskRepository,
) : ViewModel() {

    private val TAG = "TodoViewModel"

    private val _uiState = MutableStateFlow(TodoUiState())
    val uiState = _uiState.asStateFlow()

    init {
        collectTasks()
    }

    private fun collectTasks() {
        viewModelScope.launch {
            taskRepository.tasksFlow.collectLatest { tasks ->
                _uiState.value = _uiState.value.copy(tasks = tasks)
            }
        }
    }

    /** Called from [TodoFragment] when the user submits a new task title. */
    fun addTask(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.addTask(title)
        }
    }

    /** Marks a task as done or not done. */
    fun setDone(id: Long, done: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.setDone(id, done)
        }
    }

    /** Removes a task by its id. */
    fun deleteTask(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            taskRepository.deleteTask(id)
        }
    }

    /** Opens the edit dialog for [task]. */
    fun startEditingTask(task: Task) {
        _uiState.value = _uiState.value.copy(editingTask = task)
    }

    /** Closes the edit dialog without saving. */
    fun cancelEditingTask() {
        _uiState.value = _uiState.value.copy(editingTask = null)
    }

    /** Saves [title] as the new title of the task currently being edited, then closes the dialog. */
    fun updateTaskTitle(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val editingTask = _uiState.value.editingTask ?: return@launch
            taskRepository.updateTitle(editingTask.id, title)
            _uiState.value = _uiState.value.copy(editingTask = null)
        }
    }
}
