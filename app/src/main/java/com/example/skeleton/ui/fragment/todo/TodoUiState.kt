package com.example.skeleton.ui.fragment.todo

import com.example.skeleton.domain.model.Task

/**
 * UI state for the To-do screen.
 *
 * @param tasks All tasks, newest first.
 * @author Phong-Kaster
 */
data class TodoUiState(
    val tasks: List<Task> = emptyList(),
)
