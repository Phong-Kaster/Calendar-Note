package com.example.skeleton.data.repository.impl

import com.example.skeleton.data.database.local.dao.TaskDao
import com.example.skeleton.data.mapper.toDomain
import com.example.skeleton.data.mapper.toEntity
import com.example.skeleton.domain.model.Task
import com.example.skeleton.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Room-only implementation of [TaskRepository].
 *
 * @author Phong-Kaster
 */
class TaskRepositoryImpl(
    private val dao: TaskDao,
) : TaskRepository {

    override val tasksFlow: Flow<List<Task>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override suspend fun addTask(title: String) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) return

        val task = Task(
            id = 0,
            title = trimmedTitle,
            isDone = false,
            createdAt = System.currentTimeMillis(),
        )
        dao.upsert(task.toEntity())
    }

    override suspend fun setDone(id: Long, done: Boolean) {
        dao.setDone(id, done)
    }

    override suspend fun deleteTask(id: Long) {
        dao.deleteTask(id)
    }
}
