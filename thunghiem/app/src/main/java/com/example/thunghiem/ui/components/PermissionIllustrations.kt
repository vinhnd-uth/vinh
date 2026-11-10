package com.example.thunghiem.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Soft green organic blob background
val BlobColor = Color(0xFFE3F4EB)
val LocationPinColor = Color(0xFFE85D36)
val LocationShadowColor = Color(0xFF86C5A2)
val BellColor = Color(0xFFFFB300)
val BellClapperColor = Color(0xFFE85D36)
val CameraColor = Color(0xFFE85D36)
val CameraLensColor = Color(0xFFB93C19)

@Composable
fun BlobBackdrop(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    val blobShape = GenericShape { size, _ ->
        val w = size.width
        val h = size.height
        moveTo(w * 0.3f, h * 0.1f)
        cubicTo(w * 0.7f, h * 0.0f, w * 0.95f, h * 0.3f, w * 0.9f, h * 0.65f)
        cubicTo(w * 0.85f, h * 0.95f, w * 0.45f, h * 1.0f, w * 0.15f, h * 0.85f)
        cubicTo(w * -0.05f, h * 0.7f, w * -0.05f, h * 0.35f, w * 0.3f, h * 0.1f)
        close()
    }
    Box(
        modifier = modifier
            .size(size)
            .background(BlobColor, shape = blobShape)
    )
}

@Composable
fun LocationIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        BlobBackdrop(size = size)

        Canvas(modifier = Modifier.size(size * 0.75f)) {
            val w = this.size.width
            val h = this.size.height

            // Oval ground shadow plate
            drawOval(
                color = LocationShadowColor,
                topLeft = Offset(w * 0.22f, h * 0.75f),
                size = Size(w * 0.56f, h * 0.12f)
            )

            // Location Pin path
            val pinPath = Path().apply {
                val center = Offset(w * 0.5f, h * 0.4f)
                val radius = w * 0.22f
                // Top circle arc
                arcTo(
                    rect = Rect(
                        left = center.x - radius,
                        top = center.y - radius,
                        right = center.x + radius,
                        bottom = center.y + radius
                    ),
                    startAngleDegrees = 140f,
                    sweepAngleDegrees = 260f,
                    forceMoveTo = false
                )
                // Bottom point extending to ground shadow
                lineTo(w * 0.5f, h * 0.76f)
                close()
            }

            drawPath(
                path = pinPath,
                color = LocationPinColor
            )

            // Inner white circle hole inside pin
            drawCircle(
                color = Color.White,
                radius = w * 0.08f,
                center = Offset(w * 0.5f, h * 0.4f)
            )
        }
    }
}

@Composable
fun NotificationIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        BlobBackdrop(size = size)

        Canvas(modifier = Modifier.size(size * 0.75f)) {
            val w = this.size.width
            val h = this.size.height

            // Bell top handle loop
            drawCircle(
                color = BellColor,
                radius = w * 0.05f,
                center = Offset(w * 0.5f, h * 0.25f),
                style = Stroke(width = w * 0.04f)
            )

            // Bell main body path
            val bellPath = Path().apply {
                moveTo(w * 0.32f, h * 0.6f)
                cubicTo(w * 0.32f, h * 0.4f, w * 0.38f, h * 0.28f, w * 0.5f, h * 0.28f)
                cubicTo(w * 0.62f, h * 0.28f, w * 0.68f, h * 0.4f, w * 0.68f, h * 0.6f)
                lineTo(w * 0.74f, h * 0.66f)
                quadraticTo(w * 0.76f, h * 0.69f, w * 0.72f, h * 0.7f)
                lineTo(w * 0.28f, h * 0.7f)
                quadraticTo(w * 0.24f, h * 0.69f, w * 0.26f, h * 0.66f)
                close()
            }

            drawPath(
                path = bellPath,
                color = BellColor
            )

            // Bell clapper at bottom
            drawCircle(
                color = BellClapperColor,
                radius = w * 0.06f,
                center = Offset(w * 0.5f, h * 0.75f)
            )

            // Sound waves on left & right
            val strokeWidth = w * 0.05f
            // Left inner wave
            drawArc(
                color = BellColor,
                startAngle = 135f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.12f, h * 0.36f),
                size = Size(w * 0.35f, h * 0.35f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
            // Right inner wave
            drawArc(
                color = BellColor,
                startAngle = -45f,
                sweepAngle = 90f,
                useCenter = false,
                topLeft = Offset(w * 0.53f, h * 0.36f),
                size = Size(w * 0.35f, h * 0.35f),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }
    }
}

@Composable
fun CameraIllustration(
    modifier: Modifier = Modifier,
    size: Dp = 140.dp
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        BlobBackdrop(size = size)

        Canvas(modifier = Modifier.size(size * 0.75f)) {
            val w = this.size.width
            val h = this.size.height

            // Camera top flash bump
            drawRoundRect(
                color = CameraLensColor,
                topLeft = Offset(w * 0.4f, h * 0.26f),
                size = Size(w * 0.2f, h * 0.1f),
                cornerRadius = CornerRadius(w * 0.03f, w * 0.03f)
            )

            // Camera main body (rounded rectangle)
            drawRoundRect(
                color = CameraColor,
                topLeft = Offset(w * 0.22f, h * 0.32f),
                size = Size(w * 0.56f, h * 0.42f),
                cornerRadius = CornerRadius(w * 0.1f, w * 0.1f)
            )

            // Outer lens ring
            drawCircle(
                color = Color.White.copy(alpha = 0.3f),
                radius = w * 0.14f,
                center = Offset(w * 0.5f, h * 0.53f)
            )

            // Main lens circle
            drawCircle(
                color = CameraLensColor,
                radius = w * 0.11f,
                center = Offset(w * 0.5f, h * 0.53f)
            )

            // Lens reflection dot
            drawCircle(
                color = Color.White,
                radius = w * 0.035f,
                center = Offset(w * 0.54f, h * 0.49f)
            )
        }
    }
}
