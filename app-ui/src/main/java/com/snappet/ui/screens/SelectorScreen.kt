package com.snappet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.snappet.core.PetController
import com.snappet.core.models.Pet
import com.snappet.persistence.PersistenceRepository
import com.snappet.ui.components.*
import com.snappet.ui.graphics.PetEmotion
import com.snappet.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectorScreen(
    navController: NavController,
    petController: PetController,
    persistenceRepository: PersistenceRepository
) {
    val user by persistenceRepository.observeUser().collectAsState(initial = null)
    val currentPet by petController.currentPet.collectAsState()
    val scope = rememberCoroutineScope()

    val unlockedPetIds = user?.unlockedPetIds?.split(",")?.map { it.trim() } ?: listOf("cat")

    SnapPetTheme {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    SmallNavButton(
                        icon = Icons.Default.ArrowBack,
                        label = "Back",
                        onClick = { navController.popBackStack() }
                    )

                    Text(
                        text = "Select Pet",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SnapPetTheme.colors.textPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        ) { padding ->
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
                    .padding(padding)
            ) {
                // Floating particles
                FloatingParticles(
                    modifier = Modifier.fillMaxSize(),
                    particleColor = SnapPetTheme.colors.glow,
                    particleCount = 25
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(Pet.DEFAULT_PETS) { pet ->
                        val isUnlocked = unlockedPetIds.contains(pet.id)
                        val isSelected = currentPet?.id == pet.id

                        PetCard(
                            pet = pet,
                            isUnlocked = isUnlocked,
                            isSelected = isSelected,
                            userCoins = user?.coins ?: 0,
                            onSelect = {
                                if (isUnlocked) {
                                    scope.launch {
                                        user?.let { u ->
                                            persistenceRepository.updateUser(u.copy(selectedPetId = pet.id))
                                            petController.loadPet(pet.id)
                                        }
                                    }
                                }
                            },
                            onUnlock = {
                                scope.launch {
                                    val success = persistenceRepository.unlockPet(pet.id, pet.unlockCost)
                                    if (success) {
                                        // Automatically select the newly unlocked pet
                                        user?.let { u ->
                                            persistenceRepository.updateUser(u.copy(selectedPetId = pet.id))
                                            petController.loadPet(pet.id)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PetCard(
    pet: Pet,
    isUnlocked: Boolean,
    isSelected: Boolean,
    userCoins: Int,
    onSelect: () -> Unit,
    onUnlock: () -> Unit = {}
) {
    Card3D(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isUnlocked) { onSelect() },
        backgroundColor = if (isSelected)
            SnapPetTheme.colors.primary.copy(alpha = 0.15f)
        else Color.White.copy(alpha = 0.95f),
        elevation = if (isSelected) 20.dp else 12.dp,
        glowColor = if (isSelected)
            SnapPetTheme.colors.primary else Color.Gray.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pet icon / Preview with 3D effect
            if (isUnlocked) {
                PulsingCard(
                    modifier = Modifier.size(100.dp),
                    backgroundColor = SnapPetTheme.colors.background.first().copy(alpha = 0.8f),
                    pulseColor = if (isSelected)
                        SnapPetTheme.colors.primary else SnapPetTheme.colors.accent
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        PetLottieView(
                            petId = pet.id,
                            assetName = pet.animationAsset,
                            emotion = PetEmotion.HAPPY,
                            modifier = Modifier.size(80.dp)
                        )
                    }
                }
            } else {
                Card3D(
                    modifier = Modifier.size(100.dp),
                    backgroundColor = Color.LightGray.copy(alpha = 0.3f),
                    elevation = 8.dp,
                    glowColor = Color.Gray
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Locked",
                            modifier = Modifier.size(40.dp),
                            tint = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            // Pet info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SnapPetTheme.colors.textPrimary
                )
                Text(
                    text = pet.species,
                    style = MaterialTheme.typography.bodyLarge,
                    color = SnapPetTheme.colors.textSecondary.copy(alpha = 0.8f),
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pet.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = SnapPetTheme.colors.textSecondary.copy(alpha = 0.6f)
                )
                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = SnapPetTheme.colors.secondary.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            2.dp,
                            SnapPetTheme.colors.secondary
                        )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = "Cost",
                                tint = Color(0xFFFFD700),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${pet.unlockCost}",
                                style = MaterialTheme.typography.labelLarge,
                                color = SnapPetTheme.colors.textPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Selected indicator or Buy button
            if (isUnlocked) {
                if (isSelected) {
                    PulsingCard(
                        modifier = Modifier.size(48.dp),
                        backgroundColor = SnapPetTheme.colors.primary,
                        pulseColor = SnapPetTheme.colors.primary
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            } else {
                Button3D(
                    onClick = onUnlock,
                    text = "Buy",
                    enabled = userCoins >= pet.unlockCost,
                    backgroundColor = if (userCoins >= pet.unlockCost)
                        SnapPetTheme.colors.primary else Color.Gray,
                    height = 48.dp,
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}
