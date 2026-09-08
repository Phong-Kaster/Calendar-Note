package com.example.skeleton.todo

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskEditTitleTest {

    @Test
    fun `renaming a task persists the new title`() = runBlocking {
        val repository = FakeTaskRepository()
        repository.addTask("Old title")

        val allTasks = repository.tasksFlow.first()
        assertEquals(1, allTasks.size)
        val taskId = allTasks[0].id

        repository.updateTitle(taskId, "New title")

        val tasks = repository.tasksFlow.first()
        assertEquals("New title", tasks[0].title)
    }

    @Test
    fun `renaming to a blank title is rejected`() = runBlocking {
        val repository = FakeTaskRepository()
        repository.addTask("Original title")

        val allTasks = repository.tasksFlow.first()
        val taskId = allTasks[0].id

        repository.updateTitle(taskId, "   ")

        val tasks = repository.tasksFlow.first()
        assertEquals("Original title", tasks[0].title)
    }

    @Test
    fun `renaming an unknown id is a no-op`() = runBlocking {
        val repository = FakeTaskRepository()
        repository.addTask("Task 1")

        repository.updateTitle(999L, "Should not apply")

        val tasks = repository.tasksFlow.first()
        assertEquals(1, tasks.size)
        assertEquals("Task 1", tasks[0].title)
    }
}
