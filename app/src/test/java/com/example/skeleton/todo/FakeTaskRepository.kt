package com.example.skeleton.todo

import com.example.skeleton.domain.model.Task
import com.example.skeleton.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * In-memory [TaskRepository] used by tests — no Room, no Android framework.
 *
 * @author Phong-Kaster
 */
class FakeTaskRepository : TaskRepository {

    private val backing = MutableStateFlow<List<Task>>(emptyList())
    override val tasksFlow: Flow<List<Task>> = backing.asStateFlow()

    private var nextId = 1L

    override suspend fun addTask(title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val task = Task(
            id = nextId++,
            title = trimmedTitle,
            isDone = false,
            createdAt = System.currentTimeMillis(),
        )
        backing.value = listOf(task) + backing.value
    }

    override suspend fun setDone(id: Long, done: Boolean) {
        backing.value = backing.value.map { task ->
            if (task.id == id) task.copy(isDone = done) else task
        }
    }

    override suspend fun deleteTask(id: Long) {
        backing.value = backing.value.filter { it.id != id }
    }
}
