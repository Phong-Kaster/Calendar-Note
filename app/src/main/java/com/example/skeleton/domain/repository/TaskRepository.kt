package com.example.skeleton.domain.repository

import com.example.skeleton.domain.model.Task
import kotlinx.coroutines.flow.Flow

/**
 * I keep track of the user's to-do tasks.
 *
 * @author Phong-Kaster
 */
interface TaskRepository {

    /** All tasks, newest first. */
    val tasksFlow: Flow<List<Task>>

    /**
     * Adds a new task with [title].
     *
     * A blank or whitespace-only title is silently ignored (no exception, no error state) —
     * I just do nothing in that case, like a kid who won't write down an empty sticky note.
     */
    suspend fun addTask(title: String)
}
