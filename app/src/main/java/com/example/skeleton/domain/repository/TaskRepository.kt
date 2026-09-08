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

    /**
     * Marks a task as done or not done.
     *
     * @param id The task's unique identifier.
     * @param done True to mark complete; false to mark incomplete.
     */
    suspend fun setDone(id: Long, done: Boolean)

    /**
     * Removes a task by its [id].
     *
     * If the task does not exist, this is a no-op (no exception).
     *
     * @param id The task's unique identifier.
     */
    suspend fun deleteTask(id: Long)
}
