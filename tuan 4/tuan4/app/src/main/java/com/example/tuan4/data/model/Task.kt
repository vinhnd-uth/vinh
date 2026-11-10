package com.example.tuan4.data.model

data class Subtask(
    val id: Int,
    val title: String,
    val completed: Boolean = false
)

data class Task(
    val id: Int,
    val title: String,
    val note: String = "",
    val category: String = "study",
    val date: String = "",
    val time: String = "",
    val priority: String = "medium",
    val remind: Boolean = true,
    val done: Boolean = false,
    val subtasks: List<Subtask> = emptyList()
) {
    val categoryDisplayName: String
        get() = when (category.lowercase()) {
            "study", "học tập" -> "Học tập"
            "life", "cuộc sống" -> "Cuộc sống"
            else -> category.ifEmpty { "Khác" }
        }

    val priorityDisplayName: String
        get() = when (priority.lowercase()) {
            "high", "cao", "gấp" -> "Ưu tiên cao"
            "medium", "vừa" -> "Ưu tiên vừa"
            "low", "thấp" -> "Ưu tiên thấp"
            else -> "Ưu tiên vừa"
        }
}

data class CreateTaskRequest(
    val title: String,
    val note: String = "",
    val category: String = "study",
    val date: String = "",
    val time: String = "",
    val priority: String = "medium",
    val remind: Boolean = true,
    val done: Boolean = false,
    val subtasks: List<Subtask> = emptyList()
)
