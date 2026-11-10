package com.example.tuan4.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tuan4.data.model.Task
import com.example.tuan4.data.repository.TaskRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class TaskViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    val tasks: StateFlow<List<Task>> = repository.tasks
    val isLoading: StateFlow<Boolean> = repository.isLoading
    val errorMessage: StateFlow<String?> = repository.errorMessage

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("Tất cả")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedTaskId = MutableStateFlow<Int?>(1)
    val selectedTaskId: StateFlow<Int?> = _selectedTaskId.asStateFlow()

    val filteredTasks: StateFlow<List<Task>> = combine(
        tasks,
        searchQuery,
        selectedCategory
    ) { allTasks, query, category ->
        allTasks.filter { task ->
            val matchesSearch = query.isBlank() ||
                    task.title.contains(query, ignoreCase = true) ||
                    task.note.contains(query, ignoreCase = true)

            val matchesCategory = when (category) {
                "Học tập" -> task.categoryDisplayName == "Học tập"
                "Cuộc sống" -> task.categoryDisplayName == "Cuộc sống"
                "CẦN LÀM" -> !task.done
                else -> true
            }

            matchesSearch && matchesCategory
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalTodayTasks: StateFlow<Int> = tasks.combine(tasks) { _, all ->
        all.count { it.date == "2026-10-20" || it.date.isEmpty() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 4)

    val completedTodayTasks: StateFlow<Int> = tasks.combine(tasks) { _, all ->
        all.count { (it.date == "2026-10-20" || it.date.isEmpty()) && it.done }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val studyRemainingCount: StateFlow<Int> = tasks.combine(tasks) { _, all ->
        all.count { it.categoryDisplayName == "Học tập" && !it.done }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val lifeRemainingCount: StateFlow<Int> = tasks.combine(tasks) { _, all ->
        all.count { it.categoryDisplayName == "Cuộc sống" && !it.done }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val selectedTask: StateFlow<Task?> = combine(tasks, selectedTaskId) { allTasks, id ->
        allTasks.find { it.id == id } ?: allTasks.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    init {
        loadTasks()
    }

    fun loadTasks() {
        viewModelScope.launch {
            repository.loadTasks()
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun selectTask(taskId: Int) {
        _selectedTaskId.value = taskId
    }

    fun toggleTaskDone(taskId: Int) {
        viewModelScope.launch {
            repository.toggleTaskDone(taskId)
        }
    }

    fun addTask(
        title: String,
        note: String,
        category: String,
        time: String,
        date: String,
        priority: String
    ) {
        if (title.isBlank()) return
        val catKey = if (category == "Học tập") "study" else "life"
        val prioKey = when (priority) {
            "Gấp", "Cao", "Ưu tiên cao" -> "high"
            "Thấp", "Ưu tiên thấp" -> "low"
            else -> "medium"
        }
        viewModelScope.launch {
            repository.addTask(
                Task(
                    id = 0,
                    title = title,
                    note = note,
                    category = catKey,
                    date = date.ifEmpty { "2026-10-20" },
                    time = time.ifEmpty { "09:00" },
                    priority = prioKey,
                    remind = true,
                    done = false,
                    subtasks = emptyList()
                )
            )
        }
    }

    fun updateTask(task: Task) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun deleteTask(taskId: Int) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
            if (_selectedTaskId.value == taskId) {
                val remaining = tasks.value.filter { it.id != taskId }
                _selectedTaskId.value = remaining.firstOrNull()?.id
            }
        }
    }

    fun toggleSubtask(taskId: Int, subtaskId: Int) {
        repository.toggleSubtaskCompleted(taskId, subtaskId)
    }

    fun addSubtask(taskId: Int, title: String) {
        repository.addSubtask(taskId, title)
    }

    fun editSubtask(taskId: Int, subtaskId: Int, newTitle: String) {
        repository.editSubtask(taskId, subtaskId, newTitle)
    }

    fun deleteSubtask(taskId: Int, subtaskId: Int) {
        repository.deleteSubtask(taskId, subtaskId)
    }

    fun resetMockData() {
        repository.resetToMockData()
    }
}
