package com.snappet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.snappet.core.PetController
import com.snappet.core.models.Pet
import com.snappet.persistence.PersistenceRepository
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Your Pet") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
            .clickable(enabled = isUnlocked) { onSelect() },
        colors = if (isSelected) CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ) else CardDefaults.cardColors()
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
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (isUnlocked) {
                    Text(text = getPlaceholderEmoji(pet.id), fontSize = 48.sp)
                } else {
                    Icon(
                        Icons.Default.Lock,
                        contentDescription = "Locked",
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Pet info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = pet.species,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = pet.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (!isUnlocked) {
                    Text(
                        text = "Cost: ${pet.unlockCost} coins",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Selected indicator
            if (isSelected) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Selected",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
