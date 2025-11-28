package com.snappet.ui.graphics

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

enum class PetEmotion {
    HAPPY, NEUTRAL, SAD, SLEEPY, EATING, SHOCKED
}

@Composable
fun PetView(
    modifier: Modifier = Modifier,
    primaryColor: Color = Color(0xFFFF9F1C), // Orange Cat default
    secondaryColor: Color = Color(0xFFFFF3E0),
    emotion: PetEmotion = PetEmotion.NEUTRAL,
    breathingScale: Float = 1f
) {
    // Eye Blink Animation
    val infiniteTransition = rememberInfiniteTransition(label = "blink")
    val eyeScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                1f at 0
                1f at 2800
                0.1f at 2900
                1f at 3000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "eyeBlink"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2

        // Apply breathing scale
        val scaledW = w * breathingScale
        val scaledH = h * breathingScale
        val offsetX = (w - scaledW) / 2
        val offsetY = (h - scaledH) / 2

        // Shadow
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent),
                center = Offset(cx, cy + scaledH * 0.45f),
                radius = scaledW * 0.4f
            ),
            topLeft = Offset(cx - scaledW * 0.4f, cy + scaledH * 0.35f),
            size = Size(scaledW * 0.8f, scaledH * 0.2f)
        )

        // Body Gradient (3D effect)
        val bodyBrush = Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = 1f), // Highlight
                primaryColor,
                primaryColor.copy(red = primaryColor.red * 0.8f, green = primaryColor.green * 0.8f, blue = primaryColor.blue * 0.8f) // Shadow
            ),
            center = Offset(cx - scaledW * 0.1f, cy - scaledH * 0.1f),
            radius = scaledW * 0.6f
        )

        // Draw Body (Rounded Blob)
        drawRoundRect(
            brush = bodyBrush,
            topLeft = Offset(offsetX + w * 0.15f, offsetY + h * 0.2f),
            size = Size(scaledW * 0.7f, scaledH * 0.7f),
            cornerRadius = CornerRadius(w * 0.35f, w * 0.35f)
        )

        // Draw Ears (Behind head slightly?) - No, on top for cartoon look, but let's add depth
        val earPath = Path().apply {
            // Left Ear
            moveTo(cx - w * 0.25f, h * 0.25f)
            quadraticBezierTo(cx - w * 0.35f, h * 0.05f, cx - w * 0.4f, h * 0.1f) // Curved tip
            lineTo(cx - w * 0.15f, h * 0.25f)
            close()
            // Right Ear
            moveTo(cx + w * 0.25f, h * 0.25f)
            quadraticBezierTo(cx + w * 0.35f, h * 0.05f, cx + w * 0.4f, h * 0.1f)
            lineTo(cx + w * 0.15f, h * 0.25f)
            close()
        }
        drawPath(earPath, primaryColor) // Simple color for ears for now, maybe gradient later if needed
        
        // Inner Ears
        val innerEarPath = Path().apply {
             // Left
            moveTo(cx - w * 0.25f, h * 0.22f)
            lineTo(cx - w * 0.32f, h * 0.08f)
            lineTo(cx - w * 0.18f, h * 0.18f)
            // Right
            moveTo(cx + w * 0.25f, h * 0.22f)
            lineTo(cx + w * 0.32f, h * 0.08f)
            lineTo(cx + w * 0.18f, h * 0.18f)
        }
        drawPath(innerEarPath, secondaryColor)

        // Draw Belly (Gradient)
        val bellyBrush = Brush.verticalGradient(
            colors = listOf(secondaryColor, secondaryColor.copy(alpha = 0.8f)),
            startY = cy,
            endY = cy + h * 0.3f
        )
        drawOval(
            brush = bellyBrush,
            topLeft = Offset(cx - w * 0.2f, cy + h * 0.1f),
            size = Size(w * 0.4f, h * 0.35f)
        )

        // Draw Face
        val eyeY = cy - h * 0.05f
        val eyeSize = w * 0.09f
        val eyeSeparation = w * 0.16f

        if (emotion == PetEmotion.SLEEPY) {
            // Closed Eyes (Curved Lines)
            val leftEyePath = Path().apply {
                moveTo(cx - eyeSeparation - eyeSize, eyeY)
                quadraticBezierTo(cx - eyeSeparation, eyeY + eyeSize * 0.5f, cx - eyeSeparation + eyeSize, eyeY)
            }
            drawPath(leftEyePath, Color.Black, style = Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round))
            
            val rightEyePath = Path().apply {
                moveTo(cx + eyeSeparation - eyeSize, eyeY)
                quadraticBezierTo(cx + eyeSeparation, eyeY + eyeSize * 0.5f, cx + eyeSeparation + eyeSize, eyeY)
            }
            drawPath(rightEyePath, Color.Black, style = Stroke(width = 8f, cap = androidx.compose.ui.graphics.StrokeCap.Round))

        } else {
            // Open Eyes with details
            val currentEyeScale = if (emotion == PetEmotion.SHOCKED) 1.2f else eyeScale
            
            // Left Eye Sclera
            drawOval(
                color = Color.White,
                topLeft = Offset(cx - eyeSeparation - eyeSize, eyeY - eyeSize/2),
                size = Size(eyeSize * 2, eyeSize * 2 * currentEyeScale)
            )
            // Left Pupil
            drawOval(
                color = Color.Black,
                topLeft = Offset(cx - eyeSeparation - eyeSize * 0.6f, eyeY - eyeSize * 0.1f),
                size = Size(eyeSize * 1.2f, eyeSize * 1.2f * currentEyeScale)
            )
            // Left Reflection
            drawCircle(
                color = Color.White,
                radius = eyeSize * 0.25f * currentEyeScale,
                center = Offset(cx - eyeSeparation - eyeSize * 0.3f, eyeY + eyeSize * 0.2f)
            )

            // Right Eye Sclera
            drawOval(
                color = Color.White,
                topLeft = Offset(cx + eyeSeparation - eyeSize, eyeY - eyeSize/2),
                size = Size(eyeSize * 2, eyeSize * 2 * currentEyeScale)
            )
            // Right Pupil
            drawOval(
                color = Color.Black,
                topLeft = Offset(cx + eyeSeparation - eyeSize * 0.6f, eyeY - eyeSize * 0.1f),
                size = Size(eyeSize * 1.2f, eyeSize * 1.2f * currentEyeScale)
            )
            // Right Reflection
            drawCircle(
                color = Color.White,
                radius = eyeSize * 0.25f * currentEyeScale,
                center = Offset(cx + eyeSeparation - eyeSize * 0.3f, eyeY + eyeSize * 0.2f)
            )
        }

        // Nose (Cute rounded triangle)
        val nosePath = Path().apply {
            moveTo(cx - w * 0.04f, cy + h * 0.06f)
            quadraticBezierTo(cx, cy + h * 0.05f, cx + w * 0.04f, cy + h * 0.06f)
            quadraticBezierTo(cx, cy + h * 0.09f, cx - w * 0.04f, cy + h * 0.06f)
        }
        drawPath(nosePath, Color(0xFFFF8A80))

        // Mouth
        val mouthPath = Path()
        when (emotion) {
            PetEmotion.HAPPY, PetEmotion.NEUTRAL -> {
                mouthPath.moveTo(cx, cy + h * 0.09f)
                mouthPath.quadraticBezierTo(cx - w * 0.05f, cy + h * 0.13f, cx - w * 0.1f, cy + h * 0.11f)
                mouthPath.moveTo(cx, cy + h * 0.09f)
                mouthPath.quadraticBezierTo(cx + w * 0.05f, cy + h * 0.13f, cx + w * 0.1f, cy + h * 0.11f)
            }
            PetEmotion.SAD -> {
                mouthPath.moveTo(cx - w * 0.08f, cy + h * 0.14f)
                mouthPath.quadraticBezierTo(cx, cy + h * 0.1f, cx + w * 0.08f, cy + h * 0.14f)
            }
            PetEmotion.EATING -> {
                drawOval(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(cx - w * 0.06f, cy + h * 0.1f),
                    size = Size(w * 0.12f, w * 0.12f)
                )
            }
            PetEmotion.SHOCKED -> {
                 drawOval(
                    color = Color.Black,
                    topLeft = Offset(cx - w * 0.04f, cy + h * 0.12f),
                    size = Size(w * 0.08f, w * 0.12f),
                    style = Stroke(width = 5f)
                )
            }
            else -> {}
        }
        if (emotion != PetEmotion.EATING && emotion != PetEmotion.SHOCKED) {
            drawPath(
                path = mouthPath,
                color = Color.Black,
                style = Stroke(width = 6f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
        
        // Cheeks (Blush)
        if (emotion == PetEmotion.HAPPY) {
            drawOval(
                brush = Brush.radialGradient(listOf(Color(0xFFFFCDD2), Color.Transparent)),
                topLeft = Offset(cx - w * 0.25f, cy + h * 0.08f),
                size = Size(w * 0.15f, w * 0.1f)
            )
            drawOval(
                brush = Brush.radialGradient(listOf(Color(0xFFFFCDD2), Color.Transparent)),
                topLeft = Offset(cx + w * 0.1f, cy + h * 0.08f),
                size = Size(w * 0.15f, w * 0.1f)
            )
        }
    }
}
