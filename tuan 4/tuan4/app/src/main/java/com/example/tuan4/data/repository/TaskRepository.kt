package com.example.tuan4.data.repository

import com.example.tuan4.data.model.CreateTaskRequest
import com.example.tuan4.data.model.Subtask
import com.example.tuan4.data.model.Task
import com.example.tuan4.data.remote.TaskApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TaskRepository(
    private val apiService: TaskApiService = TaskApiService.create()
) {
    private val _tasks = MutableStateFlow<List<Task>>(initialMockTasks())
    val tasks: StateFlow<List<Task>> = _tasks.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    suspend fun loadTasks() {
        _isLoading.value = true
        _errorMessage.value = null
        try {
            val remoteTasks = apiService.getTasks()
            if (remoteTasks.isNotEmpty()) {
                _tasks.value = remoteTasks
            }
        } catch (e: Exception) {
            _errorMessage.value = "Không thể kết nối API. Đã dùng dữ liệu mẫu."
            if (_tasks.value.isEmpty()) {
                _tasks.value = initialMockTasks()
            }
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun toggleTaskDone(taskId: Int) {
        val currentList = _tasks.value
        val target = currentList.find { it.id == taskId } ?: return
        val updatedDone = !target.done
        val updatedTask = target.copy(done = updatedDone)

        _tasks.value = currentList.map { if (it.id == taskId) updatedTask else it }

        try {
            apiService.updateTask(taskId, updatedTask)
        } catch (e: Exception) {
            // Keep local state
        }
    }

    suspend fun addTask(task: Task) {
        val nextId = (_tasks.value.maxOfOrNull { it.id } ?: 0) + 1
        val newTask = task.copy(id = nextId)

        _tasks.value = _tasks.value + newTask

        try {
            val createReq = CreateTaskRequest(
                title = newTask.title,
                note = newTask.note,
                category = newTask.category,
                date = newTask.date,
                time = newTask.time,
                priority = newTask.priority,
                remind = newTask.remind,
                done = newTask.done,
                subtasks = newTask.subtasks
            )
            apiService.createTask(createReq)
        } catch (e: Exception) {
            // Keep local state
        }
    }

    suspend fun updateTask(task: Task) {
        _tasks.value = _tasks.value.map { if (it.id == task.id) task else it }

        try {
            apiService.updateTask(task.id, task)
        } catch (e: Exception) {
            // Keep local state
        }
    }

    suspend fun deleteTask(taskId: Int) {
        _tasks.value = _tasks.value.filter { it.id != taskId }

        try {
            apiService.deleteTask(taskId)
        } catch (e: Exception) {
            // Keep local state
        }
    }

    fun toggleSubtaskCompleted(taskId: Int, subtaskId: Int) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                val updatedSubtasks = task.subtasks.map { subtask ->
                    if (subtask.id == subtaskId) {
                        subtask.copy(completed = !subtask.completed)
                    } else {
                        subtask
                    }
                }
                task.copy(subtasks = updatedSubtasks)
            } else {
                task
            }
        }
    }

    fun addSubtask(taskId: Int, subtaskTitle: String) {
        if (subtaskTitle.isBlank()) return
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                val nextSubId = (task.subtasks.maxOfOrNull { it.id } ?: 0) + 1
                val newSub = Subtask(id = nextSubId, title = subtaskTitle.trim(), completed = false)
                task.copy(subtasks = task.subtasks + newSub)
            } else {
                task
            }
        }
    }

    fun editSubtask(taskId: Int, subtaskId: Int, newTitle: String) {
        if (newTitle.isBlank()) return
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                val updatedSubtasks = task.subtasks.map { subtask ->
                    if (subtask.id == subtaskId) {
                        subtask.copy(title = newTitle.trim())
                    } else {
                        subtask
                    }
                }
                task.copy(subtasks = updatedSubtasks)
            } else {
                task
            }
        }
    }

    fun deleteSubtask(taskId: Int, subtaskId: Int) {
        _tasks.value = _tasks.value.map { task ->
            if (task.id == taskId) {
                task.copy(subtasks = task.subtasks.filter { it.id != subtaskId })
            } else {
                task
            }
        }
    }

    fun resetToMockData() {
        _tasks.value = initialMockTasks()
    }

    private companion object {
        fun initialMockTasks(): List<Task> = listOf(
            Task(
                id = 4,
                title = "Tập thể dục 30 phút",
                note = "",
                category = "life",
                date = "2026-10-20",
                time = "06:30",
                priority = "medium",
                remind = true,
                done = true,
                subtasks = emptyList()
            ),
            Task(
                id = 1,
                title = "Nộp bài tập Lập trình di động",
                note = "Nộp file .zip lên hệ thống của trường. Nhớ kèm ảnh chụp màn hình app.",
                category = "study",
                date = "2026-10-20",
                time = "09:00",
                priority = "high",
                remind = true,
                done = true,
                subtasks = listOf(
                    Subtask(1, "Hoàn thiện màn hình đăng nhập", true),
                    Subtask(2, "Chụp ảnh màn hình app", true),
                    Subtask(3, "Nén file và nộp bài", true)
                )
            ),
            Task(
                id = 2,
                title = "Ôn chương 3 – Cấu trúc dữ liệu",
                note = "Tập trung phần cây nhị phân và bảng băm.",
                category = "study",
                date = "2026-10-20",
                time = "14:00",
                priority = "high",
                remind = true,
                done = false,
                subtasks = listOf(
                    Subtask(1, "Đọc lại slide bài giảng", true),
                    Subtask(2, "Làm 10 bài tập cuối chương", false)
                )
            ),
            Task(
                id = 3,
                title = "Đi chợ mua đồ cho tuần",
                note = "",
                category = "life",
                date = "2026-10-20",
                time = "17:30",
                priority = "medium",
                remind = true,
                done = false,
                subtasks = emptyList()
            ),
            Task(
                id = 5,
                title = "Gọi điện về nhà",
                note = "",
                category = "life",
                date = "2026-10-21",
                time = "21:00",
                priority = "medium",
                remind = true,
                done = false,
                subtasks = emptyList()
            ),
            Task(
                id = 6,
                title = "Họp nhóm đồ án",
                note = "Thảo luận về phân chia công việc tuần sau.",
                category = "study",
                date = "2026-10-25",
                time = "20:00",
                priority = "high",
                remind = true,
                done = false,
                subtasks = emptyList()
            )
        )
    }
}
