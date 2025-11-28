package com.snappet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snappet.ui.theme.SnapPetTheme

data class PetAction(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val emoji: String = "",
    val onClick: () -> Unit
)

@Composable
fun TalkingTomPanel(
    actions: List<PetAction>,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier,
        backgroundColor = Color.White.copy(alpha = 0.25f),
        borderColor = Color.White.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp, horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            actions.forEach { action ->
                TalkingTomActionButton(
                    icon = action.icon,
                    label = action.label,
                    color = action.color,
                    emoji = action.emoji,
                    onClick = action.onClick
                )
            }
        }
    }
}

@Composable
fun TalkingTomActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    emoji: String = "",
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Juicy Button for Action
        JuicyButton(
            onClick = onClick,
            icon = if (emoji.isEmpty()) icon else null,
            text = if (emoji.isNotEmpty()) emoji else null,
            color = color,
            size = 72.dp
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Label
        Surface(
            color = Color.White.copy(alpha = 0.9f),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White)
        ) {
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun PetFeedbackBubble(
    message: String,
    isVisible: Boolean,
    modifier: Modifier = Modifier
) {
    if (isVisible) {
        val scale by animateFloatAsState(
            targetValue = if (isVisible) 1f else 0f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "bubbleScale"
        )

        Box(
            modifier = modifier.scale(scale),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 12.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Animated sparkle
                    val infiniteTransition = rememberInfiniteTransition(label = "sparkle")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(2000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )

                    Text(
                        text = "✨",
                        fontSize = 24.sp,
                        modifier = Modifier.rotate(rotation)
                    )

                    Text(
                        text = message,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = SnapPetTheme.colors.primary
                    )

                    Text(
                        text = "✨",
                        fontSize = 24.sp,
                        modifier = Modifier.rotate(-rotation)
                    )
                }
            }
        }
    }
}

@Composable
fun EnergyBar(
    value: Float,
    maxValue: Float = 100f,
    label: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    val progress = (value / maxValue).coerceIn(0f, 1f)

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
        label = "progress"
    )

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.9f)
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .background(
                    Color.Black.copy(alpha = 0.3f),
                    RoundedCornerShape(6.dp)
                )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedProgress)
                    .fillMaxHeight()
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color,
                                color.copy(
                                    red = (color.red * 1.2f).coerceAtMost(1.0f),
                                    green = (color.green * 1.2f).coerceAtMost(1.0f),
                                    blue = (color.blue * 1.2f).coerceAtMost(1.0f)
                                )
                            )
                        ),
                        shape = RoundedCornerShape(6.dp)
                    )
            )
        }

        Text(
            text = "${value.toInt()}/${maxValue.toInt()}",
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White.copy(alpha = 0.7f)
        )
    }
}
