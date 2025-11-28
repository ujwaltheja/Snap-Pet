package com.snappet.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var alpha: Float,
    var size: Float,
    val color: Color,
    var lifetime: Float = 1f
)

@Composable
fun FloatingParticles(
    modifier: Modifier = Modifier,
    particleColor: Color = Color.White.copy(alpha = 0.3f),
    particleCount: Int = 20,
    enabled: Boolean = true
) {
    var particles by remember { mutableStateOf(List(particleCount) { createParticle(particleColor) }) }
    var frame by remember { mutableStateOf(0L) }

    LaunchedEffect(enabled) {
        if (enabled) {
            while (true) {
                delay(16L) // ~60 FPS
                frame++
                particles = particles.map { updateParticle(it) }
            }
        }
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEach { particle ->
            drawCircle(
                color = particle.color.copy(alpha = particle.alpha),
                radius = particle.size,
                center = Offset(particle.x, particle.y)
            )
        }
    }
}

private fun createParticle(color: Color): Particle {
    val angle = Random.nextFloat() * 2 * Math.PI.toFloat()
    val speed = Random.nextFloat() * 0.5f + 0.2f

    return Particle(
        x = Random.nextFloat() * 1000,
        y = Random.nextFloat() * 2000,
        vx = cos(angle) * speed,
        vy = sin(angle) * speed,
        alpha = Random.nextFloat() * 0.3f + 0.1f,
        size = Random.nextFloat() * 8f + 4f,
        color = color,
        lifetime = Random.nextFloat()
    )
}

private fun updateParticle(particle: Particle): Particle {
    particle.x += particle.vx
    particle.y += particle.vy
    particle.lifetime -= 0.002f

    // Wrap around screen
    if (particle.x < -50) particle.x = 1100f
    if (particle.x > 1100) particle.x = -50f
    if (particle.y < -50) particle.y = 2100f
    if (particle.y > 2100) particle.y = -50f

    // Reset if lifetime expired
    if (particle.lifetime <= 0) {
        particle.lifetime = 1f
        particle.alpha = Random.nextFloat() * 0.3f + 0.1f
    }

    return particle
}

@Composable
fun ShimmerEffect(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color.White.copy(alpha = 0.0f),
        Color.White.copy(alpha = 0.5f),
        Color.White.copy(alpha = 0.0f)
    ),
    duration: Int = 2000
) {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -1000f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(duration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    Canvas(modifier = modifier) {
        drawRect(
            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                colors = colors,
                startX = offsetX,
                endX = offsetX + 400f
            )
        )
    }
}
