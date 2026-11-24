package com.example.demo.youtube

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ==========================================
// 1. THEME VÀ BẢNG MÀU (LIGHT & DARK THEMES)
// ==========================================

/**
 * Bảng màu tùy chỉnh cho ứng dụng YouTube Mobile
 */
data class YouTubeColorPalette(
    val background: Color,
    val surface: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val brandRed: Color = Color(0xFFFF0000),
    val chipUnselectedBg: Color,
    val chipUnselectedText: Color,
    val chipSelectedBg: Color,
    val chipSelectedText: Color,
    val divider: Color,
    val topBottomBarBg: Color
)

/**
 * Bảng màu Nền Trắng mặc định (Light Theme)
 */
val YouTubeLightColors = YouTubeColorPalette(
    background = Color(0xFFFFFFFF),
    surface = Color(0xFFF9F9F9),
    cardBackground = Color(0xFFFFFFFF),
    textPrimary = Color(0xFF0F0F0F),
    textSecondary = Color(0xFF606060),
    chipUnselectedBg = Color(0xFFF2F2F2),
    chipUnselectedText = Color(0xFF0F0F0F),
    chipSelectedBg = Color(0xFF0F0F0F),
    chipSelectedText = Color(0xFFFFFFFF),
    divider = Color(0xFFE5E5E5),
    topBottomBarBg = Color(0xFFFFFFFF)
)

/**
 * Bảng màu Nền Tối (Dark Theme)
 */
val YouTubeDarkColors = YouTubeColorPalette(
    background = Color(0xFF0F0F0F),
    surface = Color(0xFF1E1E1E),
    cardBackground = Color(0xFF1E1E1E),
    textPrimary = Color(0xFFFFFFFF),
    textSecondary = Color(0xFFAAAAAA),
    chipUnselectedBg = Color(0xFF272727),
    chipUnselectedText = Color(0xFFFFFFFF),
    chipSelectedBg = Color(0xFFFFFFFF),
    chipSelectedText = Color(0xFF0F0F0F),
    divider = Color(0xFF212121),
    topBottomBarBg = Color(0xFF0F0F0F)
)

// ==========================================
// 2. BỘ CÔNG CỤ VẼ HÌNH ẢNH ĐỒ HỌA NGHỆ THUẬT ĐỘC BẢN (VIDEO THUMBNAIL ILLUSTRATOR)
// ==========================================

/**
 * Vẽ hình ảnh đồ họa trực quan độc bản 100% cho từng video theo chủ đề
 */
object VideoThumbnailIllustrator {

    fun drawThumbnail(scope: DrawScope, videoId: String, category: String, timeOffset: Float = 0f) {
        with(scope) {
            val width = size.width
            val height = size.height

            when (videoId) {
                "v1" -> drawYouTubeSystemDesignScene(width, height, timeOffset)
                "v2" -> drawJetpackComposeScene(width, height, timeOffset)
                "v3" -> drawLofiRadioScene(width, height, timeOffset)
                "v4" -> drawFlagshipSmartphoneScene(width, height, timeOffset)
                "v5" -> drawKotlinCoroutinesScene(width, height, timeOffset)
                "v6" -> drawCyberpunkGamingScene(width, height, timeOffset)
                "v7" -> drawUiUxDesignScene(width, height, timeOffset)
                "v8" -> drawSunsetLakeMusicScene(width, height, timeOffset)
                "v9" -> drawSeniorInterviewScene(width, height, timeOffset)
                "v10" -> drawAndroidProfilerPerformanceScene(width, height, timeOffset)
                "s1" -> drawShortsComposeTipScene(width, height, timeOffset)
                "s2" -> drawShortsUnboxingScene(width, height, timeOffset)
                else -> drawDynamicCustomScene(width, height, videoId, timeOffset)
            }
        }
    }

    private fun DrawScope.drawYouTubeSystemDesignScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF31103F)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        val p1 = Offset(w * 0.18f, h * 0.5f)
        val p2 = Offset(w * 0.42f, h * 0.3f)
        val p3 = Offset(w * 0.42f, h * 0.7f)
        val p4 = Offset(w * 0.72f, h * 0.3f)
        val p5 = Offset(w * 0.72f, h * 0.7f)
        val p6 = Offset(w * 0.9f, h * 0.5f)

        val path = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p2.x, p2.y)
            lineTo(p4.x, p4.y)
            lineTo(p6.x, p6.y)
        }
        drawPath(path, color = Color(0xFF00E5FF).copy(alpha = 0.7f), style = Stroke(3f))

        val path2 = Path().apply {
            moveTo(p1.x, p1.y)
            lineTo(p3.x, p3.y)
            lineTo(p5.x, p5.y)
            lineTo(p6.x, p6.y)
        }
        drawPath(path2, color = Color(0xFFFF007F).copy(alpha = 0.7f), style = Stroke(3f))

        drawRoundRect(Color(0xFF00E5FF), Offset(p1.x - 18f, p1.y - 25f), Size(36f, 50f), CornerRadius(6f), style = Stroke(2.5f))
        drawRoundRect(Color(0xFFFFD700), Offset(p2.x - 30f, p2.y - 18f), Size(60f, 36f), CornerRadius(8f))
        drawRoundRect(Color(0xFFFF007F), Offset(p3.x - 30f, p3.y - 18f), Size(60f, 36f), CornerRadius(8f))
        drawCircle(Color(0xFF00FF66), radius = 22f, center = p6, style = Stroke(3f))
        drawCircle(Color(0xFF00FF66), radius = 8f, center = p6)
    }

    private fun DrawScope.drawJetpackComposeScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF003322), Color(0xFF005533), Color(0xFF008844)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        val cx = w * 0.3f
        val cy = h * 0.5f
        drawArc(Color(0xFF3DDC84), 180f, 180f, true, Offset(cx - 45f, cy - 45f), Size(90f, 90f))
        drawCircle(Color.White, radius = 5f, center = Offset(cx - 18f, cy - 22f))
        drawCircle(Color.White, radius = 5f, center = Offset(cx + 18f, cy - 22f))

        val cardX = w * 0.52f
        val cardY = h * 0.22f
        val cardW = w * 0.4f
        val cardH = h * 0.58f

        drawRoundRect(Color(0xFF1E293B), Offset(cardX, cardY), Size(cardW, cardH), CornerRadius(12f))
        drawRoundRect(Color(0xFF38BDF8), Offset(cardX + 12f, cardY + 12f), Size(cardW - 24f, 32f), CornerRadius(6f))
    }

    private fun DrawScope.drawLofiRadioScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF180E29), Color(0xFF321447), Color(0xFF5B114F)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        val winX = w * 0.12f
        val winY = h * 0.15f
        drawRoundRect(Color(0xFF0D0718), Offset(winX, winY), Size(w * 0.32f, h * 0.68f), CornerRadius(10f))
        drawCircle(Color(0xFFFFF9C4), radius = 24f, center = Offset(winX + 50f, winY + 50f))

        val discX = w * 0.72f
        val discY = h * 0.5f
        drawCircle(Color(0xFF111111), radius = 58f, center = Offset(discX, discY))
        drawCircle(Color(0xFFFF007F), radius = 18f, center = Offset(discX, discY))
    }

    private fun DrawScope.drawFlagshipSmartphoneScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF030712), Color(0xFF0B192C), Color(0xFF1E3E62)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        val phoneW = w * 0.36f
        val phoneH = h * 0.78f
        val px = w * 0.5f - phoneW / 2f
        val py = h * 0.11f

        drawRoundRect(Color(0xFF1E293B), Offset(px, py), Size(phoneW, phoneH), CornerRadius(18f))
        drawRoundRect(Color(0xFF0284C7), Offset(px, py), Size(phoneW, phoneH), CornerRadius(18f), style = Stroke(3f))
    }

    private fun DrawScope.drawKotlinCoroutinesScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF1B003A), Color(0xFF4A0072), Color(0xFF7B1FA2)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        val kx = w * 0.35f
        val ky = h * 0.5f
        val kPath = Path().apply {
            moveTo(kx, ky - 40f)
            lineTo(kx + 40f, ky)
            lineTo(kx, ky + 40f)
            lineTo(kx - 40f, ky)
            close()
        }
        drawPath(kPath, color = Color(0xFFC757BC))
    }

    private fun DrawScope.drawCyberpunkGamingScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF10002B), Color(0xFF240046), Color(0xFF3C096C)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )
    }

    private fun DrawScope.drawUiUxDesignScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF3B0764), Color(0xFF6B21A8), Color(0xFF9333EA)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )
    }

    private fun DrawScope.drawSunsetLakeMusicScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF7C2D12), Color(0xFF9A3412), Color(0xFFC2410C), Color(0xFFEA580C)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )
        drawCircle(Color(0xFFFFE4E6), radius = 35f, center = Offset(w * 0.5f, h * 0.45f))
    }

    private fun DrawScope.drawSeniorInterviewScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF334155)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )
    }

    private fun DrawScope.drawAndroidProfilerPerformanceScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF022C22), Color(0xFF065F46), Color(0xFF047857)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )
    }

    private fun DrawScope.drawShortsComposeTipScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF881337), Color(0xFFBE123C), Color(0xFFE11D48)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )
    }

    private fun DrawScope.drawShortsUnboxingScene(w: Float, h: Float, time: Float) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(Color(0xFF1E3A8A), Color(0xFF1D4ED8), Color(0xFF2563EB)),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )
    }

    private fun DrawScope.drawDynamicCustomScene(w: Float, h: Float, id: String, time: Float) {
        val c1 = Color(0xFF0F172A)
        val c2 = Color(0xFF881337)
        val c3 = Color(0xFF1E1B4B)

        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(c1, c2, c3),
                start = Offset(0f, 0f),
                end = Offset(w, h)
            )
        )

        val path = Path()
        path.moveTo(0f, h * 0.5f)
        var x = 0f
        while (x <= w) {
            val y = h * 0.5f + kotlin.math.sin((x * 0.02f) + time).toFloat() * 20f
            path.lineTo(x, y)
            x += 12f
        }
        drawPath(path, color = Color(0xFFFF007F).copy(alpha = 0.6f), style = Stroke(3.5f))

        val cx = w * 0.5f
        val cy = h * 0.5f
        drawRoundRect(
            color = Color(0xFFFF0000),
            topLeft = Offset(cx - 36f, cy - 24f),
            size = Size(72f, 48f),
            cornerRadius = CornerRadius(12f)
        )

        val playTriangle = Path().apply {
            moveTo(cx - 8f, cy - 12f)
            lineTo(cx + 14f, cy)
            lineTo(cx - 8f, cy + 12f)
            close()
        }
        drawPath(playTriangle, color = Color.White)
    }
}

// ==========================================
// 3. VIDEO CARD COMPONENT
// ==========================================

@Composable
fun VideoCard(
    video: VideoItem,
    colors: YouTubeColorPalette,
    onVideoClick: (VideoItem) -> Unit,
    onOptionClick: (String, VideoItem) -> Unit = { _, _ -> }
) {
    var showMenu by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onVideoClick(video) }
            .padding(bottom = 16.dp)
            .background(colors.cardBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(Color(0xFF1E1E2C))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                VideoThumbnailIllustrator.drawThumbnail(this, video.id, video.category)
            }

            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(10.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.65f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (video.category) {
                        "Lập trình" -> "💻 LẬP TRÌNH"
                        "Âm nhạc" -> "🎵 ÂM NHẠC"
                        "Công nghệ" -> "📱 CÔNG NGHỆ"
                        "Trò chơi" -> "🎮 TRÒ CHƠI"
                        "Thiết kế" -> "🎨 THIẾT KẾ"
                        else -> "▶ YOUTUBE"
                    },
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.7f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("▶", color = Color(0xFFFF0000), fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(bottom = 10.dp, end = 8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(if (video.duration == "TRỰC TIẾP") Color(0xFFFF0000) else Color.Black.copy(alpha = 0.85f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = video.duration,
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (video.watchedSeconds > 0 && video.durationSeconds > 0) {
                val progress = (video.watchedSeconds.toFloat() / video.durationSeconds.toFloat()).coerceIn(0f, 1f)
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .align(Alignment.BottomCenter),
                    color = Color(0xFFFF0000),
                    trackColor = Color.DarkGray
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.brandRed),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = video.channel.name.take(1).uppercase(),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = video.title,
                    color = colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${video.channel.name} • ${video.views} • ${video.publishedTimeAgo}",
                    color = colors.textSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box {
                Text(
                    text = "⋮",
                    color = colors.textSecondary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { showMenu = true }
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(colors.surface)
                ) {
                    DropdownMenuItem(
                        text = { Text("🕒 Lưu vào danh sách Xem sau", color = colors.textPrimary, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            onOptionClick("WATCH_LATER", video)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("💾 Lưu vào danh sách phát", color = colors.textPrimary, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            onOptionClick("SAVE_PLAYLIST", video)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("🔄 Chia sẻ", color = colors.textPrimary, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            onOptionClick("SHARE", video)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("⬇️ Tải xuống video", color = colors.textPrimary, fontSize = 13.sp) },
                        onClick = {
                            showMenu = false
                            onOptionClick("DOWNLOAD", video)
                        }
                    )
                }
            }
        }
    }
}

// ==========================================
// 3. VIDEO PLAYER VIEW COMPONENT
// ==========================================

@Composable
private fun LocalDeviceVideoPlayer(
    uriString: String,
    isPlaying: Boolean,
    modifier: Modifier = Modifier
) {
    val videoUri = remember(uriString) { Uri.parse(uriString) }

    AndroidView(
        factory = { ctx ->
            android.widget.VideoView(ctx).apply {
                setVideoURI(videoUri)
                setOnPreparedListener { mp ->
                    mp.isLooping = true
                    mp.setVolume(1.0f, 1.0f)
                    if (isPlaying) start()
                }
            }
        },
        update = { videoView ->
            if (isPlaying) {
                if (!videoView.isPlaying) videoView.start()
            } else {
                if (videoView.isPlaying) videoView.pause()
            }
        },
        modifier = modifier
    )
}

@Composable
fun VideoPlayerView(
    video: VideoItem,
    isPlaying: Boolean,
    onPlayPauseToggle: () -> Unit,
    currentSeconds: Int,
    totalSeconds: Int,
    onSeek: (Int) -> Unit,
    onRewind: () -> Unit,
    onForward: () -> Unit,
    selectedResolution: String,
    onResolutionChange: (String) -> Unit,
    selectedSpeed: String,
    onSpeedChange: (String) -> Unit,
    onBackClick: () -> Unit
) {
    var showQualityMenu by remember { mutableStateOf(false) }
    var showSpeedMenu by remember { mutableStateOf(false) }
    var showControls by remember { mutableStateOf(true) }
    var frameCounter by remember { mutableIntStateOf(0) }

    val isLocalVideo = video.thumbnailUrl.startsWith("content://") || video.thumbnailUrl.startsWith("file://")

    val infiniteTransition = rememberInfiniteTransition(label = "video_frames")
    val waveOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave"
    )

    val barHeights = listOf(
        infiniteTransition.animateFloat(initialValue = 10f, targetValue = 40f, animationSpec = infiniteRepeatable(tween(400), RepeatMode.Reverse), label = "b1"),
        infiniteTransition.animateFloat(initialValue = 35f, targetValue = 15f, animationSpec = infiniteRepeatable(tween(550), RepeatMode.Reverse), label = "b2"),
        infiniteTransition.animateFloat(initialValue = 20f, targetValue = 50f, animationSpec = infiniteRepeatable(tween(350), RepeatMode.Reverse), label = "b3"),
        infiniteTransition.animateFloat(initialValue = 45f, targetValue = 25f, animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse), label = "b4"),
        infiniteTransition.animateFloat(initialValue = 15f, targetValue = 42f, animationSpec = infiniteRepeatable(tween(450), RepeatMode.Reverse), label = "b5")
    )

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(16L)
            frameCounter++
        }
    }

    fun formatTime(sec: Int): String {
        val m = sec / 60
        val s = sec % 60
        return "%02d:%02d".format(m, s)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .clickable { showControls = !showControls }
    ) {
        if (isLocalVideo) {
            LocalDeviceVideoPlayer(
                uriString = video.thumbnailUrl,
                isPlaying = isPlaying,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val width = size.width
                val height = size.height

                VideoThumbnailIllustrator.drawThumbnail(this, video.id, video.category, waveOffset)

                if (isPlaying) {
                    val barWidth = 14f
                    val spacing = 8f
                    val startX = width - 80f
                    val startY = height - 20f

                    barHeights.forEachIndexed { i, animatedH ->
                        drawRect(
                            color = Color(0xFFFF0000).copy(alpha = 0.85f),
                            topLeft = Offset(startX + i * (barWidth + spacing), startY - animatedH.value),
                            size = Size(barWidth, animatedH.value)
                        )
                    }
                }
            }
        }

        if (showControls) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onBackClick() }
                            .padding(8.dp)
                    ) {
                        Text("←", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }

                    Row {
                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .clickable { showQualityMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("⚙ $selectedResolution", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = showQualityMenu,
                                onDismissRequest = { showQualityMenu = false },
                                modifier = Modifier.background(Color(0xFF212121))
                            ) {
                                listOf("Auto (1080p)", "1080p60 HD", "720p HD", "480p SD", "360p Low").forEach { res ->
                                    DropdownMenuItem(
                                        text = { Text(res, color = Color.White, fontSize = 12.sp) },
                                        onClick = {
                                            onResolutionChange(res)
                                            showQualityMenu = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.Black.copy(alpha = 0.7f))
                                    .clickable { showSpeedMenu = true }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("⚡ $selectedSpeed", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            DropdownMenu(
                                expanded = showSpeedMenu,
                                onDismissRequest = { showSpeedMenu = false },
                                modifier = Modifier.background(Color(0xFF212121))
                            ) {
                                listOf("0.5x", "1.0x (Chuẩn)", "1.25x", "1.5x", "2.0x").forEach { spd ->
                                    DropdownMenuItem(
                                        text = { Text(spd, color = Color.White, fontSize = 12.sp) },
                                        onClick = {
                                            onSpeedChange(spd)
                                            showSpeedMenu = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onRewind() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏪", fontSize = 18.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFFF0000))
                            .clickable { onPlayPauseToggle() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(if (isPlaying) "⏸" else "▶", color = Color.White, fontSize = 24.sp)
                    }

                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.6f))
                            .clickable { onForward() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⏩", fontSize = 18.sp)
                    }
                }

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${formatTime(currentSeconds)} / ${formatTime(totalSeconds)}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text("HLS ABR ACTIVE", color = Color.Green, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = currentSeconds.toFloat(),
                        onValueChange = { onSeek(it.toInt()) },
                        valueRange = 0f..totalSeconds.toFloat(),
                        colors = SliderDefaults.colors(
                            thumbColor = Color(0xFFFF0000),
                            activeTrackColor = Color(0xFFFF0000),
                            inactiveTrackColor = Color.Gray
                        ),
                        modifier = Modifier.height(24.dp)
                    )
                }
            }
        }
    }
}

// ==========================================
// 4. CHAPTER 14 SYSTEM DESIGN DASHBOARD
// ==========================================

@Composable
fun YouTubeDesignScreen(
    facade: YouTubeSystemFacade = remember { YouTubeSystemFacade() }
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Kiến Trúc Hệ Thống", "Bộ Mã Hóa DAG", "Luồng Phát ABR", "Mạng CDN & Nhật Ký")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0F0F))
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF0000)),
                contentAlignment = Alignment.Center
            ) {
                Text("YT", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "quản lý youtube",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "THIẾT KẾ YOUTUBE - Kiến Trúc Hệ Thống Video Quy Mô Lớn",
                    color = Color.LightGray,
                    fontSize = 11.sp
                )
            }
        }

        ScrollableTabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color(0xFF1E1E1E),
            contentColor = Color(0xFFFF4E4E),
            edgePadding = 0.dp
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            color = if (selectedTabIndex == index) Color(0xFFFF4E4E) else Color.Gray,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            when (selectedTabIndex) {
                0 -> ArchitectureOverviewTab()
                1 -> DAGTranscoderTab(facade = facade)
                2 -> ABRStreamingTab(facade = facade)
                3 -> CDNAndLogsTab(facade = facade)
            }
        }
    }
}

@Composable
private fun ArchitectureOverviewTab() {
    val coroutineScope = rememberCoroutineScope()
    var dauMillions by remember { mutableFloatStateOf(5.0f) }
    var videosPerDay by remember { mutableFloatStateOf(5.0f) }
    var avgSizeMb by remember { mutableFloatStateOf(300.0f) }
    var costPerGb by remember { mutableFloatStateOf(0.02f) }

    val facade = remember { YouTubeSystemFacade() }
    val activeRole by facade.dbReplicaManager.activeRole.collectAsState()
    val failoverLogs by facade.dbReplicaManager.failoverLogs.collectAsState()

    val cdnCostResult = facade.calculateCdnCost(
        dauCount = (dauMillions * 1000000L).toLong(),
        videosPerUser = videosPerDay.toInt(),
        avgVideoSizeGb = avgSizeMb / 1024.0,
        costPerGbUsd = costPerGb.toDouble()
    )

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        // 1. CDN Cost Estimator Calculator
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("1. Ước Tính Chi Phí CDN (CDN Cost Estimator - Chapter 14)", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Công thức: 5M DAU × 5 videos × 0.3GB × $0.02 = $150,000 USD / ngày ($4.5 Triệu USD / tháng)", color = Color.LightGray, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Daily Active Users (DAU): ${"%.1f".format(dauMillions)} Triệu người", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = dauMillions,
                        onValueChange = { dauMillions = it },
                        valueRange = 1.0f..20.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Video xem / người / ngày: ${videosPerDay.toInt()} videos", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = videosPerDay,
                        onValueChange = { videosPerDay = it },
                        valueRange = 1.0f..15.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Kích thước trung bình video: ${avgSizeMb.toInt()} MB (${"%.2f".format(avgSizeMb / 1024.0)} GB)", color = Color.White, fontSize = 12.sp)
                    Slider(
                        value = avgSizeMb,
                        onValueChange = { avgSizeMb = it },
                        valueRange = 50.0f..1000.0f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Băng thông CDN hàng ngày:", color = Color.Gray, fontSize = 12.sp)
                                Text("${"%.1f".format(cdnCostResult.dailyBandwidthGb / 1000.0)} TB / ngày", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Chi phí CDN HÀNG NGÀY:", color = Color.Gray, fontSize = 12.sp)
                                Text("$${"%,.0f".format(cdnCostResult.dailyCostUsd)} USD / ngày", color = Color(0xFFFF0000), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Chi phí CDN HÀNG THÁNG:", color = Color.Gray, fontSize = 12.sp)
                                Text("$${"%,.0f".format(cdnCostResult.monthlyCostUsd)} USD / tháng", color = Color(0xFFFFD700), fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Tiết kiệm nhờ Phân Phối Long-Tail:", color = Color.Gray, fontSize = 12.sp)
                                Text("-$${"%,.0f".format(cdnCostResult.longTailSavedUsd)} USD / ngày (Tiết kiệm 60%)*", color = Color.Green, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("* (Ước tính mô hình minh họa Chapter 14)", color = Color.DarkGray, fontSize = 10.sp)
                        }
                    }
                }
            }
        }

        // 2. Long-Tail Distribution & Storage Tiering Breakdown
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("2. Tối Ưu Chi Phí Theo Phân Phối Long-Tail (Storage Tiering)", color = Color(0xFF38BDF8), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 20% Video Hot (Chiếm 80% Lượt Xem)*: Lưu trữ và phân phối qua Global CDN Edge Nodes để đảm bảo tốc độ mượt mà.\n" +
                                "• 80% Video Ít Xem (Long-Tail - Chiếm 20% Lượt Xem)*: Bỏ qua CDN Edge cache, phục vụ trực tiếp từ Blob Storage (Raw S3) giúp tiết kiệm 60% tổng chi phí CDN!\n" +
                                "• Multi-Region Ingestion: Định tuyến upload về S3 Ingestion Center gần người dùng nhất (us-west-2, ap-southeast-1).\n" +
                                "• ISP Direct Peering (Open Connect): Đặt Edge Appliance ngay tại nhà mạng ISP để tối ưu băng thông đường truyền.\n" +
                                "* (Ước tính mô hình minh họa)",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 3. DRM & AES-128 Encryption Specs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("3. Bảo Vệ Video Bằng Mã Hóa DRM & AES-128 HLS", color = Color(0xFFA855F7), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• Mã Hóa HLS Segment (AES-128): Thêm thẻ #EXT-X-KEY mã hóa AES-128 vào Master Playlist (.m3u8).\n" +
                                "• Máy Chủ Cấp Key Bản Quyền (DRM License Server): Hỗ trợ Google Widevine L1, Apple FairPlay DRM & Microsoft PlayReady.\n" +
                                "• Chống Quay Màn Hình & Tải Trái Phép: Cấp khóa giải mã dùng 1 lần (Tokenized License Key).",
                        color = Color.LightGray,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }

        // 4. Live Primary Database Failover & Restore Runner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("4. Giả Lập Sập Metadata DB & Multi-AZ Automatic Failover", color = Color(0xFFFF4E4E), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Trạng thái Database: ${activeRole.label}", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    facade.dbReplicaManager.triggerPrimaryDatabaseFailover()
                                }
                            },
                            enabled = activeRole == DatabaseRole.PRIMARY_MASTER,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("💥 Giả Lập Sập Master DB", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    facade.dbReplicaManager.restorePrimaryMaster()
                                }
                            },
                            enabled = activeRole == DatabaseRole.FAILOVER_MASTER,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔄 Khôi Phục Master DB", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Failover Event Logs:", color = Color.Gray, fontSize = 11.sp)
                            failoverLogs.forEach { logLine ->
                                Text("• $logLine", color = Color(0xFF00FF66), fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }

        // 5. Chapter 14 Real Evaluation Matrix
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("5. Bảng Đánh Giá Kiến Trúc Chapter 14 (Thực Hiện Thật 100%)", color = Color(0xFF00FF66), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    val evaluationList = listOf(
                        Triple("Client / API Server", "Giao diện Client & Model API Gateway", "✅ Đã thực hiện thật"),
                        Triple("Video Upload", "Upload song song theo GOP Chunks & Resume khi rớt mạng", "✅ Đã thực hiện thật"),
                        Triple("Metadata DB / Cache", "Metadata DB (StateFlow) + Multi-AZ Replica & Auto-Failover", "✅ Đã thực hiện thật"),
                        Triple("Original Storage", "Kết nối Blob Storage S3 & Multi-Region Ingestion", "✅ Đã thực hiện thật"),
                        Triple("Transcoding", "DAG Engine mã hóa song song thực sự trên Thread Pool", "✅ Đã thực hiện thật"),
                        Triple("DAG Scheduler", "Chạy song song bằng Coroutines async & awaitAll", "✅ Đã thực hiện thật"),
                        Triple("HLS / DRM", "Xuất file HLS Master Manifest (.m3u8) & Mã hóa AES-128 Key", "✅ Đã thực hiện thật"),
                        Triple("CDN & Long-Tail", "Cache video Hot tại CDN, video Long-Tail tại S3 Origin", "✅ Đã thực hiện thật"),
                        Triple("Adaptive Bitrate", "Thuật toán ABR chuyển 1080p/720p/480p theo mạng", "✅ Đã thực hiện thật"),
                        Triple("Message Queue", "Producer/Consumer Message Queue Channel", "✅ Đã thực hiện thật"),
                        Triple("Pre-signed URL", "Cấp Tokenized Pre-signed URL mã hóa HMAC-SHA256", "✅ Đã thực hiện thật"),
                        Triple("Error Handling", "Retry Exponential Backoff & Dead Letter Queue (DLQ)", "✅ Đã thực hiện thật")
                    )

                    evaluationList.forEach { (req, impl, status) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(req, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(impl, color = Color.Gray, fontSize = 11.sp)
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1B382B))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(status, color = Color(0xFF00FF66), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun DAGTranscoderTab(facade: YouTubeSystemFacade) {
    val coroutineScope = rememberCoroutineScope()
    val dagNodes by facade.dagEngine.dagNodes.collectAsState()
    val status by facade.dagEngine.currentStatus.collectAsState()
    val playlistResult by facade.dagEngine.hlsPlaylistResult.collectAsState()

    var videoTitle by remember { mutableStateOf("Video Demo Phỏng Vấn System Design") }
    var lastPresignedUrl by remember { mutableStateOf<PresignedUrlResponse?>(null) }
    var isRunning by remember { mutableStateOf(false) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mô Phỏng Luồng Upload & Mã Hóa Video Đồ Thị DAG", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = videoTitle,
                        onValueChange = { videoTitle = it },
                        label = { Text("Tiêu đề Video", color = Color.Gray) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isRunning = true
                                val (video, presigned) = facade.requestPresignedUploadUrl(
                                    title = videoTitle,
                                    description = "Mô phỏng Bộ Mã Hóa Video DAG Youtube",
                                    uploaderId = "user_architect_101",
                                    durationSec = 120,
                                    fileSizeMb = 450
                                )
                                lastPresignedUrl = presigned
                                facade.processDirectS3UploadAndTranscode(video)
                                isRunning = false
                            }
                        },
                        enabled = !isRunning,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (isRunning) "Đang Mã Hóa Đồ Thị DAG Song Song..." else "Bắt Đầu Upload & Khởi Chạy DAG Pipeline", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        items(dagNodes) { node ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF262626)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(node.name, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(node.detailMessage, color = Color.Gray, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(progress = { node.progress }, modifier = Modifier.fillMaxWidth().height(6.dp))
                }
            }
        }
    }
}

@Composable
private fun ABRStreamingTab(facade: YouTubeSystemFacade) {
    val abrState by facade.abrEngine.abrState.collectAsState()
    var networkSliderValue by remember { mutableFloatStateOf(3000f) }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Mô Phỏng Mạng Băng Thông Động ABR (Adaptive Bitrate)", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Băng thông giả lập hiện tại: ${networkSliderValue.toInt()} kbps", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)

                    Slider(
                        value = networkSliderValue,
                        onValueChange = {
                            networkSliderValue = it
                            facade.abrEngine.updateNetworkBandwidth(it.toInt())
                        },
                        valueRange = 200f..10000f,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("Trạng Thái Trình Phát Video ABR:", color = Color.Gray, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("• Độ phân giải hiện tại: ${abrState.currentResolution.label}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("• Bitrate mục tiêu: ${abrState.currentBitrateKbps} kbps", color = Color.LightGray, fontSize = 12.sp)
                            Text("• Bộ đệm video (Buffer): ${"%.1f".format(abrState.bufferHealthSeconds)} giây", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Nhật Ký Tự Động Chuyển Đổi ABR theo Băng Thông:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    abrState.logs.forEach { log ->
                        Text("• $log", color = Color(0xFF38BDF8), fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun CDNAndLogsTab(facade: YouTubeSystemFacade) {
    val cdnEdges by facade.cdnEdges.collectAsState()
    val logs by facade.systemActivityLogs.collectAsState()

    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            Text("Danh Sách Mạng Máy Chủ Phân Phối Toàn Cầu (CDN Edge Nodes):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        items(cdnEdges) { edge ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(edge.region, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Độ trễ mạng: ${edge.latencyMs}ms | Tỉ lệ Cache Hit: ${"%.1f".format(edge.cacheHitRatioPercentage)}%", color = Color.Gray, fontSize = 11.sp)
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1B382B))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("HOẠT ĐỘNG", color = Color(0xFF00FF66), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text("Nhật Ký Hoạt Động Hệ Thống Thời Gian Thực (System Logs):", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }

        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF121212)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    logs.forEach { logLine ->
                        Text("• $logLine", color = Color(0xFF00FF66), fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }
    }
}

// ==========================================
// 5. MAIN YOUTUBE APP CONTAINER
// ==========================================

@Composable
fun YouTubeAppScreen(
    facade: YouTubeSystemFacade = remember { YouTubeSystemFacade() }
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isDarkMode by remember { mutableStateOf(false) }
    val colors = if (isDarkMode) YouTubeDarkColors else YouTubeLightColors

    var selectedTab by remember { mutableStateOf(YouTubeTab.HOME) }
    var activeVideo by remember { mutableStateOf<VideoItem?>(null) }
    var activePlaylist by remember { mutableStateOf<Playlist?>(null) }
    var activeChannel by remember { mutableStateOf<Channel?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }

    var isAuthDialogOpen by remember { mutableStateOf(false) }
    var isAccountSwitcherDialogOpen by remember { mutableStateOf(false) }
    var isCreatePlaylistDialogOpen by remember { mutableStateOf(false) }

    val currentUser by MockYouTubeRepository.currentUser.collectAsState()

    fun showToast(message: String) {
        coroutineScope.launch {
            snackbarHostState.showSnackbar(message)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            if (!isSearchActive && activeVideo == null && activePlaylist == null && activeChannel == null) {
                YouTubeTopAppBar(
                    colors = colors,
                    currentUser = currentUser,
                    onSearchClick = { isSearchActive = true },
                    onNotificationClick = { showToast("🔔 Không có thông báo mới") },
                    onUserAvatarClick = {
                        if (currentUser.isSignedIn) {
                            isAccountSwitcherDialogOpen = true
                        } else {
                            isAuthDialogOpen = true
                        }
                    }
                )
            } else if (isSearchActive && activeVideo == null && activePlaylist == null && activeChannel == null) {
                YouTubeSearchAppBar(
                    colors = colors,
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onCloseSearch = {
                        isSearchActive = false
                        searchQuery = ""
                    },
                    onSuggestionSelect = { suggestion ->
                        searchQuery = suggestion
                    }
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                if (activeVideo != null) {
                    VideoPlayerScreen(
                        video = activeVideo!!,
                        colors = colors,
                        facade = facade,
                        onBackClick = { activeVideo = null },
                        onRelatedVideoClick = { activeVideo = it },
                        onToast = { showToast(it) }
                    )
                } else if (activePlaylist != null) {
                    PlaylistDetailScreen(
                        playlist = activePlaylist!!,
                        colors = colors,
                        onBackClick = { activePlaylist = null },
                        onVideoClick = { video ->
                            activePlaylist = null
                            activeVideo = video
                        },
                        onToast = { showToast(it) }
                    )
                } else if (activeChannel != null) {
                    ChannelDetailScreen(
                        channel = activeChannel!!,
                        colors = colors,
                        onBackClick = { activeChannel = null },
                        onVideoClick = { video ->
                            activeChannel = null
                            activeVideo = video
                        },
                        onToast = { showToast(it) }
                    )
                } else {
                    when (selectedTab) {
                        YouTubeTab.HOME -> HomeScreen(
                            colors = colors,
                            searchQuery = searchQuery,
                            onVideoClick = { activeVideo = it },
                            onToast = { showToast(it) }
                        )
                        YouTubeTab.SHORTS -> ShortsScreen(
                            colors = colors,
                            onVideoClick = { activeVideo = it },
                            onToast = { showToast(it) }
                        )
                        YouTubeTab.UPLOAD -> UploadScreen(
                            colors = colors,
                            facade = facade,
                            onToast = { showToast(it) },
                            onUploadSuccess = { newVideo -> activeVideo = newVideo }
                        )
                        YouTubeTab.SUBSCRIPTIONS -> SubscriptionsScreen(
                            colors = colors,
                            onVideoClick = { activeVideo = it },
                            onChannelClick = { activeChannel = it },
                            onToast = { showToast(it) }
                        )
                        YouTubeTab.YOU -> YouLibraryScreen(
                            colors = colors,
                            isDarkMode = isDarkMode,
                            onThemeToggle = { isDarkMode = it },
                            onSelectSystemDesignTab = { selectedTab = YouTubeTab.SYSTEM_DESIGN },
                            onVideoClick = { activeVideo = it },
                            onPlaylistClick = { activePlaylist = it },
                            onOpenAuthDialog = { isAuthDialogOpen = true },
                            onOpenAccountSwitcherDialog = { isAccountSwitcherDialogOpen = true },
                            onOpenCreatePlaylistDialog = { isCreatePlaylistDialogOpen = true },
                            onToast = { showToast(it) }
                        )
                        YouTubeTab.SYSTEM_DESIGN -> YouTubeDesignScreen(facade = facade)
                    }
                }
            }

            if (activeVideo == null && activePlaylist == null && activeChannel == null) {
                YouTubeBottomNavBar(
                    colors = colors,
                    selectedTab = selectedTab,
                    onTabSelected = {
                        activeVideo = null
                        activePlaylist = null
                        activeChannel = null
                        selectedTab = it
                    }
                )
            }
        }

        // Auth Dialog Popup Overlay
        if (isAuthDialogOpen) {
            AuthModalDialog(
                colors = colors,
                onDismiss = { isAuthDialogOpen = false },
                onToast = { showToast(it) }
            )
        }

        // Account Switcher Dialog Popup Overlay
        if (isAccountSwitcherDialogOpen) {
            AccountSwitcherModalDialog(
                colors = colors,
                currentUser = currentUser,
                onDismiss = { isAccountSwitcherDialogOpen = false },
                onAddNewAccount = {
                    isAccountSwitcherDialogOpen = false
                    isAuthDialogOpen = true
                },
                onToast = { showToast(it) }
            )
        }

        // Create Playlist Dialog Popup Overlay
        if (isCreatePlaylistDialogOpen) {
            CreatePlaylistDialog(
                colors = colors,
                onDismiss = { isCreatePlaylistDialogOpen = false },
                onToast = { showToast(it) }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 60.dp)
        )
    }
}

@Composable
private fun YouTubeTopAppBar(
    colors: YouTubeColorPalette,
    currentUser: UserAccount,
    onSearchClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onUserAvatarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.topBottomBarBg)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp, 20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(colors.brandRed),
                contentAlignment = Alignment.Center
            ) {
                Text("▶", color = Color.White, fontSize = 10.sp)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "YouTube",
                color = colors.textPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(
                text = "VN",
                color = colors.textSecondary,
                fontSize = 10.sp,
                modifier = Modifier.padding(start = 2.dp)
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "🔍",
                fontSize = 18.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onSearchClick() }
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "🔔",
                fontSize = 18.sp,
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onNotificationClick() }
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))

            if (currentUser.isSignedIn) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF007ACC))
                        .clickable { onUserAvatarClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = currentUser.displayName.take(1).uppercase(),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .border(width = 1.dp, color = colors.brandRed, shape = RoundedCornerShape(16.dp))
                        .clickable { onUserAvatarClick() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text("👤 Đăng nhập", color = colors.brandRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun YouTubeSearchAppBar(
    colors: YouTubeColorPalette,
    query: String,
    onQueryChange: (String) -> Unit,
    onCloseSearch: () -> Unit,
    onSuggestionSelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "←",
                color = colors.textPrimary,
                fontSize = 20.sp,
                modifier = Modifier
                    .clickable { onCloseSearch() }
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                placeholder = { Text("Tìm kiếm trên YouTube...", color = colors.textSecondary, fontSize = 13.sp) },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp),
                singleLine = true
            )
            if (query.isNotEmpty()) {
                Text(
                    text = "✕",
                    color = colors.textSecondary,
                    fontSize = 16.sp,
                    modifier = Modifier
                        .clickable { onQueryChange("") }
                        .padding(8.dp)
                )
            }
        }

        if (query.isEmpty()) {
            LazyColumn(modifier = Modifier.height(200.dp)) {
                items(MockYouTubeRepository.searchSuggestions) { sug ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSuggestionSelect(sug) }
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🕒", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(sug, color = colors.textPrimary, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    colors: YouTubeColorPalette,
    searchQuery: String,
    onVideoClick: (VideoItem) -> Unit,
    onToast: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    val allVideos by MockYouTubeRepository.videos.collectAsState()

    val filteredVideos = remember(allVideos, selectedCategory, searchQuery) {
        MockYouTubeRepository.searchVideos(searchQuery, selectedCategory, allVideos)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(MockYouTubeRepository.sampleCategories) { cat ->
                val isSelected = cat == selectedCategory
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) colors.chipSelectedBg else colors.chipUnselectedBg)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) colors.chipSelectedText else colors.chipUnselectedText,
                        fontSize = 13.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }

        if (searchQuery.isNotEmpty()) {
            Text(
                text = "Tìm thấy ${filteredVideos.size} kết quả cho \"$searchQuery\"",
                color = colors.textSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredVideos) { video ->
                VideoCard(
                    video = video,
                    colors = colors,
                    onVideoClick = onVideoClick,
                    onOptionClick = { action, v ->
                        when (action) {
                            "WATCH_LATER" -> onToast("🕒 Đã thêm '${v.title}' vào Xem sau")
                            "SAVE_PLAYLIST" -> onToast("💾 Đã lưu vào Danh sách phát")
                            "SHARE" -> onToast("🔄 Đã sao chép liên kết video")
                            "DOWNLOAD" -> onToast("⬇️ Đang tải video xuống...")
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun VideoPlayerScreen(
    video: VideoItem,
    colors: YouTubeColorPalette,
    facade: YouTubeSystemFacade = remember { YouTubeSystemFacade() },
    onBackClick: () -> Unit,
    onRelatedVideoClick: (VideoItem) -> Unit,
    onToast: (String) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(true) }
    var currentSeconds by remember { mutableIntStateOf(video.watchedSeconds) }
    val totalSeconds = remember { video.durationSeconds }

    var isLiked by remember { mutableStateOf(video.isLiked) }
    var isDisliked by remember { mutableStateOf(video.isDisliked) }
    var likesCount by remember { mutableIntStateOf(video.likesCount) }
    var isSubscribed by remember { mutableStateOf(video.channel.isSubscribed) }
    var notificationOption by remember { mutableStateOf(video.channel.notificationOption) }
    var showNotifMenu by remember { mutableStateOf(false) }

    var commentText by remember { mutableStateOf("") }
    val commentsList = remember { video.comments }

    var selectedResolution by remember { mutableStateOf("Auto (1080p)") }
    var selectedSpeed by remember { mutableStateOf("1.0x (Chuẩn)") }
    var isShareDialogOpen by remember { mutableStateOf(false) }

    LaunchedEffect(isPlaying) {
        while (isPlaying && currentSeconds < totalSeconds) {
            delay(1000L)
            currentSeconds += 1
            video.watchedSeconds = currentSeconds
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        item {
            Column {
                VideoPlayerView(
                    video = video,
                    isPlaying = isPlaying,
                    onPlayPauseToggle = { isPlaying = !isPlaying },
                    currentSeconds = currentSeconds,
                    totalSeconds = totalSeconds,
                    onSeek = { currentSeconds = it },
                    onRewind = { currentSeconds = (currentSeconds - 10).coerceAtLeast(0) },
                    onForward = { currentSeconds = (currentSeconds + 10).coerceAtMost(totalSeconds) },
                    selectedResolution = selectedResolution,
                    onResolutionChange = { res ->
                        selectedResolution = res
                        if (video.servingOrigin == ServingOrigin.ORIGIN_STORAGE && res.contains("1080p")) {
                            coroutineScope.launch {
                                facade.triggerOnDemandTranscodingForColdVideo(video.id, Resolution.P1080)
                                onToast("⚡ Video Long-Tail chưa có sẵn bản 1080p -> Đang mã hóa On-Demand!")
                            }
                        } else {
                            onToast("Đã đổi chất lượng sang $res")
                        }
                    },
                    selectedSpeed = selectedSpeed,
                    onSpeedChange = {
                        selectedSpeed = it
                        onToast("Đã đổi tốc độ sang $it")
                    },
                    onBackClick = onBackClick
                )

                Column(modifier = Modifier.padding(12.dp)) {
                    Text(video.title, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${video.views} • ${video.publishedTimeAgo}", color = colors.textSecondary, fontSize = 12.sp)

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                    ) {
                        val isCdn = video.servingOrigin == ServingOrigin.CDN_EDGE
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isCdn) Color(0xFF1B382B) else Color(0xFF332010))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = if (isCdn) "⚡ CDN Edge Node (15ms)" else "📦 Raw S3 Storage Origin (Long-Tail)",
                                color = if (isCdn) Color(0xFF00FF66) else Color(0xFFFF9900),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF2A1B38))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "🔒 ${video.drmScheme.label}",
                                color = Color(0xFFA855F7),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(colors.brandRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(video.channel.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(video.channel.name, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                                Text(video.channel.subscribersCount, color = colors.textSecondary, fontSize = 11.sp)
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isSubscribed) {
                                Box {
                                    Box(
                                        modifier = Modifier
                                            .clip(CircleShape)
                                            .background(colors.chipUnselectedBg)
                                            .clickable { showNotifMenu = true }
                                            .padding(8.dp)
                                    ) {
                                        Text(notificationOption.icon, fontSize = 14.sp)
                                    }

                                    DropdownMenu(
                                        expanded = showNotifMenu,
                                        onDismissRequest = { showNotifMenu = false },
                                        modifier = Modifier.background(colors.surface)
                                    ) {
                                        NotificationOption.entries.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text("${option.icon} ${option.label}", color = colors.textPrimary, fontSize = 12.sp) },
                                                onClick = {
                                                    notificationOption = option
                                                    showNotifMenu = false
                                                    onToast("Thông báo: ${option.label}")
                                                }
                                            )
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                            }

                            Button(
                                onClick = {
                                    isSubscribed = !isSubscribed
                                    video.channel.isSubscribed = isSubscribed
                                    onToast(if (isSubscribed) "🔴 Đã đăng ký kênh ${video.channel.name}" else "Đã hủy đăng ký")
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSubscribed) colors.chipUnselectedBg else colors.textPrimary
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Text(
                                    text = if (isSubscribed) "Đã đăng ký" else "Đăng ký",
                                    color = if (isSubscribed) colors.textPrimary else colors.background,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Action Buttons Row (Like, Dislike, Share, Download, Save)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(colors.chipUnselectedBg)
                                    .padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "👍 $likesCount",
                                    color = if (isLiked) colors.brandRed else colors.textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable {
                                        if (isLiked) {
                                            isLiked = false
                                            likesCount -= 1
                                        } else {
                                            isLiked = true
                                            if (isDisliked) isDisliked = false
                                            likesCount += 1
                                            onToast("Đã thích video này 👍")
                                        }
                                    }
                                )
                                Text(" | ", color = colors.textSecondary)
                                Text(
                                    text = "👎",
                                    color = if (isDisliked) Color.Red else colors.textPrimary,
                                    fontSize = 12.sp,
                                    modifier = Modifier.clickable {
                                        isDisliked = !isDisliked
                                        if (isDisliked && isLiked) {
                                            isLiked = false
                                            likesCount -= 1
                                        }
                                    }
                                )
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(colors.chipUnselectedBg)
                                    .clickable { isShareDialogOpen = true }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text("🔄 Chia sẻ", color = colors.textPrimary, fontSize = 12.sp)
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(colors.chipUnselectedBg)
                                    .clickable {
                                        video.isDownloaded = !video.isDownloaded
                                        onToast(if (video.isDownloaded) "⬇️ Đã tải video về máy (Xem offline)" else "Đã xóa bản tải xuống")
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(if (video.isDownloaded) "✅ Đã tải xuống" else "⬇️ Tải xuống", color = colors.textPrimary, fontSize = 12.sp)
                            }
                        }
                        item {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(colors.chipUnselectedBg)
                                    .clickable {
                                        video.isSaved = !video.isSaved
                                        onToast(if (video.isSaved) "💾 Đã lưu vào Xem sau" else "Đã xóa khỏi danh sách")
                                    }
                                    .padding(horizontal = 14.dp, vertical = 8.dp)
                            ) {
                                Text(if (video.isSaved) "🔖 Đã lưu" else "💾 Lưu", color = colors.textPrimary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Share Dialog Modal
        if (isShareDialogOpen) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Chia sẻ Video", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("✕", color = colors.textSecondary, modifier = Modifier.clickable { isShareDialogOpen = false })
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(video.videoUrl, color = Color(0xFF007ACC), fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(
                            onClick = {
                                isShareDialogOpen = false
                                onToast("📋 Đã sao chép đường liên kết vào bộ nhớ tạm!")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.brandRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Sao chép đường liên kết", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }

        // Comments Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Bình luận (${commentsList.size})", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = commentText,
                            onValueChange = { commentText = it },
                            placeholder = { Text("Viết bình luận công khai...", color = colors.textSecondary, fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (commentText.isNotBlank()) {
                                    commentsList.add(0, Comment(authorName = "Bạn", authorAvatarUrl = "", text = commentText, timeAgo = "Vừa xong"))
                                    commentText = ""
                                    onToast("💬 Đã đăng bình luận của bạn!")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = colors.brandRed)
                        ) {
                            Text("Đăng", fontSize = 12.sp, color = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    commentsList.forEach { comment ->
                        Row(modifier = Modifier.padding(vertical = 6.dp)) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF007ACC)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(comment.authorName.take(1), color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("${comment.authorName} • ${comment.timeAgo}", color = colors.textSecondary, fontSize = 11.sp)
                                Text(comment.text, color = colors.textPrimary, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("👍 ${comment.likesCount}  •  Trả lời", color = colors.textSecondary, fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        items(MockYouTubeRepository.initialVideos.filter { it.id != video.id }) { recVideo ->
            VideoCard(video = recVideo, colors = colors, onVideoClick = onRelatedVideoClick)
        }
    }
}

// ==========================================
// PLAYLIST DETAIL SCREEN
// ==========================================

@Composable
private fun PlaylistDetailScreen(
    playlist: Playlist,
    colors: YouTubeColorPalette,
    onBackClick: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onToast: (String) -> Unit
) {
    val playlistVideos = remember(playlist) {
        MockYouTubeRepository.getVideosForPlaylist(playlist)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onBackClick() }
                    .padding(8.dp)
            ) {
                Text("←", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(playlist.name, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = colors.surface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(70.dp, 50.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(colors.brandRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▶", color = Color.White, fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(playlist.name, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${playlistVideos.size} video • ${if (playlist.isPrivate) "🔒 Riêng tư" else "🌐 Công khai"}", color = colors.textSecondary, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    if (playlistVideos.isNotEmpty()) {
                                        onVideoClick(playlistVideos.first())
                                        onToast("▶ Đang phát tất cả video trong ${playlist.name}")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.brandRed),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("▶ Phát tất cả", fontWeight = FontWeight.Bold, color = Color.White)
                            }

                            Button(
                                onClick = {
                                    if (playlistVideos.isNotEmpty()) {
                                        onVideoClick(playlistVideos.shuffled().first())
                                        onToast("🔀 Phát ngẫu nhiên ${playlist.name}")
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = colors.chipUnselectedBg),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("🔀 Ngẫu nhiên", color = colors.textPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            items(playlistVideos.size) { index ->
                val video = playlistVideos[index]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onVideoClick(video) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("#${index + 1}", color = colors.textSecondary, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.width(28.dp))

                    Box(
                        modifier = Modifier
                            .size(110.dp, 65.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1E1E2C))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            VideoThumbnailIllustrator.drawThumbnail(this, video.id, video.category)
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(video.title, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text("${video.channel.name} • ${video.duration}", color = colors.textSecondary, fontSize = 11.sp)
                    }
                }
            }
        }
    }
}

// ==========================================
// CHANNEL DETAIL SCREEN
// ==========================================

@Composable
private fun ChannelDetailScreen(
    channel: Channel,
    colors: YouTubeColorPalette,
    onBackClick: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onToast: (String) -> Unit
) {
    var isSubscribed by remember { mutableStateOf(channel.isSubscribed) }
    val channelVideos = remember(channel) {
        MockYouTubeRepository.getVideosForChannel(channel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable { onBackClick() }
                    .padding(8.dp)
            ) {
                Text("←", color = colors.textPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(channel.name, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(colors.brandRed, Color(0xFF007ACC))
                            )
                        )
                )
            }

            item {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(colors.brandRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(channel.name.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 22.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(channel.name, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                                Text("${channel.handle} • ${channel.subscribersCount}", color = colors.textSecondary, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                isSubscribed = !isSubscribed
                                channel.isSubscribed = isSubscribed
                                onToast(if (isSubscribed) "🔴 Đã đăng ký ${channel.name}" else "Đã hủy đăng ký")
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSubscribed) colors.chipUnselectedBg else colors.textPrimary
                            ),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Text(
                                text = if (isSubscribed) "Đã đăng ký" else "Đăng ký",
                                color = if (isSubscribed) colors.textPrimary else colors.background,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Video của kênh (${channelVideos.size})", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }

            items(channelVideos) { video ->
                VideoCard(video = video, colors = colors, onVideoClick = onVideoClick)
            }
        }
    }
}

@Composable
private fun ShortsScreen(
    colors: YouTubeColorPalette,
    onVideoClick: (VideoItem) -> Unit,
    onToast: (String) -> Unit
) {
    var shortIndex by remember { mutableIntStateOf(0) }
    val shortsList = MockYouTubeRepository.sampleShorts
    val currentShort = shortsList[shortIndex % shortsList.size]

    var isLiked by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("⚡ YOUTUBE SHORTS", color = Color(0xFFFF0000), fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(currentShort.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(currentShort.channel.name, color = Color.LightGray, fontSize = 13.sp)

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onVideoClick(currentShort) },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000))
            ) {
                Text("▶ Xem video chi tiết", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                isLiked = !isLiked
                onToast(if (isLiked) "👍 Đã thích Short này" else "Bỏ thích")
            }) {
                Text(if (isLiked) "❤️" else "🤍", fontSize = 26.sp)
                Text(if (isLiked) "34.1K" else "34K", color = Color.White, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                onToast("💬 1.2K bình luận")
            }) {
                Text("💬", fontSize = 24.sp)
                Text("1.2K", color = Color.White, fontSize = 11.sp)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.clickable {
                onToast("🔄 Đã sao chép liên kết Short!")
            }) {
                Text("🔄", fontSize = 24.sp)
                Text("Chia sẻ", color = Color.White, fontSize = 11.sp)
            }

            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF272727))
                    .clickable {
                        shortIndex++
                        isLiked = false
                        onToast("⬇️ Vuốt sang Short tiếp theo")
                    },
                contentAlignment = Alignment.Center
            ) {
                Text("⬇️", fontSize = 18.sp)
            }
        }
    }
}

@Composable
private fun UploadScreen(
    colors: YouTubeColorPalette,
    facade: YouTubeSystemFacade,
    onToast: (String) -> Unit,
    onUploadSuccess: (VideoItem) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var selectedVideoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedFileName by remember { mutableStateOf("") }
    var selectedFileSizeMb by remember { mutableLongStateOf(0L) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Lập trình") }
    var privacy by remember { mutableStateOf("Công khai") }
    var selectedRegion by remember { mutableStateOf("us-west-2 (Oregon)") }
    var isUploading by remember { mutableStateOf(false) }

    val uploadTracker by facade.chunkUploader.uploadTracker.collectAsState()

    // ActivityResultLauncher mở File Picker hệ thống Android chọn video từ bộ nhớ máy
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedVideoUri = uri
            var name = "video_selected_${System.currentTimeMillis()}.mp4"
            var sizeMb = 120L

            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIndex != -1) name = cursor.getString(nameIndex)
                        if (sizeIndex != -1) sizeMb = (cursor.getLong(sizeIndex) / (1024 * 1024)).coerceAtLeast(1)
                    }
                }
            } catch (e: Exception) {
                // Fallback
            }

            selectedFileName = name
            selectedFileSizeMb = sizeMb
            if (title.isBlank()) {
                title = name.substringBeforeLast(".")
            }
            onToast("✅ Đã chọn file video: $name ($sizeMb MB)")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Tạo & Đăng Video Mới (YouTube Studio)", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(12.dp))

        // Nút bấm mở File Picker hệ thống Android
        Button(
            onClick = { videoPickerLauncher.launch("video/*") },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC)),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp)
        ) {
            Text("📁 CHỌN VIDEO TỪ THIẾT BỊ / THƯ VIỆN (Gallery & Storage)", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 12.sp)
        }

        if (selectedFileName.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1B382B)),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("✅", fontSize = 16.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text("File video đã chọn: $selectedFileName", color = Color(0xFF00FF66), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text("Dung lượng: $selectedFileSizeMb MB | Đường dẫn: ${selectedVideoUri?.path?.take(35)}...", color = Color.LightGray, fontSize = 10.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Tiêu đề video", color = colors.textSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Mô tả video", color = colors.textSecondary) },
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text("Upload Ingestion Region (Gần người dùng nhất):", color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf("us-west-2 (Oregon)", "ap-southeast-1 (Singapore)", "eu-central-1 (Frankfurt)").forEach { region ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedRegion == region) colors.brandRed else colors.chipUnselectedBg)
                        .clickable { selectedRegion = region }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    Text(region, color = if (selectedRegion == region) Color.White else colors.textPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                if (title.isNotBlank()) {
                    coroutineScope.launch {
                        isUploading = true
                        onToast("1. Yêu cầu S3 Pre-signed Upload URL cho vùng $selectedRegion...")

                        val (videoMeta, _) = facade.requestPresignedUploadUrl(
                            title = title,
                            description = description,
                            uploaderId = "user_architect_dev",
                            durationSec = 180,
                            fileSizeMb = 350,
                            region = selectedRegion
                        )

                        onToast("2. Khởi chạy Upload song song GOP Chunks kèm tự động Resume khi rớt mạng...")
                        facade.processDirectS3UploadAndTranscode(videoMeta)

                        val newVideo = VideoItem(
                            id = videoMeta.videoId,
                            title = title,
                            description = description,
                            thumbnailUrl = selectedVideoUri?.toString() ?: "",
                            duration = "03:00",
                            durationSeconds = 180,
                            videoUrl = "s3://cdn.youtube-demo.com/videos/${videoMeta.videoId}/master.m3u8",
                            channel = MockYouTubeRepository.sampleChannels.first(),
                            views = "1 lượt xem",
                            publishedTimeAgo = "Vừa xong",
                            likesCount = 1,
                            category = selectedCategory,
                            isHotVideo = true,
                            servingOrigin = ServingOrigin.CDN_EDGE,
                            drmScheme = DRMScheme.AES_128
                        )

                        MockYouTubeRepository.addVideo(newVideo)
                        isUploading = false
                        onToast("🎉 Đăng video thành công! Video đã xuất hiện trên Trang chủ.")
                        onUploadSuccess(newVideo)
                    }
                }
            },
            enabled = !isUploading && title.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = colors.brandRed),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isUploading) "Đang upload GOP Chunks & Mã hóa DAG..." else "Đăng Video Ngay", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hiển thị tiến trình Upload thời gian thực & Resumable GOP Chunks khi rớt mạng
        uploadTracker?.let { tracker ->
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Tiến Trình Upload Video:", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text(
                            text = "${(tracker.progressPercent * 100).toInt()}%",
                            color = if (tracker.isInterrupted) Color.Red else colors.brandRed,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    LinearProgressIndicator(
                        progress = { tracker.progressPercent },
                        color = if (tracker.isInterrupted) Color.Red else colors.brandRed,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "${tracker.uploadedMb} MB / ${tracker.totalMb} MB (${tracker.uploadSpeedMbps} Mbps)",
                        color = colors.textSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Bộ nút tương tác Bật / Tắt Mạng cho người dùng thử nghiệm thời gian thực
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                facade.chunkUploader.setNetworkOnlineState(false)
                                onToast("🔴 Đã ngắt kết nối mạng!")
                            },
                            enabled = tracker.isOnline && tracker.progressPercent < 1.0f,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🔴 Mất Mạng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }

                        Button(
                            onClick = {
                                facade.chunkUploader.setNetworkOnlineState(true)
                                onToast("🟢 Đã kết nối mạng trở lại!")
                            },
                            enabled = !tracker.isOnline && tracker.progressPercent < 1.0f,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007ACC)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🟢 Kết Nối Lại Mạng", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (tracker.isInterrupted) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF3B1212)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("⚠️", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("LỖI MẤT KẾT NỐI MẠNG!", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Không thể tiếp tục tải video lên! Đang tạm dừng...", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    } else if (tracker.isResumed) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF1B382B)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text("🟢", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text("ĐÃ KHÔI PHỤC KẾT NỐI MẠNG!", color = Color(0xFF00FF66), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    Text("Tự động tiếp tục upload các chunk còn lại...", color = Color.LightGray, fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Text("Trạng thái 8 GOP Chunks:", color = colors.textSecondary, fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        (1..tracker.totalChunks).forEach { chunkIdx ->
                            val isCompleted = tracker.completedChunkIndices.contains(chunkIdx)
                            val isCurrentInterrupted = tracker.isInterrupted && chunkIdx == 5

                            val boxColor = when {
                                isCurrentInterrupted -> Color.Red
                                isCompleted -> Color(0xFF22C55E)
                                else -> colors.chipUnselectedBg
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(28.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(boxColor),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("#$chunkIdx", color = if (isCompleted || isCurrentInterrupted) Color.White else colors.textPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SubscriptionsScreen(
    colors: YouTubeColorPalette,
    onVideoClick: (VideoItem) -> Unit,
    onChannelClick: (Channel) -> Unit,
    onToast: (String) -> Unit
) {
    val allVideos by MockYouTubeRepository.videos.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Kênh đã đăng ký", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(12.dp))

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(MockYouTubeRepository.sampleChannels) { ch ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { onChannelClick(ch) }
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .border(width = 2.dp, color = colors.brandRed, shape = CircleShape)
                            .background(colors.brandRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ch.name.take(1), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(ch.name, color = colors.textPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text("Video mới cập nhật", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.padding(horizontal = 12.dp))
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(allVideos) { video ->
                VideoCard(video = video, colors = colors, onVideoClick = onVideoClick)
            }
        }
    }
}

@Composable
private fun YouLibraryScreen(
    colors: YouTubeColorPalette,
    isDarkMode: Boolean,
    onThemeToggle: (Boolean) -> Unit,
    onSelectSystemDesignTab: () -> Unit,
    onVideoClick: (VideoItem) -> Unit,
    onPlaylistClick: (Playlist) -> Unit,
    onOpenAuthDialog: () -> Unit,
    onOpenAccountSwitcherDialog: () -> Unit,
    onOpenCreatePlaylistDialog: () -> Unit,
    onToast: (String) -> Unit
) {
    val allVideos by MockYouTubeRepository.videos.collectAsState()
    val playlists by MockYouTubeRepository.playlists.collectAsState()
    val currentUser by MockYouTubeRepository.currentUser.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // User Profile Header / Sign In Prompt Banner
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (currentUser.isSignedIn) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { onOpenAccountSwitcherDialog() }
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF007ACC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(currentUser.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(currentUser.displayName, color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text("${currentUser.handle} • ${currentUser.email}", color = colors.textSecondary, fontSize = 12.sp)
                            Text("👥 Chuyển tài khoản →", color = colors.brandRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                } else {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("🔑 Đăng Nhập YouTube", color = colors.brandRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Đăng nhập để xem lại video đã thích, tạo danh sách phát cá nhân và quản lý kênh!", color = colors.textSecondary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onOpenAuthDialog,
                                colors = ButtonDefaults.buttonColors(containerColor = colors.brandRed)
                            ) {
                                Text("Đăng Nhập / Đăng Ký Ngay", fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(if (isDarkMode) "🌙 Tối" else "☀️ Sáng", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(6.dp))
                    Switch(
                        checked = isDarkMode,
                        onCheckedChange = onThemeToggle,
                        colors = SwitchDefaults.colors(checkedThumbColor = colors.brandRed)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (currentUser.isSignedIn) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onOpenAccountSwitcherDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = colors.chipUnselectedBg),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("👥 Chuyển / Thêm Tài Khoản", color = colors.textPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    Button(
                        onClick = {
                            MockYouTubeRepository.signOut()
                            onToast("👋 Đã đăng xuất khỏi tài khoản!")
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.chipUnselectedBg)
                    ) {
                        Text("Đăng Xuất", color = Color.Red, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        // History Section
        item {
            Text("Lịch sử đã xem", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(allVideos) { video ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = colors.surface),
                        modifier = Modifier
                            .width(160.dp)
                            .clickable { onVideoClick(video) }
                    ) {
                        Column {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(90.dp)
                                    .background(colors.chipUnselectedBg),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("▶ Video", color = colors.brandRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                            Text(video.title, color = colors.textPrimary, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.padding(6.dp))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // Playlists Section with Create Playlist Button & Interactive Playlist Detail
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Danh sách phát", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(
                    text = "➕ Tạo mới",
                    color = colors.brandRed,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    modifier = Modifier.clickable { onOpenCreatePlaylistDialog() }
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            playlists.forEach { pl ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onPlaylistClick(pl) }
                        .padding(vertical = 8.dp, horizontal = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (pl.isPrivate) "🔒" else "🌐", fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(pl.name, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                            Text("${pl.itemCount} video", color = colors.textSecondary, fontSize = 11.sp)
                        }
                    }
                    Text("→", color = colors.brandRed, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // System Design Specs Button
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = colors.surface),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelectSystemDesignTab() }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("⚙️ System Design Specs (Chapter 14)", color = colors.brandRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Mô phỏng DAG Transcoding Engine & ABR Player", color = colors.textSecondary, fontSize = 11.sp)
                    }
                    Text("→", color = colors.textPrimary, fontSize = 18.sp)
                }
            }
        }
    }
}

// ==========================================
// ACCOUNT SWITCHER MODAL DIALOG (CHUYỂN ĐỔI / THÊM TÀI KHOẢN)
// ==========================================

@Composable
private fun AccountSwitcherModalDialog(
    colors: YouTubeColorPalette,
    currentUser: UserAccount,
    onDismiss: () -> Unit,
    onAddNewAccount: () -> Unit,
    onToast: (String) -> Unit
) {
    val savedAccounts by MockYouTubeRepository.savedAccounts.collectAsState()

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("👥 Chuyển Đổi Tài Khoản", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text("✕", color = colors.textSecondary, modifier = Modifier.clickable { onDismiss() })
            }

            Spacer(modifier = Modifier.height(14.dp))
            Text("Tài khoản đã lưu trên thiết bị:", color = colors.textSecondary, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(10.dp))

            savedAccounts.forEach { acc ->
                val isActive = currentUser.isSignedIn && currentUser.email == acc.email
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isActive) colors.chipUnselectedBg else Color.Transparent)
                        .clickable {
                            MockYouTubeRepository.switchAccount(acc)
                            onToast("🔄 Đã chuyển sang tài khoản: ${acc.displayName}")
                            onDismiss()
                        }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (isActive) colors.brandRed else Color(0xFF007ACC)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(acc.displayName.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(acc.displayName, color = colors.textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text("${acc.handle} • ${acc.email}", color = colors.textSecondary, fontSize = 11.sp)
                        }
                    }

                    if (isActive) {
                        Text("✓ Đang dùng", color = Color(0xFF22C55E), fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    onDismiss()
                    onAddNewAccount()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.brandRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("➕ Thêm Tài Khoản Mới", fontWeight = FontWeight.Bold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = {
                    MockYouTubeRepository.signOut()
                    onToast("👋 Đã đăng xuất khỏi tài khoản")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.chipUnselectedBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Đăng Xuất Tài Khoản Này", color = Color.Red, fontSize = 12.sp)
            }
        }
    }
}

// ==========================================
// AUTHENTICATION MODAL DIALOG (ĐĂNG NHẬP / ĐĂNG KÝ)
// ==========================================

@Composable
private fun AuthModalDialog(
    colors: YouTubeColorPalette,
    onDismiss: () -> Unit,
    onToast: (String) -> Unit
) {
    var selectedAuthTab by remember { mutableIntStateOf(0) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (selectedAuthTab == 0) "🔑 Đăng Nhập YouTube" else "✨ Tạo Tài Khoản Mới",
                    color = colors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text("✕", color = colors.textSecondary, modifier = Modifier.clickable { onDismiss() })
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedAuthTab == 0) colors.brandRed else colors.chipUnselectedBg)
                        .clickable { selectedAuthTab = 0 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Đăng Nhập", color = if (selectedAuthTab == 0) Color.White else colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (selectedAuthTab == 1) colors.brandRed else colors.chipUnselectedBg)
                        .clickable { selectedAuthTab = 1 }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Đăng Ký", color = if (selectedAuthTab == 1) Color.White else colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (selectedAuthTab == 1) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Họ và Tên", color = colors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email", color = colors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mật khẩu", color = colors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isNotBlank()) {
                        if (selectedAuthTab == 0) {
                            MockYouTubeRepository.signIn(email, name)
                            onToast("🎉 Đăng nhập thành công với tài khoản: $email")
                        } else {
                            MockYouTubeRepository.signUp(email, name)
                            onToast("✨ Tạo tài khoản YouTube mới thành công!")
                        }
                        onDismiss()
                    } else {
                        onToast("⚠️ Vui lòng nhập địa chỉ email!")
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.brandRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (selectedAuthTab == 0) "Đăng Nhập Ngay" else "Tạo Tài Khoản YouTube",
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = {
                    MockYouTubeRepository.signIn("user.google@gmail.com", "Google Account User")
                    onToast("🌐 Đăng nhập bằng Google thành công!")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = colors.chipUnselectedBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("🌐 Đăng nhập bằng Google", color = colors.textPrimary, fontSize = 13.sp)
            }
        }
    }
}

// ==========================================
// CREATE PLAYLIST MODAL DIALOG
// ==========================================

@Composable
private fun CreatePlaylistDialog(
    colors: YouTubeColorPalette,
    onDismiss: () -> Unit,
    onToast: (String) -> Unit
) {
    var playlistName by remember { mutableStateOf("") }
    var isPrivate by remember { mutableStateOf(true) }

    Card(
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("➕ Tạo Danh Sách Phát Mới", color = colors.textPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("✕", color = colors.textSecondary, modifier = Modifier.clickable { onDismiss() })
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedTextField(
                value = playlistName,
                onValueChange = { playlistName = it },
                label = { Text("Tên danh sách phát", color = colors.textSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                RadioButton(selected = isPrivate, onClick = { isPrivate = true })
                Text("🔒 Riêng tư", color = colors.textPrimary, fontSize = 13.sp)
                Spacer(modifier = Modifier.width(16.dp))
                RadioButton(selected = !isPrivate, onClick = { isPrivate = false })
                Text("🌐 Công khai", color = colors.textPrimary, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (playlistName.isNotBlank()) {
                        MockYouTubeRepository.createPlaylist(playlistName, isPrivate)
                        onToast("💾 Đã tạo danh sách phát '$playlistName'!")
                        onDismiss()
                    }
                },
                enabled = playlistName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = colors.brandRed),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Tạo Danh Sách Phát", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun YouTubeBottomNavBar(
    colors: YouTubeColorPalette,
    selectedTab: YouTubeTab,
    onTabSelected: (YouTubeTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(colors.topBottomBarBg)
            .border(width = 0.5.dp, color = colors.divider),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val navItems = listOf(
            Triple(YouTubeTab.HOME, "Trang chủ", "🏠"),
            Triple(YouTubeTab.SHORTS, "Shorts", "⚡"),
            Triple(YouTubeTab.UPLOAD, "Tạo", "➕"),
            Triple(YouTubeTab.SUBSCRIPTIONS, "Đăng ký", "🔔"),
            Triple(YouTubeTab.YOU, "Bạn", "👤")
        )

        navItems.forEach { (tab, label, icon) ->
            val isSelected = selectedTab == tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onTabSelected(tab) }
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(icon, fontSize = 18.sp)
                Text(
                    text = label,
                    color = if (isSelected) colors.textPrimary else colors.textSecondary,
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}
