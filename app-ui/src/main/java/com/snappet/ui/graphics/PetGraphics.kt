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

        // Draw Body (Rounded Blob)
        drawRoundRect(
            color = primaryColor,
            topLeft = Offset(offsetX + w * 0.15f, offsetY + h * 0.2f),
            size = Size(scaledW * 0.7f, scaledH * 0.7f),
            cornerRadius = CornerRadius(w * 0.3f, w * 0.3f)
        )

        // Draw Ears
        val earPath = Path().apply {
            // Left Ear
            moveTo(cx - w * 0.25f, h * 0.25f)
            lineTo(cx - w * 0.35f, h * 0.05f)
            lineTo(cx - w * 0.15f, h * 0.2f)
            // Right Ear
            moveTo(cx + w * 0.25f, h * 0.25f)
            lineTo(cx + w * 0.35f, h * 0.05f)
            lineTo(cx + w * 0.15f, h * 0.2f)
        }
        drawPath(earPath, primaryColor)
        
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

        // Draw Belly
        drawOval(
            color = secondaryColor,
            topLeft = Offset(cx - w * 0.2f, cy + h * 0.1f),
            size = Size(w * 0.4f, h * 0.3f)
        )

        // Draw Face
        val eyeY = cy - h * 0.05f
        val eyeSize = w * 0.08f
        val eyeSeparation = w * 0.15f

        if (emotion == PetEmotion.SLEEPY) {
            // Closed Eyes (Lines)
            drawLine(
                color = Color.Black,
                start = Offset(cx - eyeSeparation - eyeSize, eyeY),
                end = Offset(cx - eyeSeparation + eyeSize, eyeY),
                strokeWidth = 8f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            drawLine(
                color = Color.Black,
                start = Offset(cx + eyeSeparation - eyeSize, eyeY),
                end = Offset(cx + eyeSeparation + eyeSize, eyeY),
                strokeWidth = 8f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        } else {
            // Open Eyes
            val currentEyeScale = if (emotion == PetEmotion.SHOCKED) 1.2f else eyeScale
            
            // Left Eye
            drawOval(
                color = Color.White,
                topLeft = Offset(cx - eyeSeparation - eyeSize, eyeY - eyeSize/2),
                size = Size(eyeSize * 2, eyeSize * 2 * currentEyeScale)
            )
            drawOval(
                color = Color.Black,
                topLeft = Offset(cx - eyeSeparation - eyeSize/2, eyeY),
                size = Size(eyeSize, eyeSize * currentEyeScale)
            )

            // Right Eye
            drawOval(
                color = Color.White,
                topLeft = Offset(cx + eyeSeparation - eyeSize, eyeY - eyeSize/2),
                size = Size(eyeSize * 2, eyeSize * 2 * currentEyeScale)
            )
            drawOval(
                color = Color.Black,
                topLeft = Offset(cx + eyeSeparation - eyeSize/2, eyeY),
                size = Size(eyeSize, eyeSize * currentEyeScale)
            )
        }

        // Nose
        drawOval(
            color = Color(0xFFFF8A80), // Pink nose
            topLeft = Offset(cx - w * 0.03f, cy + h * 0.05f),
            size = Size(w * 0.06f, w * 0.04f)
        )

        // Mouth
        val mouthPath = Path()
        when (emotion) {
            PetEmotion.HAPPY, PetEmotion.NEUTRAL -> {
                mouthPath.moveTo(cx, cy + h * 0.08f)
                mouthPath.quadraticBezierTo(cx - w * 0.05f, cy + h * 0.12f, cx - w * 0.1f, cy + h * 0.1f)
                mouthPath.moveTo(cx, cy + h * 0.08f)
                mouthPath.quadraticBezierTo(cx + w * 0.05f, cy + h * 0.12f, cx + w * 0.1f, cy + h * 0.1f)
            }
            PetEmotion.SAD -> {
                mouthPath.moveTo(cx - w * 0.08f, cy + h * 0.12f)
                mouthPath.quadraticBezierTo(cx, cy + h * 0.08f, cx + w * 0.08f, cy + h * 0.12f)
            }
            PetEmotion.EATING -> {
                drawOval(
                    color = Color(0xFF5D4037),
                    topLeft = Offset(cx - w * 0.05f, cy + h * 0.08f),
                    size = Size(w * 0.1f, w * 0.1f)
                )
            }
            PetEmotion.SHOCKED -> {
                 drawOval(
                    color = Color.Black,
                    topLeft = Offset(cx - w * 0.03f, cy + h * 0.1f),
                    size = Size(w * 0.06f, w * 0.08f),
                    style = Stroke(width = 5f)
                )
            }
            else -> {}
        }
        if (emotion != PetEmotion.EATING && emotion != PetEmotion.SHOCKED) {
            drawPath(
                path = mouthPath,
                color = Color.Black,
                style = Stroke(width = 5f, cap = androidx.compose.ui.graphics.StrokeCap.Round)
            )
        }
    }
}
