package com.example.skeleton.todo

import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TaskToggleAndDeleteTest {

    @Test
    fun `toggling a task done then not-done then done again works correctly`() = runBlocking {
        val repository = FakeTaskRepository()
        repository.addTask("Test task")

        val allTasks = repository.tasksFlow.first()
        assertEquals(1, allTasks.size)
        val taskId = allTasks[0].id
        assertFalse(allTasks[0].isDone)

        // Toggle to done
        repository.setDone(taskId, true)
        var tasks = repository.tasksFlow.first()
        assertTrue(tasks[0].isDone)

        // Toggle back to not done
        repository.setDone(taskId, false)
        tasks = repository.tasksFlow.first()
        assertFalse(tasks[0].isDone)

        // Toggle to done again
        repository.setDone(taskId, true)
        tasks = repository.tasksFlow.first()
        assertTrue(tasks[0].isDone)
    }

    @Test
    fun `deleting a task removes it from the list`() = runBlocking {
        val repository = FakeTaskRepository()
        repository.addTask("Task 1")
        repository.addTask("Task 2")

        var tasks = repository.tasksFlow.first()
        assertEquals(2, tasks.size)
        val taskIdToDelete = tasks[1].id  // Second task (oldest, since we insert at front)

        repository.deleteTask(taskIdToDelete)
        tasks = repository.tasksFlow.first()
        assertEquals(1, tasks.size)
        assertEquals("Task 2", tasks[0].title)
    }

    @Test
    fun `deleting a non-existent task is a no-op`() = runBlocking {
        val repository = FakeTaskRepository()
        repository.addTask("Task 1")

        val tasks = repository.tasksFlow.first()
        assertEquals(1, tasks.size)

        // Delete a non-existent ID
        repository.deleteTask(999L)

        val tasksAfter = repository.tasksFlow.first()
        assertEquals(1, tasksAfter.size)
        assertEquals("Task 1", tasksAfter[0].title)
    }
}
