package com.snappet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.snappet.persistence.PersistenceRepository
import com.snappet.persistence.entities.ItemEntity
import com.snappet.store.StoreRepository
import com.snappet.ui.components.*
import com.snappet.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShopScreen(
    navController: NavController,
    storeRepository: StoreRepository,
    persistenceRepository: PersistenceRepository
) {
    val items by storeRepository.observeAllItems().collectAsState(initial = emptyList())
    val user by persistenceRepository.observeUser().collectAsState(initial = null)
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }

    SnapPetTheme {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Back Button
                    SmallNavButton(
                        icon = Icons.Default.ArrowBack,
                        label = "Back",
                        onClick = { navController.popBackStack() }
                    )

                    // Title
                    Text(
                        text = "Shop",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = SnapPetTheme.colors.textPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    // Coins
                    CoinDisplay(
                        coins = user?.coins ?: 0,
                        modifier = Modifier.align(Alignment.CenterEnd)
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
                // Floating particles background
                FloatingParticles(
                    modifier = Modifier.fillMaxSize(),
                    particleColor = SnapPetTheme.colors.glow,
                    particleCount = 20
                )
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    message?.let { msg ->
                        MessageToast(msg)
                        LaunchedEffect(msg) {
                            kotlinx.coroutines.delay(2000)
                            message = null
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(items) { item ->
                            ShopItemCard(
                                item = item,
                                userCoins = user?.coins ?: 0,
                                onPurchase = {
                                    scope.launch {
                                        val result = storeRepository.purchaseItem(item.itemId)
                                        message = when (result) {
                                            is StoreRepository.PurchaseResult.Success -> "Purchased ${item.name}!"
                                            is StoreRepository.PurchaseResult.InsufficientFunds -> "Not enough coins!"
                                            is StoreRepository.PurchaseResult.AlreadyOwned -> "Already owned!"
                                            else -> "Purchase failed"
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
}

@Composable
fun ShopItemCard(
    item: ItemEntity,
    userCoins: Int,
    onPurchase: () -> Unit
) {
    Card3D(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White.copy(alpha = 0.95f),
        elevation = 12.dp,
        glowColor = if (userCoins >= item.cost && !item.owned)
            SnapPetTheme.colors.primary else Color.Gray.copy(alpha = 0.3f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon with 3D effect
            Card3D(
                modifier = Modifier.size(72.dp),
                backgroundColor = SnapPetTheme.colors.primary.copy(alpha = 0.15f),
                elevation = 8.dp,
                glowColor = SnapPetTheme.colors.primary
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
                Spacer(modifier = Modifier.height(8.dp))

                // Price badge with shimmer
                Surface(
                    color = Color(0xFFFFD700).copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = "Cost",
                            modifier = Modifier.size(20.dp),
                            tint = Color(0xFFFFD700)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${item.cost}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SnapPetTheme.colors.textPrimary
                        )
                    }
                }
            }

            if (item.owned) {
                PulsingCard(
                    modifier = Modifier,
                    backgroundColor = SnapPetTheme.colors.secondary.copy(alpha = 0.15f),
                    pulseColor = SnapPetTheme.colors.secondary
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            Icons.Default.Check,
                            contentDescription = "Owned",
                            tint = SnapPetTheme.colors.secondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            "Owned",
                            color = SnapPetTheme.colors.secondary,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            } else {
                Button3D(
                    onClick = onPurchase,
                    text = "Buy",
                    enabled = userCoins >= item.cost,
                    backgroundColor = if (userCoins >= item.cost)
                        SnapPetTheme.colors.primary else Color.Gray,
                    height = 48.dp,
                    modifier = Modifier.width(100.dp)
                )
            }
        }
    }
}
