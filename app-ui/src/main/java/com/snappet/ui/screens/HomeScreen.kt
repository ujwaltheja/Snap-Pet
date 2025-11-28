package com.snappet.ui.screens

import android.Manifest
import android.content.Context
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.navigation.NavController
import com.snappet.core.PetController
import com.snappet.core.models.Interaction
import com.snappet.core.models.PetState
import com.snappet.media.MediaController
import com.snappet.persistence.PersistenceRepository
import com.snappet.ui.components.*
import com.snappet.ui.graphics.*
import com.snappet.ui.navigation.Screen
import com.snappet.ui.theme.*
import kotlinx.coroutines.launch
import java.io.File
import androidx.compose.ui.draw.blur

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
    var hasAudioPermission by remember { mutableStateOf(false) }

    // Audio permission launcher
    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasAudioPermission = isGranted
        if (!isGranted) {
            showMessage = "Microphone permission required for voice recording"
        }
    }

    // Reward States
    var dailyReward by remember { mutableStateOf<com.snappet.core.DailyReward?>(null) }
    var levelUpLevel by remember { mutableStateOf<Int?>(null) }

    LaunchedEffect(petController) {
        launch {
            petController.dailyRewardEvent.collect { reward ->
                dailyReward = reward
            }
        }
        launch {
            petController.levelUpEvent.collect { level ->
                levelUpLevel = level
            }
        }
    }
    
    // Room State
    var currentRoom by remember { mutableStateOf(Room.LIVING_ROOM) }

    // Animations
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")
    // breathScale removed as it was unused

    SnapPetTheme(petId = currentPet?.id) {
        // Background Layer with Parallax
        Box(modifier = Modifier.fillMaxSize()) {
            // Animated gradient background
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = SnapPetTheme.colors.background + listOf(
                                SnapPetTheme.colors.background.last().copy(alpha = 0.8f)
                            )
                        )
                    )
            )

            // Floating particles
            FloatingParticles(
                modifier = Modifier.fillMaxSize(),
                particleColor = SnapPetTheme.colors.glow,
                particleCount = 30
            )

            // Room specific background
            Box(modifier = Modifier.fillMaxSize().blur(2.dp)) {
                when (currentRoom) {
                    Room.LIVING_ROOM -> LivingRoomBackground(Modifier.fillMaxSize())
                    Room.KITCHEN -> KitchenBackground(Modifier.fillMaxSize())
                    Room.BEDROOM -> BedroomBackground(Modifier.fillMaxSize())
                    Room.BATHROOM -> BathroomBackground(Modifier.fillMaxSize())
                }
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
                // Top Status Bar (3D Glassmorphic)
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    backgroundColor = Color.White.copy(alpha = 0.2f)
                ) {
                    TopStatusBar(
                        userCoins = user?.coins ?: 0,
                        petLevel = petState?.level ?: 1,
                        unlockedPetIds = user?.unlockedPetIds?.split(",")?.map { it.trim() } ?: listOf("cat"),
                        currentPetId = currentPet?.id,
                        onPetSelect = { petId ->
                            scope.launch {
                                user?.let { u ->
                                    persistenceRepository.updateUser(u.copy(selectedPetId = petId))
                                    petController.loadPet(petId)
                                }
                            }
                        },
                        onSettingsClick = { navController.navigate(Screen.Settings.route) },
                        onShopClick = { navController.navigate(Screen.Shop.route) }
                    )
                }

                // Message Toast
                showMessage?.let { message ->
                    MessageToast(message)
                    LaunchedEffect(message) {
                        kotlinx.coroutines.delay(2000)
                        showMessage = null
                    }
                }

                // Daily Reward Dialog
                dailyReward?.let { reward ->
                    RewardDialog(
                        title = "Daily Reward! ☀️",
                        message = "Streak: ${reward.streak} days\n+${reward.coins} Coins",
                        onDismiss = { dailyReward = null }
                    )
                }

                // Level Up Dialog
                levelUpLevel?.let { level ->
                    RewardDialog(
                        title = "Level Up! 🎉",
                        message = "Your pet reached Level $level!\n+${level * 100} Coins",
                        onDismiss = { levelUpLevel = null }
                    )
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

                    // The Pet (Talking Tom Style - Large and Visible)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(500.dp)
                            .offset(y = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Radial glow behind pet
                        val glowColor = SnapPetTheme.colors.glow
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            drawCircle(
                                brush = Brush.radialGradient(
                                    colors = listOf(
                                        glowColor.copy(alpha = 0.4f),
                                        Color.Transparent
                                    ),
                                    radius = size.width * 0.5f
                                )
                            )
                        }

                        // Large Visible Pet
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.9f)
                                .fillMaxHeight()
                        ) {
                            PetLottieView(
                                petId = currentPet?.id ?: "cat",
                                assetName = currentPet?.animationAsset ?: "animations/cat_idle.json",
                                emotion = emotion.value,
                                modifier = Modifier.fillMaxSize(),
                                onTap = {
                                    // Fallback tap
                                    scope.launch {
                                        petController.handleInteraction(Interaction.TAP)
                                        showMessage = "Yay! +5 coins 🎉"
                                    }
                                }
                            )

                            // Head Zone
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .fillMaxHeight(0.25f)
                                    .align(Alignment.TopCenter)
                                    .offset(y = 80.dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                    ) {
                                        scope.launch {
                                            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                                            petController.handleInteraction(Interaction.PET)
                                            showMessage = "Purr... 😺"
                                        }
                                    }
                            )

                            // Belly Zone
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .fillMaxHeight(0.3f)
                                    .align(Alignment.Center)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                    ) {
                                        scope.launch {
                                            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                                            petController.handleInteraction(Interaction.POKE)
                                            showMessage = "Tickle tickle! 😂"
                                        }
                                    }
                            )

                            // Feet Zone
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.6f)
                                    .fillMaxHeight(0.2f)
                                    .align(Alignment.BottomCenter)
                                    .offset(y = (-100).dp)
                                    .clickable(
                                        indication = null,
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
                                    ) {
                                        scope.launch {
                                            vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
                                            petController.handleInteraction(Interaction.TAP)
                                            showMessage = "High five! 🐾"
                                        }
                                    }
                            )
                        }

                        // Pet Stats Overlay (Top of pet area)
                        Column(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .padding(top = 16.dp)
                        ) {
                            GlassmorphicCard(
                                backgroundColor = Color.Black.copy(alpha = 0.4f),
                                borderColor = Color.White.copy(alpha = 0.3f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    EnergyBar(
                                        value = petState?.hunger ?: 100f,
                                        label = "Hunger",
                                        color = StatHunger,
                                        icon = Icons.Default.ShoppingCart,
                                        modifier = Modifier.weight(1f)
                                    )
                                    EnergyBar(
                                        value = petState?.energy ?: 100f,
                                        label = "Energy",
                                        color = StatEnergy,
                                        icon = Icons.Default.Star,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Talking Tom Style Action Panel - Always visible
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 16.dp)
                            .fillMaxWidth()
                    ) {
                        TalkingTomPanel(
                            actions = listOf(
                                PetAction(
                                    icon = Icons.Default.ShoppingCart,
                                    label = "Feed",
                                    color = StatHunger,
                                    emoji = "🍎",
                                    onClick = {
                                        scope.launch {
                                            petController.handleInteraction(Interaction.FEED)
                                            showMessage = "Nom nom! 😋"
                                        }
                                    }
                                ),
                                PetAction(
                                    icon = Icons.Default.Call,
                                    label = "Talk",
                                    color = SnapPetTheme.colors.primary,
                                    emoji = "🎤",
                                    onClick = {
                                        if (!isRecording) {
                                            // Check permission first
                                            if (!hasAudioPermission) {
                                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            } else {
                                                scope.launch {
                                                    isRecording = true
                                                    val result = mediaController.startRecording()
                                                    isRecording = false
                                                    when (result) {
                                                        is MediaController.RecordingResult.Success -> {
                                                            recordingFile = result.file
                                                            showMessage = "Hehe! 🎵"
                                                            val pitchShift =
                                                                currentPet?.voiceEffect?.pitchShift ?: 1.0f
                                                            mediaController.playWithEffect(
                                                                result.file,
                                                                pitchShift
                                                            )
                                                        }

                                                        else -> showMessage = "Oops! Try again"
                                                    }
                                                }
                                            }
                                        }
                                    }
                                ),
                                PetAction(
                                    icon = Icons.Default.Star,
                                    label = "Play",
                                    color = StatHappiness,
                                    emoji = "⚽",
                                    onClick = {
                                        navController.navigate(Screen.CatchGame.route)
                                    }
                                ),
                                PetAction(
                                    icon = Icons.Default.Face,
                                    label = "Sleep",
                                    color = StatEnergy,
                                    emoji = "😴",
                                    onClick = {
                                        scope.launch {
                                            petController.handleInteraction(Interaction.REST)
                                            showMessage = "Zzz... 💤"
                                        }
                                    }
                                )
                            )
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TopStatusBar(
    userCoins: Int,
    petLevel: Int,
    unlockedPetIds: List<String>,
    currentPetId: String?,
    onPetSelect: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onShopClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Left: Navigation
        Row(
            modifier = Modifier.align(Alignment.TopStart),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SmallNavButton(Icons.Default.ShoppingCart, "Shop", onShopClick)
            SmallNavButton(Icons.Default.Settings, "Settings", onSettingsClick)
        }

        // Center: Coins and Level
        Column(
            modifier = Modifier.align(Alignment.TopCenter),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CoinDisplay(
                coins = userCoins
            )
            LevelBadge(level = petLevel)
        }

        // Right: Pet Switcher
        PetSwitcher(
            unlockedPetIds = unlockedPetIds,
            currentPetId = currentPetId,
            onPetSelect = onPetSelect,
            modifier = Modifier.align(Alignment.TopEnd)
        )
    }
}

@Composable
fun PetSwitcher(
    unlockedPetIds: List<String>,
    currentPetId: String?,
    onPetSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    GlassmorphicCard(
        modifier = modifier,
        backgroundColor = Color.White.copy(alpha = 0.2f),
        borderColor = Color.White.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            unlockedPetIds.forEach { petId ->
                val isSelected = petId == currentPetId
                var isPressed by remember { mutableStateOf(false) }

                val scale by animateFloatAsState(
                    targetValue = if (isPressed) 0.85f else if (isSelected) 1.1f else 1.0f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                )

                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .scale(scale)
                        .shadow(
                            elevation = if (isSelected) 8.dp else 4.dp,
                            shape = CircleShape,
                            spotColor = if (isSelected) SnapPetTheme.colors.primary else Color.Gray
                        )
                        .clip(CircleShape)
                        .background(
                            brush = if (isSelected) Brush.radialGradient(
                                colors = listOf(
                                    SnapPetTheme.colors.primary,
                                    SnapPetTheme.colors.primary
                                        .copy(
                                            red = SnapPetTheme.colors.primary.red * 0.7f,
                                            green = SnapPetTheme.colors.primary.green * 0.7f,
                                            blue = SnapPetTheme.colors.primary.blue * 0.7f
                                        )
                                )
                            ) else Brush.radialGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.4f)
                                )
                            )
                        )
                        .border(
                            width = if (isSelected) 3.dp else 0.dp,
                            color = Color.White,
                            shape = CircleShape
                        )
                        .clickable {
                            onPetSelect(petId)
                        }
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    isPressed = true
                                    tryAwaitRelease()
                                    isPressed = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        // Pulse ring effect for selected pet
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val pulseScale by infiniteTransition.animateFloat(
                            initialValue = 1f,
                            targetValue = 1.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(1000, easing = FastOutSlowInEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "pulseScale"
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(pulseScale)
                                .background(
                                    Color.White.copy(alpha = 0.2f),
                                    CircleShape
                                )
                        )
                    }

                    Text(
                        getPlaceholderEmoji(petId),
                        fontSize = 22.sp
                    )
                }
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
    GlassmorphicCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        backgroundColor = Color.White.copy(alpha = 0.25f),
        borderColor = Color.White.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            DockItem(
                icon = Icons.Default.ShoppingCart,
                label = "Kitchen",
                value = petState?.hunger ?: 100f,
                color = StatHunger,
                isSelected = currentRoom == Room.KITCHEN,
                onClick = { onRoomSelect(Room.KITCHEN) }
            )
            DockItem(
                icon = Icons.Default.Info,
                label = "Bath",
                value = petState?.hygiene ?: 100f,
                color = Color(0xFF4DD0E1),
                isSelected = currentRoom == Room.BATHROOM,
                onClick = { onRoomSelect(Room.BATHROOM) }
            )
            DockItem(
                icon = Icons.Default.Star,
                label = "Sleep",
                value = petState?.energy ?: 100f,
                color = StatEnergy,
                isSelected = currentRoom == Room.BEDROOM,
                onClick = { onRoomSelect(Room.BEDROOM) }
            )
            DockItem(
                icon = Icons.Default.Face,
                label = "Play",
                value = petState?.happiness ?: 100f,
                color = StatHappiness,
                isSelected = currentRoom == Room.LIVING_ROOM,
                onClick = { onRoomSelect(Room.LIVING_ROOM) }
            )
        }
    }
}

@Composable
fun DockItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: Float,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.15f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "dockScale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clickable(
                interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(52.dp)
                .shadow(
                    elevation = if (isSelected) 8.dp else 2.dp,
                    shape = CircleShape,
                    spotColor = if (isSelected) color else Color.Black.copy(alpha = 0.2f)
                )
                .clip(CircleShape)
                .background(
                    brush = if (isSelected) Brush.radialGradient(
                        colors = listOf(
                            color,
                            color.copy(
                                red = color.red * 0.8f,
                                green = color.green * 0.8f,
                                blue = color.blue * 0.8f
                            )
                        )
                    ) else Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.9f),
                            Color.White.copy(alpha = 0.7f)
                        )
                    )
                )
        ) {
            // Shine for selected
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(26.dp)
                        .align(Alignment.TopCenter)
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.3f),
                                    Color.Transparent
                                )
                            )
                        )
                )
            }

            Icon(
                icon,
                contentDescription = label,
                modifier = Modifier.size(28.dp),
                tint = if (isSelected) Color.White else Color.Gray
            )
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        // Mini Bar (Glass)
        Box(
            modifier = Modifier
                .width(36.dp)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(Color.Black.copy(alpha = 0.1f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(value / 100f)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                color,
                                color.copy(alpha = 0.7f)
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun MessageToast(message: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 120.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        AnimatedCard(
            backgroundColor = SnapPetTheme.colors.secondary,
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                fontWeight = FontWeight.Bold
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
        shadowElevation = 6.dp,
        modifier = Modifier.size(48.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = label, tint = SnapPetTheme.colors.textPrimary, modifier = Modifier.size(24.dp))
        }
    }
}

@Composable
fun CircularGlossyButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, color: Color, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            color,
                            color.copy(red = color.red * 0.8f, green = color.green * 0.8f, blue = color.blue * 0.8f)
                        )
                    )
                )
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            // Shine
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    )
            )
            Icon(icon, contentDescription = label, modifier = Modifier.size(32.dp), tint = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            color = Color.White.copy(alpha = 0.8f),
            shape = RoundedCornerShape(12.dp),
            shadowElevation = 2.dp
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = SnapPetTheme.colors.textPrimary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
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

@Composable
fun LevelBadge(level: Int) {
    Surface(
        color = SnapPetTheme.colors.primary,
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 4.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Level",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
            Text(
                text = "Lvl $level",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun RewardDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        GlassmorphicCard(
            modifier = Modifier
                .padding(32.dp)
                .fillMaxWidth(),
            backgroundColor = SnapPetTheme.colors.surface
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium,
                    color = SnapPetTheme.colors.textPrimary,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = message,
                    style = MaterialTheme.typography.titleLarge,
                    color = SnapPetTheme.colors.textSecondary,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = SnapPetTheme.colors.primary
                    )
                ) {
                    Text("Awesome!")
                }
            }
        }
    }
}
