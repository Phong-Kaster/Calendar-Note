package com.example.skeleton.ui.fragment.todo

import com.example.skeleton.domain.model.Task

/**
 * UI state for the To-do screen.
 *
 * @param tasks All tasks, newest first.
 * @param editingTask The task currently being renamed; null when the edit dialog is hidden.
 * @param pendingDeleteTaskId The task awaiting delete confirmation; null when no confirm dialog
 * should be shown. Deletion is irreversible, so it is a two-step action.
 * @author Phong-Kaster
 */
data class TodoUiState(
    val tasks: List<Task> = emptyList(),
    val editingTask: Task? = null,
    val pendingDeleteTaskId: Long? = null,
)
