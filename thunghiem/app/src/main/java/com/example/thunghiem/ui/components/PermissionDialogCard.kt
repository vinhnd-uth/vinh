package com.example.thunghiem.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val PrimaryButtonOrange = Color(0xFFE85D36)
val PrimaryButtonOrangeGradientEnd = Color(0xFFEE5A24)
val SecondaryButtonBorderOrange = Color(0xFFDF6B44)
val TitleTextColor = Color(0xFF1E1E1E)
val SubtitleTextColor = Color(0xFF757575)

@Composable
fun PermissionDialogCard(
    title: String,
    description: String,
    primaryButtonText: String,
    iconContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    secondaryButtonText: String = "Skip for now",
    onPrimaryClick: () -> Unit = {},
    onSecondaryClick: () -> Unit = {},
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Illustration / Icon Header
            iconContent()

            Spacer(modifier = Modifier.height(20.dp))

            // Title
            Text(
                text = title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TitleTextColor,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description / Subtitle
            Text(
                text = description,
                fontSize = 14.sp,
                color = SubtitleTextColor,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Primary Action Button ("Allow" / "Turn on")
            Button(
                onClick = onPrimaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(PrimaryButtonOrange, PrimaryButtonOrangeGradientEnd)
                        ),
                        shape = RoundedCornerShape(50)
                    ),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = primaryButtonText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Secondary Action Button ("Skip for now")
            OutlinedButton(
                onClick = onSecondaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, SecondaryButtonBorderOrange),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = SecondaryButtonBorderOrange
                )
            ) {
                Text(
                    text = secondaryButtonText,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}
