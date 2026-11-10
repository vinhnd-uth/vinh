package com.example.thunghiem.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
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
import androidx.core.content.ContextCompat
import com.example.thunghiem.ui.components.CameraIllustration
import com.example.thunghiem.ui.components.LocationIllustration
import com.example.thunghiem.ui.components.NotificationIllustration
import com.example.thunghiem.ui.components.PermissionDialogCard
import com.example.thunghiem.ui.components.PrimaryButtonOrange
import com.example.thunghiem.ui.theme.ThunghiemTheme

enum class PermissionStep(
    val title: String,
    val description: String,
    val primaryButtonText: String
) {
    LOCATION(
        title = "Location",
        description = "Allow maps to access your location whiloe you use the app?",
        primaryButtonText = "Allow"
    ),
    NOTIFICATION(
        title = "Notification",
        description = "Please enable notifications to receive updates and reminders",
        primaryButtonText = "Turn on"
    ),
    CAMERA(
        title = "Camera",
        description = "We need access to your camera to scan QR codes",
        primaryButtonText = "Turn on"
    )
}

@Composable
fun PermissionFlowScreen(
    modifier: Modifier = Modifier,
    initialStepIndex: Int = 0,
    forceShowMainApp: Boolean = false
) {
    val context = LocalContext.current

    var currentStepIndex by remember { mutableIntStateOf(initialStepIndex) }
    var selectedLanguage by remember { mutableStateOf("ENG") }
    var isFlowCompleted by remember { mutableStateOf(forceShowMainApp) }

    // Track statuses of 3 permissions
    var isLocationGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var isNotificationGranted by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) == PackageManager.PERMISSION_GRANTED
            } else true
        )
    }

    var isCameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // System Permission Launchers
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isLocationGranted = isGranted
        Toast.makeText(
            context,
            if (isGranted) "Vị trí: Đã được cấp quyền!" else "Vị trí: Bỏ qua / Từ chối",
            Toast.LENGTH_SHORT
        ).show()
        if (currentStepIndex < PermissionStep.entries.size - 1) {
            currentStepIndex++
        } else {
            isFlowCompleted = true
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isNotificationGranted = isGranted
        Toast.makeText(
            context,
            if (isGranted) "Thông báo: Đã được cấp quyền!" else "Thông báo: Bỏ qua / Từ chối",
            Toast.LENGTH_SHORT
        ).show()
        if (currentStepIndex < PermissionStep.entries.size - 1) {
            currentStepIndex++
        } else {
            isFlowCompleted = true
        }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        isCameraGranted = isGranted
        Toast.makeText(
            context,
            if (isGranted) "Camera: Đã được cấp quyền!" else "Camera: Bỏ qua / Từ chối",
            Toast.LENGTH_SHORT
        ).show()
        if (currentStepIndex < PermissionStep.entries.size - 1) {
            currentStepIndex++
        } else {
            isFlowCompleted = true
        }
    }

    // If flow is completed, display the Main Interactive Application!
    if (isFlowCompleted) {
        MainAppScreen(
            permissionStatuses = PermissionStatusMap(
                locationGranted = isLocationGranted,
                notificationGranted = isNotificationGranted,
                cameraGranted = isCameraGranted
            ),
            onResetPermissions = {
                currentStepIndex = 0
                isFlowCompleted = false
            },
            modifier = modifier
        )
        return
    }

    val currentStep = PermissionStep.entries.getOrNull(currentStepIndex)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1C1B1F)) // Dark background matching assignment
    ) {
        // Decorative background elements representing the app under the overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF2C2C2C),
                            Color(0xFF141414)
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top Status & Header Bar (Time + Language Switcher)
            TopHeaderBar(
                selectedLanguage = selectedLanguage,
                onLanguageSelected = { selectedLanguage = it }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Step Navigation Bar / Tabs (allow direct tab click or step inspection)
            TabRow(
                selectedTabIndex = currentStepIndex.coerceAtMost(2),
                containerColor = Color.Transparent,
                contentColor = PrimaryButtonOrange,
                indicator = { tabPositions ->
                    if (currentStepIndex < 3) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[currentStepIndex]),
                            color = PrimaryButtonOrange
                        )
                    }
                },
                divider = {}
            ) {
                PermissionStep.entries.forEachIndexed { index, step ->
                    Tab(
                        selected = currentStepIndex == index,
                        onClick = {
                            currentStepIndex = index
                        },
                        text = {
                            Text(
                                text = "${index + 1}. ${step.title}",
                                fontSize = 13.sp,
                                fontWeight = if (currentStepIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (currentStepIndex == index) Color.White else Color.Gray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Center Content: Permission Card
            if (currentStep != null) {
                PermissionDialogCard(
                    title = currentStep.title,
                    description = currentStep.description,
                    primaryButtonText = currentStep.primaryButtonText,
                    secondaryButtonText = "Skip for now",
                    iconContent = {
                        when (currentStep) {
                            PermissionStep.LOCATION -> LocationIllustration()
                            PermissionStep.NOTIFICATION -> NotificationIllustration()
                            PermissionStep.CAMERA -> CameraIllustration()
                        }
                    },
                    onPrimaryClick = {
                        // Request real System Permission & advance
                        when (currentStep) {
                            PermissionStep.LOCATION -> {
                                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                            }
                            PermissionStep.NOTIFICATION -> {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    isNotificationGranted = true
                                    if (currentStepIndex < PermissionStep.entries.size - 1) currentStepIndex++ else isFlowCompleted = true
                                }
                            }
                            PermissionStep.CAMERA -> {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        }
                    },
                    onSecondaryClick = {
                        Toast.makeText(
                            context,
                            "Đã bỏ qua quyền ${currentStep.title}",
                            Toast.LENGTH_SHORT
                        ).show()
                        if (currentStepIndex < PermissionStep.entries.size - 1) {
                            currentStepIndex++
                        } else {
                            isFlowCompleted = true
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Background Buttons ("Login", "Continue without account")
            BottomBackgroundButtons(
                onLoginClick = {
                    Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                    isFlowCompleted = true
                },
                onContinueClick = {
                    Toast.makeText(context, "Bắt đầu sử dụng ứng dụng!", Toast.LENGTH_SHORT).show()
                    isFlowCompleted = true
                }
            )
        }
    }
}

@Composable
fun TopHeaderBar(
    selectedLanguage: String,
    onLanguageSelected: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status bar time indicator
        Text(
            text = "16:54",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White.copy(alpha = 0.9f)
        )

        // Language Switcher Pill (বাংলা | ENG)
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(Color(0xFF333333))
                .border(BorderStroke(1.dp, Color(0xFF444444)), CircleShape)
                .padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val isBangla = selectedLanguage == "BANG"
            val isEng = selectedLanguage == "ENG"

            // Bangla option
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isBangla) PrimaryButtonOrange else Color.Transparent)
                    .clickable { onLanguageSelected("BANG") }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "বাংলা",
                    fontSize = 12.sp,
                    color = if (isBangla) Color.White else Color.LightGray,
                    fontWeight = if (isBangla) FontWeight.Bold else FontWeight.Normal
                )
            }

            // ENG option
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(if (isEng) PrimaryButtonOrange else Color.Transparent)
                    .clickable { onLanguageSelected("ENG") }
                    .padding(horizontal = 10.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ENG",
                    fontSize = 12.sp,
                    color = if (isEng) Color.White else Color.LightGray,
                    fontWeight = if (isEng) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
fun BottomBackgroundButtons(
    onLoginClick: () -> Unit,
    onContinueClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Dark orange "Login" button behind overlay
        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF5C2919),
                contentColor = Color(0xFFB07663)
            )
        ) {
            Text(
                text = "Login",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // "Continue without account" button
        OutlinedButton(
            onClick = onContinueClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color(0xFF4A3832)),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color(0xFF8D736A)
            )
        ) {
            Text(
                text = "Continue without account",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

// Single Screen Previews
@Preview(showBackground = true, name = "1. Location Permission")
@Composable
fun LocationPermissionPreview() {
    ThunghiemTheme {
        PermissionFlowScreen(initialStepIndex = 0)
    }
}

@Preview(showBackground = true, name = "2. Notification Permission")
@Composable
fun NotificationPermissionPreview() {
    ThunghiemTheme {
        PermissionFlowScreen(initialStepIndex = 1)
    }
}

@Preview(showBackground = true, name = "3. Camera Permission")
@Composable
fun CameraPermissionPreview() {
    ThunghiemTheme {
        PermissionFlowScreen(initialStepIndex = 2)
    }
}

@Preview(showBackground = true, name = "4. Main Application Preview")
@Composable
fun MainAppPreview() {
    ThunghiemTheme {
        PermissionFlowScreen(forceShowMainApp = true)
    }
}

// Side-by-side preview showing all 3 screens as in homework image
@Preview(showBackground = true, widthDp = 1080, heightDp = 700, name = "Side By Side All 3 Screens")
@Composable
fun SideBySideAll3ScreensPreview() {
    ThunghiemTheme {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PermissionFlowScreen(initialStepIndex = 0)
            }
            Box(modifier = Modifier.weight(1f)) {
                PermissionFlowScreen(initialStepIndex = 1)
            }
            Box(modifier = Modifier.weight(1f)) {
                PermissionFlowScreen(initialStepIndex = 2)
            }
        }
    }
}
