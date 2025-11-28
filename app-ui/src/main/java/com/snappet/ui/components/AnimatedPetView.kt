package com.snappet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.snappet.ui.graphics.PetEmotion

@Composable
fun AnimatedPetView(
    petId: String,
    emotion: PetEmotion,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    var isTapped by remember { mutableStateOf(false) }
    var isJumping by remember { mutableStateOf(false) }

    // Breathing animation
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Bobbing animation
    val bobOffset by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob"
    )

    // Tap scale animation
    val tapScale by animateFloatAsState(
        targetValue = if (isTapped) 0.85f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "tapScale"
    )

    // Jump animation
    val jumpOffset by animateFloatAsState(
        targetValue = if (isJumping) -40f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        finishedListener = {
            if (isJumping) isJumping = false
        },
        label = "jump"
    )

    // Eye blink animation
    var isBlinking by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay((3000..5000).random().toLong())
            isBlinking = true
            kotlinx.coroutines.delay(150)
            isBlinking = false
        }
    }

    // Rotation for sleepy emotion
    val rotation by animateFloatAsState(
        targetValue = if (emotion == PetEmotion.SLEEPY) 15f else 0f,
        animationSpec = tween(500),
        label = "rotation"
    )

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isTapped = true
                        tryAwaitRelease()
                        isTapped = false
                    },
                    onTap = {
                        isJumping = true
                        onTap()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Glow effect
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        getPetColor(petId).copy(alpha = 0.3f),
                        Color.Transparent
                    ),
                    radius = size.width * 0.5f
                )
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset(y = (bobOffset + jumpOffset).dp)
                .scale(breathScale * tapScale)
                .rotate(rotation)
        ) {
            // Pet Body
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                getPetColor(petId),
                                getPetColor(petId).copy(
                                    red = getPetColor(petId).red * 0.7f,
                                    green = getPetColor(petId).green * 0.7f,
                                    blue = getPetColor(petId).blue * 0.7f
                                )
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Face
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(30.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        // Eyes
                        Text(
                            text = if (isBlinking) "—" else getEyeEmoji(emotion),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBlinking) "—" else getEyeEmoji(emotion),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Mouth
                    Text(
                        text = getMouthEmoji(emotion),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold
                    )

                    // Sleep indicator
                    if (emotion == PetEmotion.SLEEPY) {
                        val zzScale by infiniteTransition.animateFloat(
                            initialValue = 0.8f,
                            targetValue = 1.2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "zzz"
                        )

                        Text(
                            text = "Zzz",
                            fontSize = 24.sp,
                            modifier = Modifier
                                .offset(x = 60.dp, y = (-40).dp)
                                .scale(zzScale),
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            // Pet Name Badge
            Spacer(modifier = Modifier.height(16.dp))

            Card3D(
                modifier = Modifier,
                backgroundColor = Color.White.copy(alpha = 0.95f),
                elevation = 8.dp,
                glowColor = getPetColor(petId)
            ) {
                Text(
                    text = getPetName(petId),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = getPetColor(petId),
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }
        }
    }
}

private fun getPetColor(petId: String): Color {
    return when (petId) {
        "cat" -> Color(0xFFFF6B6B)
        "dog" -> Color(0xFF4FC3F7)
        "bunny" -> Color(0xFFF06292)
        else -> Color(0xFFFF6B6B)
    }
}

private fun getPetName(petId: String): String {
    return when (petId) {
        "cat" -> "😺 Whiskers"
        "dog" -> "🐶 Buddy"
        "bunny" -> "🐰 Fluffy"
        else -> "🐾 Pet"
    }
}

private fun getEyeEmoji(emotion: PetEmotion): String {
    return when (emotion) {
        PetEmotion.HAPPY -> "●"
        PetEmotion.SAD -> "◕"
        PetEmotion.SLEEPY -> "~"
        PetEmotion.EATING -> "◉"
        PetEmotion.SHOCKED -> "◎"
        PetEmotion.NEUTRAL -> "●"
    }
}

private fun getMouthEmoji(emotion: PetEmotion): String {
    return when (emotion) {
        PetEmotion.HAPPY -> "◡"
        PetEmotion.SAD -> "︵"
        PetEmotion.SLEEPY -> "⌒"
        PetEmotion.EATING -> "○"
        PetEmotion.SHOCKED -> "O"
        PetEmotion.NEUTRAL -> "–"
    }
}
