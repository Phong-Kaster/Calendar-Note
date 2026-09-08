package com.example.skeleton.todo

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Verifies the create-then-read contract of [TaskRepository][com.example.skeleton.domain.repository.TaskRepository]
 * using [FakeTaskRepository]: adding a task makes it observable, and blank titles are ignored.
 *
 * @author Phong-Kaster
 */
class TaskRepositoryCreateReadTest {

    @Test
    fun `adding a task makes it observable via tasksFlow`() = runBlocking {
        val repository = FakeTaskRepository()

        repository.addTask("Buy groceries")

        val tasks = repository.tasksFlow.first()
        assertEquals(1, tasks.size)
        assertEquals("Buy groceries", tasks.first().title)
    }

    @Test
    fun `adding a blank title does not add anything`() = runBlocking {
        val repository = FakeTaskRepository()

        repository.addTask("   ")

        val tasks = repository.tasksFlow.first()
        assertTrue(tasks.isEmpty())
    }
}
