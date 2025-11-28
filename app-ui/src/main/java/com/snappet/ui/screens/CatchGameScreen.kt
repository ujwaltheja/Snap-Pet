package com.snappet.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.snappet.core.PetController
import com.snappet.core.models.Interaction
import com.snappet.ui.components.GlassmorphicCard
import com.snappet.ui.theme.SnapPetTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.random.Random

@Composable
fun CatchGameScreen(
    navController: NavController,
    petController: PetController
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val density = LocalDensity.current
    
    // Game State
    var score by remember { mutableStateOf(0) }
    var lives by remember { mutableStateOf(3) }
    var isGameOver by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(false) }
    
    // Pet Position (X axis, 0 to 1)
    var petX by remember { mutableStateOf(0.5f) }
    
    // Falling Items
    data class GameItem(
        val id: Long,
        val x: Float, // 0 to 1
        val y: Float, // 0 to 1
        val type: ItemType,
        val speed: Float
    )
    
    val items = remember { mutableStateListOf<GameItem>() }
    
    // Game Loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            var lastSpawnTime = 0L
            val startTime = System.currentTimeMillis()
            
            while (isPlaying && !isGameOver) {
                val currentTime = System.currentTimeMillis()
                val deltaTime = 16L // ~60 FPS
                
                // Spawn items
                if (currentTime - lastSpawnTime > 1000 - (score * 10).coerceAtMost(800)) {
                    items.add(
                        GameItem(
                            id = System.nanoTime(),
                            x = Random.nextFloat().coerceIn(0.1f, 0.9f),
                            y = -0.1f,
                            type = ItemType.values().random(),
                            speed = 0.005f + (score * 0.0001f)
                        )
                    )
                    lastSpawnTime = currentTime
                }
                
                // Update items
                val iterator = items.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next()
                    val newY = item.y + item.speed
                    
                    // Collision Detection
                    if (newY >= 0.85f && newY <= 0.95f && abs(item.x - petX) < 0.15f) {
                        // Caught!
                        score += item.type.points
                        iterator.remove()
                    } else if (newY > 1.0f) {
                        // Missed
                        if (item.type.isGood) {
                            lives--
                            if (lives <= 0) {
                                isGameOver = true
                                isPlaying = false
                            }
                        }
                        iterator.remove()
                    } else {
                        // Update position
                        val index = items.indexOf(item)
                        if (index != -1) {
                            items[index] = item.copy(y = newY)
                        }
                    }
                }
                
                delay(deltaTime)
            }
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SnapPetTheme.colors.background.first())
    ) {
        // Game Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val deltaX = dragAmount.x / (screenWidth.toPx())
                        petX = (petX + deltaX).coerceIn(0.1f, 0.9f)
                    }
                }
        ) {
            // Falling Items
            items.forEach { item ->
                Text(
                    text = item.type.emoji,
                    fontSize = 32.sp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset(
                            x = (screenWidth * item.x) - 20.dp,
                            y = (screenHeight * item.y)
                        )
                )
            }
            
            // Player (Pet)
            Text(
                text = "🐱", // Should use current pet emoji
                fontSize = 64.sp,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(
                        x = (screenWidth * petX) - 32.dp,
                        y = screenHeight * 0.85f
                    )
            )
        }
        
        // HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            GlassmorphicCard(backgroundColor = Color.Black.copy(alpha = 0.3f)) {
                Text(
                    "Score: $score",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            GlassmorphicCard(backgroundColor = Color.Black.copy(alpha = 0.3f)) {
                Text(
                    "Lives: " + "❤️".repeat(lives),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(12.dp)
                )
            }
            
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.Close, "Exit", tint = Color.White)
            }
        }
        
        // Start/Game Over Overlay
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                GlassmorphicCard(
                    modifier = Modifier.padding(32.dp),
                    backgroundColor = SnapPetTheme.colors.surface
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = if (isGameOver) "Game Over!" else "Catch the Food!",
                            style = MaterialTheme.typography.headlineMedium,
                            color = SnapPetTheme.colors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        
                        if (isGameOver) {
                            Text(
                                "Final Score: $score",
                                style = MaterialTheme.typography.titleLarge,
                                color = SnapPetTheme.colors.textSecondary
                            )
                            
                            val coinsEarned = score / 10
                            Text(
                                "+$coinsEarned Coins",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color(0xFFFFD700)
                            )
                            
                            LaunchedEffect(Unit) {
                                petController.handleInteraction(Interaction.PLAY, coinsEarned)
                            }
                        }
                        
                        Button(
                            onClick = {
                                score = 0
                                lives = 3
                                items.clear()
                                isGameOver = false
                                isPlaying = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SnapPetTheme.colors.primary
                            )
                        ) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text(if (isGameOver) "Play Again" else "Start Game")
                        }
                        
                        if (isGameOver) {
                            OutlinedButton(
                                onClick = { navController.popBackStack() }
                            ) {
                                Text("Exit")
                            }
                        }
                    }
                }
            }
        }
    }
}

enum class ItemType(val emoji: String, val points: Int, val isGood: Boolean) {
    APPLE("🍎", 10, true),
    BURGER("🍔", 20, true),
    COOKIE("🍪", 15, true),
    BOMB("💣", -50, false),
    DIAMOND("💎", 50, true)
}
