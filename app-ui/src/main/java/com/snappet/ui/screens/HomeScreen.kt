package com.snappet.ui.screens

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Snap Pet") },
                actions = {
                    // Display coins
                    Row(
                        modifier = Modifier.padding(end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Star, contentDescription = "Coins", tint = Color(0xFFFFD700))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("${user?.coins ?: 0}", fontWeight = FontWeight.Bold)
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    selected = true,
                    onClick = { }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Favorite, contentDescription = "Selector") },
                    label = { Text("Selector") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Selector.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.ShoppingCart, contentDescription = "Shop") },
                    label = { Text("Shop") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Shop.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.List, contentDescription = "Inventory") },
                    label = { Text("Inventory") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Inventory.route) }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                    label = { Text("Settings") },
                    selected = false,
                    onClick = { navController.navigate(Screen.Settings.route) }
                )
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Show message if any
            showMessage?.let { message ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Text(
                        text = message,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                LaunchedEffect(message) {
                    kotlinx.coroutines.delay(2000)
                    showMessage = null
                }
            }

            // Pet name and info
            currentPet?.let { pet ->
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pet.species,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pet display (placeholder image)
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .clickable {
                        scope.launch {
                            vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                            petController.handleInteraction(Interaction.TAP)
                            showMessage = "Pet tapped! +5 coins"
                        }
                    }
                    .semantics { contentDescription = "Your virtual pet" },
                contentAlignment = Alignment.Center
            ) {
                // Placeholder pet image (would be Lottie animation in production)
                Text(
                    text = getPlaceholderEmoji(currentPet?.id),
                    fontSize = 120.sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Needs meters
            petState?.let { state ->
                NeedsMeter("Hunger", state.hunger, Color(0xFFFF6B6B))
                Spacer(modifier = Modifier.height(8.dp))
                NeedsMeter("Happiness", state.happiness, Color(0xFFFFD93D))
                Spacer(modifier = Modifier.height(8.dp))
                NeedsMeter("Energy", state.energy, Color(0xFF6BCF7F))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionButton(
                    icon = Icons.Default.Face,
                    label = "Pet",
                    onClick = {
                        scope.launch {
                            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                            petController.handleInteraction(Interaction.PET)
                            showMessage = "Pet is happy! +5 coins"
                        }
                    }
                )
                ActionButton(
                    icon = Icons.Default.Favorite,
                    label = "Feed",
                    onClick = {
                        scope.launch {
                            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                            petController.handleInteraction(Interaction.FEED)
                            showMessage = "Fed your pet! +5 coins"
                        }
                    }
                )
                ActionButton(
                    icon = Icons.Default.Star,
                    label = "Play",
                    onClick = {
                        scope.launch {
                            vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
                            petController.handleInteraction(Interaction.PLAY)
                            showMessage = "Playing with pet! +5 coins"
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Voice recording buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
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
                                    }
                                    else -> showMessage = "Recording failed"
                                }
                            }
                        } else {
                            mediaController.stopRecording()
                            isRecording = false
                        }
                    },
                    colors = if (isRecording) ButtonDefaults.buttonColors(containerColor = Color.Red)
                    else ButtonDefaults.buttonColors()
                ) {
                    Icon(if (isRecording) Icons.Default.Close else Icons.Default.Star, contentDescription = "Record")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isRecording) "Stop" else "Record")
                }

                Button(
                    onClick = {
                        scope.launch {
                            recordingFile?.let { file ->
                                val pitchShift = currentPet?.voiceEffect?.pitchShift ?: 1.0f
                                mediaController.playWithEffect(file, pitchShift)
                                showMessage = "Playing with effect!"
                            } ?: run {
                                showMessage = "No recording to play"
                            }
                        }
                    },
                    enabled = recordingFile != null && !isRecording
                ) {
                    Icon(Icons.Default.Star, contentDescription = "Play")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Play")
                }
            }
        }
    }
}

@Composable
fun NeedsMeter(label: String, value: Float, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.bodyMedium)
            Text("${value.toInt()}/100", style = MaterialTheme.typography.bodyMedium)
        }
        LinearProgressIndicator(
            progress = value / 100f,
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FilledIconButton(
            onClick = onClick,
            modifier = Modifier.size(56.dp)
        ) {
            Icon(icon, contentDescription = label)
        }
        Text(label, style = MaterialTheme.typography.labelSmall)
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
