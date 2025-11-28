package com.snappet.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.snappet.R
import com.snappet.core.PetController
import com.snappet.core.models.Interaction
import com.snappet.core.models.Pet
import com.snappet.core.models.PetState
import com.snappet.media.MediaController
import com.snappet.persistence.PersistenceRepository
import com.snappet.ui.navigation.Screen
import com.snappet.ui.theme.*
import com.snappet.ui.graphics.*
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    petController: PetController,
    mediaController: MediaController,
    persistenceRepository: PersistenceRepository
) {
    val petState by petController.currentPetState.collectAsState()
    val currentPet by petController.currentPet.collectAsState()
    val user by persistenceRepository.observeUser().collectAsState(initial = null)

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator

    var isRecording by remember { mutableStateOf(false) }
    var recordingFile by remember { mutableStateOf<File?>(null) }
    var showMessage by remember { mutableStateOf<String?>(null) }
    
    // Room State
    var currentRoom by remember { mutableStateOf(Room.LIVING_ROOM) }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    val breathScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Background Colors for Rooms - REMOVED, using Graphics now

    // Background Layer
    Box(modifier = Modifier.fillMaxSize()) {
        when (currentRoom) {
            Room.LIVING_ROOM -> LivingRoomBackground(Modifier.fillMaxSize())
            Room.KITCHEN -> KitchenBackground(Modifier.fillMaxSize())
            Room.BEDROOM -> BedroomBackground(Modifier.fillMaxSize())
            Room.BATHROOM -> BathroomBackground(Modifier.fillMaxSize())
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            NeedsDock(
                petState = petState,
                currentRoom = currentRoom,
                onRoomSelect = { currentRoom = it }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Top HUD (Coins & Level)
            TopHud(userCoins = user?.coins ?: 0)

            // Message Toast
            showMessage?.let { message ->
                MessageToast(message)
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(2000)
                    showMessage = null
                }
            }

            // Main Content Area (Pet)
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                // Determine Pet Emotion
                val emotion = remember(petState, currentRoom) {
                    derivedStateOf {
                        val state = petState
                        if (state == null) PetEmotion.NEUTRAL
                        else if (currentRoom == Room.BEDROOM && state.energy < 90) PetEmotion.SLEEPY
                        else if (currentRoom == Room.KITCHEN && state.hunger < 50) PetEmotion.SAD
                        else if (state.happiness < 30) PetEmotion.SAD
                        else PetEmotion.HAPPY
                    }
                }

                // The Pet
                Box(
                    modifier = Modifier
                        .offset(y = 60.dp) // Position on floor
                        .size(300.dp)
                        .scale(breathScale)
                        .clickable {
                            scope.launch {
                                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                petController.handleInteraction(Interaction.TAP)
                                showMessage = "Pet tapped! +5 coins"
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    PetView(
                        modifier = Modifier.fillMaxSize(),
                        emotion = emotion.value,
                        primaryColor = if (currentPet?.id == "dog") Color(0xFF8D6E63) else Color(0xFFFF9F1C), // Brown for dog, Orange for cat
                        secondaryColor = Color(0xFFFFF3E0)
                    )
                    
                    // Sleep Overlay Zzz
                    if (currentRoom == Room.BEDROOM && (petState?.energy ?: 0f) < 90) {
                        Text("💤", fontSize = 60.sp, modifier = Modifier.align(Alignment.TopEnd))
                    }
                }

                // Room Specific Controls
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp)
                ) {
                    when (currentRoom) {
                        Room.LIVING_ROOM -> {
                            // Voice Controls
                             Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Button(
                                    onClick = {
                                        if (!isRecording) {
                                            scope.launch {
                                                isRecording = true
                                                val result = mediaController.startRecording()
                                                isRecording = false
                                                when (result) {
                                                    is MediaController.RecordingResult.Success -> {
                                                        recordingFile = result.file
                                                        showMessage = "Recording saved!"
                                                        // Auto play back
                                                        val pitchShift = currentPet?.voiceEffect?.pitchShift ?: 1.0f
                                                        mediaController.playWithEffect(result.file, pitchShift)
                                                    }
                                                    else -> showMessage = "Recording failed"
                                                }
                                            }
                                        } else {
                                            mediaController.stopRecording()
                                            isRecording = false
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isRecording) StatHunger else GamePrimary,
                                        contentColor = Color.White
                                    ),
                                    shape = CircleShape,
                                    modifier = Modifier.size(72.dp)
                                ) {
                                    Icon(if (isRecording) Icons.Default.Close else Icons.Default.Call, contentDescription = "Talk", modifier = Modifier.size(32.dp))
                                }
                            }
                        }
                        Room.KITCHEN -> {
                            GameActionButton(Icons.Default.ShoppingCart, "Feed", StatHunger) {
                                scope.launch {
                                    petController.handleInteraction(Interaction.FEED)
                                    showMessage = "Yum! +5 coins"
                                }
                            }
                        }
                        Room.BEDROOM -> {
                            GameActionButton(Icons.Default.Star, "Sleep", StatEnergy) {
                                scope.launch {
                                    petController.handleInteraction(Interaction.REST)
                                    showMessage = "Zzz... +5 coins"
                                }
                            }
                        }
                        Room.BATHROOM -> {
                            GameActionButton(Icons.Default.Info, "Clean", StatHappiness) { // Using Info as placeholder for Clean
                                scope.launch {
                                    petController.handleInteraction(Interaction.CLEAN)
                                    showMessage = "Sparkling clean! +5 coins"
                                }
                            }
                        }
                    }
                }
            }
            
            // Navigation Buttons (Shop, etc) - Floating Top Left
            Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(16.dp)
            ) {
                SmallNavButton(Icons.Default.ShoppingCart, "Shop") { navController.navigate(Screen.Shop.route) }
                Spacer(modifier = Modifier.height(8.dp))
                SmallNavButton(Icons.Default.List, "Items") { navController.navigate(Screen.Inventory.route) }
                Spacer(modifier = Modifier.height(8.dp))
                SmallNavButton(Icons.Default.Settings, "Settings") { navController.navigate(Screen.Settings.route) }
            }
        }
    }
}

enum class Room {
    LIVING_ROOM, KITCHEN, BEDROOM, BATHROOM
}

@Composable
fun NeedsDock(
    petState: PetState?,
    currentRoom: Room,
    onRoomSelect: (Room) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .shadow(8.dp, RoundedCornerShape(32.dp))
            .clip(RoundedCornerShape(32.dp)),
        color = Color.White.copy(alpha = 0.95f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NeedIcon(
                icon = Icons.Default.ShoppingCart, // Kitchen
                value = petState?.hunger ?: 100f,
                color = StatHunger,
                isSelected = currentRoom == Room.KITCHEN,
                onClick = { onRoomSelect(Room.KITCHEN) }
            )
            NeedIcon(
                icon = Icons.Default.Info, // Bathroom
                value = petState?.hygiene ?: 100f,
                color = Color(0xFF4DD0E1),
                isSelected = currentRoom == Room.BATHROOM,
                onClick = { onRoomSelect(Room.BATHROOM) }
            )
            NeedIcon(
                icon = Icons.Default.Star, // Bedroom
                value = petState?.energy ?: 100f,
                color = StatEnergy,
                isSelected = currentRoom == Room.BEDROOM,
                onClick = { onRoomSelect(Room.BEDROOM) }
            )
            NeedIcon(
                icon = Icons.Default.Face, // Living Room
                value = petState?.happiness ?: 100f,
                color = StatHappiness,
                isSelected = currentRoom == Room.LIVING_ROOM,
                onClick = { onRoomSelect(Room.LIVING_ROOM) }
            )
        }
    }
}

@Composable
fun NeedIcon(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: Float,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.BottomCenter
        ) {
            // Icon
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .padding(4.dp),
                tint = if (isSelected) color else Color.Gray
            )
        }
        // Mini Bar
        LinearProgressIndicator(
            progress = value / 100f,
            modifier = Modifier
                .width(40.dp)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = Color.LightGray.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun TopHud(userCoins: Int) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, end = 16.dp),
        color = Color.Transparent
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .shadow(4.dp, CircleShape),
                shape = CircleShape,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Coins", tint = Color(0xFFFFD700))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "$userCoins",
                        fontWeight = FontWeight.Bold,
                        color = GameOnSurface,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}

@Composable
fun MessageToast(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 100.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = GameSecondary),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )
        }
    }
}

@Composable
fun SmallNavButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 4.dp,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = GameOnSurface)
        }
    }
}

@Composable
fun GameActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            onClick = onClick,
            modifier = Modifier
                .size(72.dp)
                .shadow(4.dp, CircleShape),
            shape = CircleShape,
            color = color,
            contentColor = Color.White
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = label, modifier = Modifier.size(36.dp))
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = GameOnSurface,
            modifier = Modifier.background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(4.dp)).padding(horizontal = 4.dp)
        )
    }
}

fun getPlaceholderEmoji(petId: String?): String {
    return when (petId) {
        "cat" -> "🐱"
        "dog" -> "🐶"
        "bunny" -> "🐰"
        else -> "🐾"
    }
}
