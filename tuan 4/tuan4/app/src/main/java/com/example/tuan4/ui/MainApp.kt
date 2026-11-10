package com.example.tuan4.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tuan4.ui.components.AddTaskDialog
import com.example.tuan4.ui.components.BottomNavBar
import com.example.tuan4.ui.screens.HomeScreen
import com.example.tuan4.ui.screens.NotificationsScreen
import com.example.tuan4.ui.screens.SettingsScreen
import com.example.tuan4.ui.screens.TaskDetailScreen
import com.example.tuan4.ui.screens.TaskListScreen
import com.example.tuan4.ui.viewmodel.TaskViewModel

@Composable
fun MainApp(
    viewModel: TaskViewModel = viewModel()
) {
    var currentRoute by remember { mutableStateOf("home") }
    var previousRoute by remember { mutableStateOf("home") }
    var showAddTaskDialog by remember { mutableStateOf(false) }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskAdded = { title, note, category, time, date, priority ->
                viewModel.addTask(title, note, category, time, date, priority)
            }
        )
    }

    val showBottomBar = currentRoute != "detail"

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        previousRoute = currentRoute
                        currentRoute = route
                    },
                    onAddClick = {
                        showAddTaskDialog = true
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                "home" -> HomeScreen(
                    viewModel = viewModel,
                    onSeeAllClick = {
                        previousRoute = "home"
                        currentRoute = "list"
                    },
                    onTaskClick = { taskId ->
                        viewModel.selectTask(taskId)
                        previousRoute = "home"
                        currentRoute = "detail"
                    }
                )

                "list" -> TaskListScreen(
                    viewModel = viewModel,
                    onTaskClick = { taskId ->
                        viewModel.selectTask(taskId)
                        previousRoute = "list"
                        currentRoute = "detail"
                    }
                )

                "detail" -> TaskDetailScreen(
                    viewModel = viewModel,
                    onBackClick = {
                        currentRoute = previousRoute
                    }
                )

                "notifications" -> NotificationsScreen()

                "settings" -> SettingsScreen(
                    viewModel = viewModel
                )
            }
        }
    }
}
