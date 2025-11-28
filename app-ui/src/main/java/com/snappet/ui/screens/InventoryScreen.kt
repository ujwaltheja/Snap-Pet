package com.snappet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.snappet.persistence.entities.ItemEntity
import com.snappet.store.StoreRepository
import com.snappet.ui.components.*
import com.snappet.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    navController: NavController,
    storeRepository: StoreRepository
) {
    val items by storeRepository.observeOwnedItems().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

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
                        text = "Inventory",
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
                    particleCount = 15
                )
                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No items owned yet. Visit the shop!",
                            style = MaterialTheme.typography.titleMedium,
                            color = SnapPetTheme.colors.textPrimary.copy(alpha = 0.6f)
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(items) { item ->
                            InventoryItemCard(
                                item = item,
                                onEquip = {
                                    scope.launch {
                                        storeRepository.equipItem(item.itemId)
                                    }
                                },
                                onUnequip = {
                                    scope.launch {
                                        storeRepository.unequipItem(item.itemId)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InventoryItemCard(
    item: ItemEntity,
    onEquip: () -> Unit,
    onUnequip: () -> Unit
) {
    Card3D(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.95f),
        elevation = if (item.equipped) 16.dp else 12.dp,
        glowColor = if (item.equipped)
            SnapPetTheme.colors.primary else Color.Gray.copy(alpha = 0.2f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with glow effect if equipped
            if (item.equipped) {
                PulsingCard(
                    modifier = Modifier.size(72.dp),
                    backgroundColor = SnapPetTheme.colors.primary.copy(alpha = 0.2f),
                    pulseColor = SnapPetTheme.colors.primary
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = item.name.take(1),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = SnapPetTheme.colors.primary
                        )
                    }
                }
            } else {
                Card3D(
                    modifier = Modifier.size(72.dp),
                    backgroundColor = Color.Gray.copy(alpha = 0.1f),
                    elevation = 8.dp,
                    glowColor = Color.Gray
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = item.name.take(1),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SnapPetTheme.colors.textPrimary
                )
                Text(
                    text = item.type.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = SnapPetTheme.colors.textSecondary.copy(alpha = 0.7f),
                    fontWeight = FontWeight.SemiBold
                )
                if (item.equipped) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = SnapPetTheme.colors.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "✓ Equipped",
                            style = MaterialTheme.typography.labelLarge,
                            color = SnapPetTheme.colors.primary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }

            Button3D(
                onClick = if (item.equipped) onUnequip else onEquip,
                text = if (item.equipped) "Remove" else "Equip",
                backgroundColor = if (item.equipped)
                    SnapPetTheme.colors.secondary else SnapPetTheme.colors.primary,
                height = 48.dp,
                modifier = Modifier.width(110.dp)
            )
        }
    }
}
