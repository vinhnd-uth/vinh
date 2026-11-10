package com.example.tuan4.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onAddClick: () -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        NavigationBarItem(
            selected = currentRoute == "home",
            onClick = { onNavigate("home") },
            icon = { Icon(Icons.Default.Home, contentDescription = "Trang chủ") },
            label = { Text("Trang chủ", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2563EB),
                selectedTextColor = Color(0xFF2563EB),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = Color(0xFFEFF6FF)
            )
        )

        NavigationBarItem(
            selected = currentRoute == "list",
            onClick = { onNavigate("list") },
            icon = { Icon(Icons.AutoMirrored.Filled.FormatListBulleted, contentDescription = "Danh sách") },
            label = { Text("Danh sách", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2563EB),
                selectedTextColor = Color(0xFF2563EB),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = Color(0xFFEFF6FF)
            )
        )

        NavigationBarItem(
            selected = false,
            onClick = onAddClick,
            icon = {
                FloatingActionButton(
                    onClick = onAddClick,
                    shape = CircleShape,
                    containerColor = Color(0xFF2563EB),
                    contentColor = Color.White,
                    elevation = FloatingActionButtonDefaults.elevation(4.dp),
                    modifier = Modifier.size(44.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm công việc", modifier = Modifier.size(24.dp))
                }
            },
            label = { Text("", fontSize = 0.sp) },
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = Color.Transparent
            )
        )

        NavigationBarItem(
            selected = currentRoute == "notifications",
            onClick = { onNavigate("notifications") },
            icon = { Icon(Icons.Default.Notifications, contentDescription = "Thông báo") },
            label = { Text("Thông báo", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2563EB),
                selectedTextColor = Color(0xFF2563EB),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = Color(0xFFEFF6FF)
            )
        )

        NavigationBarItem(
            selected = currentRoute == "settings",
            onClick = { onNavigate("settings") },
            icon = { Icon(Icons.Default.Settings, contentDescription = "Cài đặt") },
            label = { Text("Cài đặt", fontSize = 11.sp) },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = Color(0xFF2563EB),
                selectedTextColor = Color(0xFF2563EB),
                unselectedIconColor = Color(0xFF64748B),
                unselectedTextColor = Color(0xFF64748B),
                indicatorColor = Color(0xFFEFF6FF)
            )
        )
    }
}
