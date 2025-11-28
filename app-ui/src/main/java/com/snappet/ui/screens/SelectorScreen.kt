package com.snappet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.snappet.core.PetController
import com.snappet.core.models.Pet
import com.snappet.persistence.PersistenceRepository
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

    // Background Gradient
    val bgBrush = Brush.verticalGradient(
        colors = listOf(BgGradientStart, BgGradientEnd)
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Select Your Pet", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = GameOnSurface)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(bgBrush)
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(Pet.DEFAULT_PETS) { pet ->
                    val isUnlocked = unlockedPetIds.contains(pet.id)
                    val isSelected = currentPet?.id == pet.id

                    PetCard(
                        pet = pet,
                        isUnlocked = isUnlocked,
                        isSelected = isSelected,
                        onSelect = {
                            if (isUnlocked) {
                                scope.launch {
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

@Composable
fun PetCard(
    pet: Pet,
    isUnlocked: Boolean,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(enabled = isUnlocked) { onSelect() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) GamePrimaryContainer else Color.White
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, GamePrimary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pet icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(if (isUnlocked) GameBackground else Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(text = getPlaceholderEmoji(pet.id), fontSize = 48.sp)
                } else {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(32.dp),
                        tint = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Pet info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GameOnSurface
                )
                Text(
                    text = pet.species,
                    style = MaterialTheme.typography.bodyMedium,
                    color = GameOnSurface.copy(alpha = 0.7f)
                )
                Text(
                    text = pet.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = GameOnSurface.copy(alpha = 0.5f)
                )
                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        color = GameSecondary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "Cost: ${pet.unlockCost}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Selected indicator
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = GamePrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
