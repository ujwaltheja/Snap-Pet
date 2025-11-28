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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.snappet.persistence.entities.ItemEntity
import com.snappet.store.StoreRepository
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

    // Background Gradient
    val bgBrush = Brush.verticalGradient(
        colors = listOf(BgGradientStart, BgGradientEnd)
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Inventory", fontWeight = FontWeight.Bold) },
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
            if (items.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "No items owned yet. Visit the shop!",
                        style = MaterialTheme.typography.bodyLarge,
                        color = GameOnSurface.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

@Composable
fun InventoryItemCard(
    item: ItemEntity,
    onEquip: () -> Unit,
    onUnequip: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = GameOnSurface
                )
                Text(
                    text = item.type.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = GameOnSurface.copy(alpha = 0.6f)
                )
                if (item.equipped) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Currently equipped",
                        style = MaterialTheme.typography.labelSmall,
                        color = GamePrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            if (item.equipped) {
                Button(
                    onClick = onUnequip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GameSecondary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Unequip")
                }
            } else {
                Button(
                    onClick = onEquip,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamePrimary,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Equip")
                }
            }
        }
    }
}
