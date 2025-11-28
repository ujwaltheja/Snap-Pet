package com.snappet.ui.graphics

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke

@Composable
fun LivingRoomBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Wall (Gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE0F7FA), Color(0xFF80DEEA)),
                startY = 0f,
                endY = h * 0.7f
            ),
            size = Size(w, h * 0.7f)
        )

        // Floor (Wood Gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF8D6E63), Color(0xFF6D4C41)),
                startY = h * 0.7f,
                endY = h
            ),
            topLeft = Offset(0f, h * 0.7f),
            size = Size(w, h * 0.3f)
        )
        
        // Floor Planks
        for (i in 0 until 10) {
            drawLine(
                color = Color(0xFF5D4037).copy(alpha = 0.5f),
                start = Offset(0f, h * 0.7f + (h * 0.3f / 10) * i),
                end = Offset(w, h * 0.7f + (h * 0.3f / 10) * i),
                strokeWidth = 2f
            )
        }

        // Window
        val windowW = w * 0.4f
        val windowH = h * 0.3f
        val windowX = w * 0.1f
        val windowY = h * 0.15f

        // Window View (Sky Gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF29B6F6), Color(0xFF81D4FA))
            ),
            topLeft = Offset(windowX, windowY),
            size = Size(windowW, windowH)
        )
        // Cloud
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = windowW * 0.15f,
            center = Offset(windowX + windowW * 0.3f, windowY + windowH * 0.3f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.8f),
            radius = windowW * 0.2f,
            center = Offset(windowX + windowW * 0.5f, windowY + windowH * 0.4f)
        )

        // Window Frame
        drawRect(
            color = Color.White,
            topLeft = Offset(windowX - 10f, windowY - 10f),
            size = Size(windowW + 20f, windowH + 20f),
            style = Stroke(width = 20f)
        )
        // Window Cross
        drawLine(
            color = Color.White,
            start = Offset(windowX + windowW/2, windowY),
            end = Offset(windowX + windowW/2, windowY + windowH),
            strokeWidth = 10f
        )
        drawLine(
            color = Color.White,
            start = Offset(windowX, windowY + windowH/2),
            end = Offset(windowX + windowW, windowY + windowH/2),
            strokeWidth = 10f
        )

        // Rug (Gradient)
        drawOval(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFEF9A9A), Color(0xFFE57373)),
                center = Offset(w * 0.5f, h * 0.82f),
                radius = w * 0.3f
            ),
            topLeft = Offset(w * 0.2f, h * 0.75f),
            size = Size(w * 0.6f, h * 0.15f)
        )
    }
}

@Composable
fun KitchenBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Wall (Yellowish Gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF9C4), Color(0xFFFFF59D))
            ),
            size = Size(w, h * 0.65f)
        )

        // Floor (Checkered Tiles)
        val floorY = h * 0.65f
        val tileSize = w / 8
        drawRect(
            color = Color.White,
            topLeft = Offset(0f, floorY),
            size = Size(w, h - floorY)
        )
        
        var isBlack = false
        for (x in 0 until 8) {
            for (y in 0 until ((h - floorY) / tileSize).toInt() + 1) {
                if (isBlack) {
                    drawRect(
                        color = Color(0xFFE0E0E0),
                        topLeft = Offset(x * tileSize, floorY + y * tileSize),
                        size = Size(tileSize, tileSize)
                    )
                }
                isBlack = !isBlack
            }
            isBlack = !isBlack
        }

        // Fridge Outline (Right side)
        drawRect(
            brush = Brush.horizontalGradient(
                colors = listOf(Color(0xFFF5F5F5), Color(0xFFE0E0E0))
            ),
            topLeft = Offset(w * 0.75f, h * 0.2f),
            size = Size(w * 0.25f, h * 0.5f)
        )
        // Fridge Handle
        drawLine(
            color = Color.Gray,
            start = Offset(w * 0.78f, h * 0.3f),
            end = Offset(w * 0.78f, h * 0.4f),
            strokeWidth = 8f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

@Composable
fun BedroomBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Wall (Dark Blue Gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF3949AB), Color(0xFF283593))
            ),
            size = Size(w, h * 0.7f)
        )

        // Floor (Carpet Gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF5C6BC0), Color(0xFF3F51B5)),
                startY = h * 0.7f,
                endY = h
            ),
            topLeft = Offset(0f, h * 0.7f),
            size = Size(w, h * 0.3f)
        )

        // Window with Moon
        val windowW = w * 0.3f
        val windowH = h * 0.3f
        val windowX = w * 0.6f
        val windowY = h * 0.1f

        // Night Sky Gradient
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF1A237E), Color(0xFF000051))
            ),
            topLeft = Offset(windowX, windowY),
            size = Size(windowW, windowH)
        )
        
        // Stars
        drawCircle(Color.White, 2f, Offset(windowX + windowW * 0.2f, windowY + windowH * 0.2f))
        drawCircle(Color.White, 3f, Offset(windowX + windowW * 0.5f, windowY + windowH * 0.1f))
        drawCircle(Color.White, 2f, Offset(windowX + windowW * 0.8f, windowY + windowH * 0.4f))

        // Moon (Glow)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color(0xFFFFF176), Color.Transparent),
                center = Offset(windowX + windowW * 0.7f, windowY + windowH * 0.3f),
                radius = windowW * 0.25f
            ),
            radius = windowW * 0.25f,
            center = Offset(windowX + windowW * 0.7f, windowY + windowH * 0.3f)
        )
        drawCircle(
            color = Color(0xFFFFF59D),
            radius = windowW * 0.15f,
            center = Offset(windowX + windowW * 0.7f, windowY + windowH * 0.3f)
        )

        // Window Frame
        drawRect(
            color = Color(0xFF9FA8DA),
            topLeft = Offset(windowX - 5f, windowY - 5f),
            size = Size(windowW + 10f, windowH + 10f),
            style = Stroke(width = 10f)
        )
    }
}

@Composable
fun BathroomBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Wall (Tiles Gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE0F7FA), Color(0xFFB2EBF2))
            ),
            size = Size(w, h * 0.7f)
        )
        // Tile Lines
        val tileSize = 60f
        for (y in 0 until (h * 0.7f / tileSize).toInt()) {
            drawLine(
                color = Color(0xFF80DEEA),
                start = Offset(0f, y * tileSize),
                end = Offset(w, y * tileSize),
                strokeWidth = 2f
            )
        }
        for (x in 0 until (w / tileSize).toInt()) {
            drawLine(
                color = Color(0xFF80DEEA),
                start = Offset(x * tileSize, 0f),
                end = Offset(x * tileSize, h * 0.7f),
                strokeWidth = 2f
            )
        }

        // Floor (Blue Tiles Gradient)
        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF4DD0E1), Color(0xFF00ACC1)),
                startY = h * 0.7f,
                endY = h
            ),
            topLeft = Offset(0f, h * 0.7f),
            size = Size(w, h * 0.3f)
        )

        // Bubbles (Randomly placed for effect)
        val bubbleColor = Color.White.copy(alpha = 0.6f)
        drawCircle(bubbleColor, 20f, Offset(w * 0.2f, h * 0.6f))
        drawCircle(bubbleColor, 15f, Offset(w * 0.25f, h * 0.55f))
        drawCircle(bubbleColor, 25f, Offset(w * 0.8f, h * 0.65f))
        drawCircle(bubbleColor, 10f, Offset(w * 0.85f, h * 0.58f))
    }
}
