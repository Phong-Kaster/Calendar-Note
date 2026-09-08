package com.example.skeleton.ui.fragment.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
}
