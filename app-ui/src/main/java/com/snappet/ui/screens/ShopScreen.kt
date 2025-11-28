package com.snappet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Star
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
import com.snappet.persistence.PersistenceRepository
import com.snappet.persistence.entities.ItemEntity
import com.snappet.store.StoreRepository
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

    // Background Gradient
    val bgBrush = Brush.verticalGradient(
        colors = listOf(BgGradientStart, BgGradientEnd)
    )

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text("Shop", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { navController.popBackStack() },
                        colors = IconButtonDefaults.iconButtonColors(contentColor = GameOnSurface)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Surface(
                        shape = CircleShape,
                        color = Color.White,
                        shadowElevation = 2.dp,
                        modifier = Modifier.padding(end = 16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Star, contentDescription = "Coins", tint = Color(0xFFFFD700))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("${user?.coins ?: 0}", fontWeight = FontWeight.Bold, color = GameOnSurface)
                        }
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
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                message?.let { msg ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = GameSecondary
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(
                            text = msg,
                            modifier = Modifier.padding(12.dp),
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    LaunchedEffect(msg) {
                        kotlinx.coroutines.delay(2000)
                        message = null
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
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

@Composable
fun ShopItemCard(
    item: ItemEntity,
    userCoins: Int,
    onPurchase: () -> Unit
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
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "Cost",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFD700)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${item.cost}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = GameOnSurface
                    )
                }
            }

            if (item.owned) {
                Surface(
                    color = GameSecondary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Owned", tint = GameSecondary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Owned", color = GameSecondary, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                Button(
                    onClick = onPurchase,
                    enabled = userCoins >= item.cost,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GamePrimary,
                        contentColor = Color.White,
                        disabledContainerColor = Color.Gray.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Buy")
                }
            }
        }
    }
}
