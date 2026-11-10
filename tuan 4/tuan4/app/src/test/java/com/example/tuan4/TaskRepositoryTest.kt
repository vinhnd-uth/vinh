package com.example.tuan4

import com.example.tuan4.data.model.Task
import com.example.tuan4.data.repository.TaskRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class TaskRepositoryTest {

    private lateinit var repository: TaskRepository

    @Before
    fun setUp() {
        repository = TaskRepository()
    }

    @Test
    fun testInitialMockTasksLoaded() {
        val tasks = repository.tasks.value
        assertTrue("Initial tasks list should not be empty", tasks.isNotEmpty())
        assertEquals(6, tasks.size)
    }

    @Test
    fun testToggleTaskDone() = runBlocking {
        val initialTask = repository.tasks.value.find { it.id == 2 }
        assertNotNull(initialTask)
        assertFalse(initialTask!!.done)

        repository.toggleTaskDone(2)

        val updatedTask = repository.tasks.value.find { it.id == 2 }
        assertNotNull(updatedTask)
        assertTrue(updatedTask!!.done)
    }

    @Test
    fun testAddTask() = runBlocking {
        val initialSize = repository.tasks.value.size
        val newTask = Task(
            id = 0,
            title = "Test Task",
            note = "Test Note",
            category = "study",
            date = "2026-10-20",
            time = "10:00",
            priority = "high",
            remind = true,
            done = false
        )

        repository.addTask(newTask)

        val updatedTasks = repository.tasks.value
        assertEquals(initialSize + 1, updatedTasks.size)
        val added = updatedTasks.find { it.title == "Test Task" }
        assertNotNull(added)
        assertEquals("Học tập", added!!.categoryDisplayName)
    }

    @Test
    fun testAddAndToggleSubtask() {
        val taskId = 2
        repository.addSubtask(taskId, "Test Subtask")

        val task = repository.tasks.value.find { it.id == taskId }
        assertNotNull(task)
        val subtask = task!!.subtasks.find { it.title == "Test Subtask" }
        assertNotNull(subtask)
        assertFalse(subtask!!.completed)

        repository.toggleSubtaskCompleted(taskId, subtask.id)

        val updatedTask = repository.tasks.value.find { it.id == taskId }
        val updatedSubtask = updatedTask!!.subtasks.find { it.id == subtask.id }
        assertTrue(updatedSubtask!!.completed)
    }

    @Test
    fun testDeleteTask() = runBlocking {
        repository.deleteTask(4)
        val task = repository.tasks.value.find { it.id == 4 }
        assertTrue(task == null)
    }
}
