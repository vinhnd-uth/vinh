package com.example.thunghiem.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.thunghiem.ui.components.PrimaryButtonOrange
import com.example.thunghiem.ui.theme.ThunghiemTheme
import com.example.thunghiem.utils.NotificationHelper

enum class MainTab(val title: String) {
    HOME("Home"),
    LOCATION("Location"),
    CAMERA("Camera"),
    NOTIFICATIONS("Alerts"),
    SETTINGS("Settings")
}

data class PermissionStatusMap(
    val locationGranted: Boolean = true,
    val notificationGranted: Boolean = true,
    val cameraGranted: Boolean = true
)

@Composable
fun MainAppScreen(
    permissionStatuses: PermissionStatusMap = PermissionStatusMap(),
    onResetPermissions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(MainTab.HOME) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color(0xFFF8F9FA),
        topBar = {
            MainTopAppBar(permissionStatuses = permissionStatuses)
        },
        bottomBar = {
            MainBottomNavigationBar(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                MainTab.HOME -> HomeDashboardTab(
                    permissionStatuses = permissionStatuses,
                    onNavigateTab = { selectedTab = it }
                )
                MainTab.LOCATION -> LocationFeatureTab(isGranted = permissionStatuses.locationGranted)
                MainTab.CAMERA -> CameraFeatureTab(isGranted = permissionStatuses.cameraGranted)
                MainTab.NOTIFICATIONS -> NotificationFeatureTab(isGranted = permissionStatuses.notificationGranted)
                MainTab.SETTINGS -> SettingsTab(
                    permissionStatuses = permissionStatuses,
                    onResetPermissions = onResetPermissions
                )
            }
        }
    }
}

@Composable
fun MainTopAppBar(permissionStatuses: PermissionStatusMap) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(
                    colors = listOf(Color(0xFF1E1E1E), Color(0xFF2D2D2D))
                )
            )
            .padding(horizontal = 20.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "UTH Smart App",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Ứng dụng đã sẵn sàng sử dụng",
                    fontSize = 12.sp,
                    color = Color.LightGray
                )
            }

            // Permission status badges indicator
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatusBadge(label = "GPS", isGranted = permissionStatuses.locationGranted)
                StatusBadge(label = "NOTIF", isGranted = permissionStatuses.notificationGranted)
                StatusBadge(label = "CAM", isGranted = permissionStatuses.cameraGranted)
            }
        }
    }
}

@Composable
fun StatusBadge(label: String, isGranted: Boolean) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isGranted) Color(0xFF2E7D32) else Color(0xFFC62828))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = "$label ${if (isGranted) "✓" else "✕"}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun MainBottomNavigationBar(
    selectedTab: MainTab,
    onTabSelected: (MainTab) -> Unit
) {
    NavigationBar(
        containerColor = Color.White,
        tonalElevation = 8.dp
    ) {
        MainTab.entries.forEach { tab ->
            val isSelected = selectedTab == tab
            NavigationBarItem(
                selected = isSelected,
                onClick = { onTabSelected(tab) },
                icon = {
                    TabIcon(tab = tab, isSelected = isSelected)
                },
                label = {
                    Text(
                        text = tab.title,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PrimaryButtonOrange,
                    selectedTextColor = PrimaryButtonOrange,
                    indicatorColor = PrimaryButtonOrange.copy(alpha = 0.15f),
                    unselectedIconColor = Color.Gray,
                    unselectedTextColor = Color.Gray
                )
            )
        }
    }
}

@Composable
fun TabIcon(tab: MainTab, isSelected: Boolean) {
    val color = if (isSelected) PrimaryButtonOrange else Color.Gray
    Canvas(modifier = Modifier.size(24.dp)) {
        val w = size.width
        val h = size.height
        when (tab) {
            MainTab.HOME -> {
                // House shape
                val path = Path().apply {
                    moveTo(w * 0.5f, h * 0.15f)
                    lineTo(w * 0.85f, h * 0.45f)
                    lineTo(w * 0.85f, h * 0.85f)
                    lineTo(w * 0.15f, h * 0.85f)
                    lineTo(w * 0.15f, h * 0.45f)
                    close()
                }
                drawPath(path, color = color, style = Stroke(width = w * 0.1f))
            }
            MainTab.LOCATION -> {
                // Pin shape
                drawCircle(color = color, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.4f), style = Stroke(width = w * 0.1f))
                drawCircle(color = color, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.4f))
            }
            MainTab.CAMERA -> {
                // Camera body
                drawRoundRect(color = color, topLeft = Offset(w * 0.15f, h * 0.25f), size = Size(w * 0.7f, h * 0.55f), cornerRadius = CornerRadius(w * 0.08f))
                drawCircle(color = Color.White, radius = w * 0.15f, center = Offset(w * 0.5f, h * 0.52f))
            }
            MainTab.NOTIFICATIONS -> {
                // Bell shape
                val bell = Path().apply {
                    moveTo(w * 0.3f, h * 0.65f)
                    cubicTo(w * 0.3f, h * 0.35f, w * 0.7f, h * 0.35f, w * 0.7f, h * 0.65f)
                    lineTo(w * 0.85f, h * 0.75f)
                    lineTo(w * 0.15f, h * 0.75f)
                    close()
                }
                drawPath(bell, color = color)
                drawCircle(color = color, radius = w * 0.08f, center = Offset(w * 0.5f, h * 0.85f))
            }
            MainTab.SETTINGS -> {
                // Gear outline
                drawCircle(color = color, radius = w * 0.35f, center = Offset(w * 0.5f, h * 0.5f), style = Stroke(width = w * 0.12f))
                drawCircle(color = color, radius = w * 0.12f, center = Offset(w * 0.5f, h * 0.5f))
            }
        }
    }
}

// ----------------------------------------------------
// TAB 1: HOME DASHBOARD
// ----------------------------------------------------
@Composable
fun HomeDashboardTab(
    permissionStatuses: PermissionStatusMap,
    onNavigateTab: (MainTab) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Welcome Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "👋 Xin chào sinh viên UTH!",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1E1E1E)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Bạn đã hoàn thành cấp 3 quyền truy cập (Vị trí, Thông báo, Máy ảnh). Hãy trải nghiệm đầy đủ các tính năng bên dưới.",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        lineHeight = 20.sp
                    )
                }
            }
        }

        item {
            Text(
                text = "Tính năng khả dụng",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E)
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FeatureQuickCard(
                    title = "Vị trí GPS",
                    subtitle = if (permissionStatuses.locationGranted) "Đã bật GPS" else "Chưa cấp quyền",
                    isGranted = permissionStatuses.locationGranted,
                    color = Color(0xFFE8F5E9),
                    accentColor = Color(0xFF2E7D32),
                    onClick = { onNavigateTab(MainTab.LOCATION) },
                    modifier = Modifier.weight(1f)
                )

                FeatureQuickCard(
                    title = "Quét Mã QR",
                    subtitle = if (permissionStatuses.cameraGranted) "Sẵn sàng quét" else "Chưa cấp quyền",
                    isGranted = permissionStatuses.cameraGranted,
                    color = Color(0xFFFFF3E0),
                    accentColor = PrimaryButtonOrange,
                    onClick = { onNavigateTab(MainTab.CAMERA) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            FeatureQuickCard(
                title = "Hệ thống Thông báo",
                subtitle = if (permissionStatuses.notificationGranted) "Nhận thông báo cập nhật học tập & sự kiện" else "Thông báo bị tắt",
                isGranted = permissionStatuses.notificationGranted,
                color = Color(0xFFE3F2FD),
                accentColor = Color(0xFF1565C0),
                onClick = { onNavigateTab(MainTab.NOTIFICATIONS) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Nhật ký hoạt động gần đây",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E1E)
            )
        }

        items(
            listOf(
                "Đã cấp quyền Location - Định vị vị trí Campus",
                "Đã cấp quyền Notification - Đăng ký kênh thông báo UTH",
                "Đã cấp quyền Camera - Quét mã QR điểm danh",
                "Hệ thống đã xác nhận cấp đủ 3 quyền thành công"
            )
        ) { activity ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(PrimaryButtonOrange)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = activity,
                        fontSize = 13.sp,
                        color = Color(0xFF424242)
                    )
                }
            }
        }
    }
}

@Composable
fun FeatureQuickCard(
    title: String,
    subtitle: String,
    isGranted: Boolean,
    color: Color,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
                Text(
                    text = if (isGranted) "✓" else "!",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.DarkGray
            )
        }
    }
}

// ----------------------------------------------------
// TAB 2: LOCATION FEATURE
// ----------------------------------------------------
@Composable
fun LocationFeatureTab(isGranted: Boolean) {
    val context = LocalContext.current
    var gpsCoordinates by remember { mutableStateOf("10.8016° N, 106.7121° E") }
    var locationName by remember { mutableStateOf("Trường Đại học Giao thông vận tải TP.HCM (UTH)") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "📍 Tính năng Định vị GPS",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGranted) "Quyền vị trí đã được cho phép." else "Quyền vị trí bị từ chối.",
                    fontSize = 13.sp,
                    color = if (isGranted) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Radar / Map Display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E293B)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2, size.height / 2)
                drawCircle(color = Color(0xFF334155), radius = 120.dp.toPx(), style = Stroke(2.dp.toPx()))
                drawCircle(color = Color(0xFF334155), radius = 70.dp.toPx(), style = Stroke(2.dp.toPx()))
                drawCircle(color = Color(0xFF0EA5E9), radius = 8.dp.toPx(), center = center)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "🛰️ Mock Map Radar", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = locationName, color = Color.White, fontSize = 13.sp)
                Text(text = "Tọa độ: $gpsCoordinates", color = Color.LightGray, fontSize = 11.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                gpsCoordinates = "10.8021° N, 106.7128° E"
                locationName = "Cơ sở 1 UTH - Võ Văn Ngân, TP. Thủ Đức"
                Toast.makeText(context, "Đã cập nhật vị trí GPS mới!", Toast.LENGTH_SHORT).show()
            },
            enabled = isGranted,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryButtonOrange)
        ) {
            Text("Lấy tọa độ GPS hiện tại", color = Color.White, fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Địa điểm xung quanh bạn:",
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start)
        )

        Spacer(modifier = Modifier.height(8.dp))

        listOf(
            "📚 Thư viện trung tâm UTH - Cách 50m",
            "☕ Căn tin Sinh viên - Cách 120m",
            "🏫 Phòng Thí nghiệm Công nghệ - Cách 200m"
        ).forEach { place ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White)
            ) {
                Text(text = place, modifier = Modifier.padding(12.dp), fontSize = 13.sp)
            }
        }
    }
}

// ----------------------------------------------------
// TAB 3: CAMERA & QR SCANNER FEATURE
// ----------------------------------------------------
@Composable
fun CameraFeatureTab(isGranted: Boolean) {
    val context = LocalContext.current
    var isFlashOn by remember { mutableStateOf(false) }
    var scanResult by remember { mutableStateOf<String?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "scan_line")
    val scanLineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 180f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "y"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "📷 Tính năng Máy ảnh & Quét Mã QR",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGranted) "Quyền Camera đã sẵn sàng." else "Chưa cấp quyền Camera.",
                    fontSize = 13.sp,
                    color = if (isGranted) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Simulated Camera Viewfinder
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isFlashOn) Color(0xFF475569) else Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                val w = size.width
                val strokeW = 4.dp.toPx()
                // Target Corners
                val cornerLength = 30.dp.toPx()
                // Top-Left
                drawLine(Color(0xFF22C55E), Offset(0f, 0f), Offset(cornerLength, 0f), strokeWidth = strokeW)
                drawLine(Color(0xFF22C55E), Offset(0f, 0f), Offset(0f, cornerLength), strokeWidth = strokeW)
                // Top-Right
                drawLine(Color(0xFF22C55E), Offset(w, 0f), Offset(w - cornerLength, 0f), strokeWidth = strokeW)
                drawLine(Color(0xFF22C55E), Offset(w, 0f), Offset(w, cornerLength), strokeWidth = strokeW)
                // Bottom-Left
                drawLine(Color(0xFF22C55E), Offset(0f, w), Offset(cornerLength, w), strokeWidth = strokeW)
                drawLine(Color(0xFF22C55E), Offset(0f, w), Offset(0f, w - cornerLength), strokeWidth = strokeW)
                // Bottom-Right
                drawLine(Color(0xFF22C55E), Offset(w, w), Offset(w - cornerLength, w), strokeWidth = strokeW)
                drawLine(Color(0xFF22C55E), Offset(w, w), Offset(w, w - cornerLength), strokeWidth = strokeW)

                // Animated Scan Line
                if (isGranted) {
                    drawLine(
                        color = Color(0xFFEF4444),
                        start = Offset(0f, scanLineY.dp.toPx()),
                        end = Offset(w, scanLineY.dp.toPx()),
                        strokeWidth = 3.dp.toPx()
                    )
                }
            }

            Text(
                text = if (isGranted) "Đang tìm mã QR..." else "Cần cấp quyền Camera để bật Viewfinder",
                color = Color.White,
                fontSize = 13.sp,
                modifier = Modifier.padding(top = 180.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { isFlashOn = !isFlashOn },
                enabled = isGranted,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text(if (isFlashOn) "🔦 Tắt Flash" else "💡 Bật Flash")
            }

            Button(
                onClick = {
                    scanResult = "Mã Sinh Viên: MSSV-2151050090 | UTH Student Pass"
                    Toast.makeText(context, "Quét mã QR thành công!", Toast.LENGTH_SHORT).show()
                },
                enabled = isGranted,
                modifier = Modifier
                    .weight(1.2f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryButtonOrange)
            ) {
                Text("Quét Mã Mẫu", color = Color.White)
            }
        }

        scanResult?.let { result ->
            Spacer(modifier = Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = "✅ Kết quả quét QR:", fontWeight = FontWeight.Bold, color = Color(0xFF15803D))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = result, fontSize = 13.sp, color = Color(0xFF166534))
                }
            }
        }
    }
}

// ----------------------------------------------------
// TAB 4: NOTIFICATIONS FEATURE
// ----------------------------------------------------
@Composable
fun NotificationFeatureTab(isGranted: Boolean) {
    val context = LocalContext.current
    var isEventsEnabled by remember { mutableStateOf(true) }
    var isAcademicEnabled by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🔔 Quản lý Thông báo",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (isGranted) "Đã bật quyền thông báo hệ thống." else "Chưa cho phép gửi thông báo.",
                    fontSize = 13.sp,
                    color = if (isGranted) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Tùy chọn kênh thông báo:", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Thông báo Lịch học & Thi", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(text = "Cập nhật thay đổi phòng học", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isAcademicEnabled,
                        onCheckedChange = { isAcademicEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryButtonOrange)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "Sự kiện & Hoạt động Sinh viên", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                        Text(text = "Tin tức hội thao, hội thảo khoa học", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = isEventsEnabled,
                        onCheckedChange = { isEventsEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = PrimaryButtonOrange)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                NotificationHelper.sendTestNotification(
                    context = context,
                    title = "UTH Thông báo học tập",
                    message = "Bạn có lịch học bài tập nhóm Android lúc 16:54 chiều nay!"
                )
                Toast.makeText(context, "Đã gửi thông báo thử nghiệm!", Toast.LENGTH_SHORT).show()
            },
            enabled = isGranted,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PrimaryButtonOrange)
        ) {
            Text("Gửi Thông Báo Thử Nghiệm Ngay", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// TAB 5: SETTINGS & PERMISSION MANAGER
// ----------------------------------------------------
@Composable
fun SettingsTab(
    permissionStatuses: PermissionStatusMap,
    onResetPermissions: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "⚙️ Trạng thái Quyền Ứng dụng",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1E1E1E)
                )
                Spacer(modifier = Modifier.height(12.dp))

                PermissionRow("Quyền Vị trí (Location)", permissionStatuses.locationGranted)
                PermissionRow("Quyền Thông báo (Notification)", permissionStatuses.notificationGranted)
                PermissionRow("Quyền Máy ảnh (Camera)", permissionStatuses.cameraGranted)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = onResetPermissions,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F))
        ) {
            Text("Cấu hình lại 3 Quyền ban đầu", color = Color.White, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun PermissionRow(name: String, isGranted: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = name, fontSize = 14.sp)
        Text(
            text = if (isGranted) "Đã cấp ✓" else "Chưa cấp ✕",
            color = if (isGranted) Color(0xFF2E7D32) else Color(0xFFC62828),
            fontWeight = FontWeight.Bold,
            fontSize = 13.sp
        )
    }
}

@Preview(showBackground = true, name = "Main App Dashboard")
@Composable
fun MainAppScreenPreview() {
    ThunghiemTheme {
        MainAppScreen()
    }
}
