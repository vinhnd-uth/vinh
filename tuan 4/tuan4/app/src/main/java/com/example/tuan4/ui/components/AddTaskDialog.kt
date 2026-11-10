package com.example.tuan4.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskAdded: (title: String, note: String, category: String, time: String, date: String, priority: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Học tập") }
    var time by remember { mutableStateOf("09:00") }
    var date by remember { mutableStateOf("2026-10-20") }
    var priority by remember { mutableStateOf("Vừa") }
    var isTitleError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Thêm công việc mới",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = { Text("Tên công việc *") },
                    isError = isTitleError,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (isTitleError) {
                    Text(
                        text = "Vui lòng nhập tên công việc",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 4.dp, top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Ghi chú") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Danh mục:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = category == "Học tập",
                        onClick = { category = "Học tập" },
                        label = { Text("Học tập") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = category == "Cuộc sống",
                        onClick = { category = "Cuộc sống" },
                        label = { Text("Cuộc sống") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text("Mức độ ưu tiên:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Row(modifier = Modifier.fillMaxWidth()) {
                    FilterChip(
                        selected = priority == "Vừa",
                        onClick = { priority = "Vừa" },
                        label = { Text("Vừa") }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = priority == "Gấp",
                        onClick = { priority = "Gấp" },
                        label = { Text("Gấp (Cao)") }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        label = { Text("Ngày") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = time,
                        onValueChange = { time = it },
                        label = { Text("Giờ") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isBlank()) {
                        isTitleError = true
                    } else {
                        onTaskAdded(title, note, category, time, date, priority)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Thêm", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy")
            }
        }
    )
}
