package com.snappet.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.snappet.ui.components.*
import com.snappet.ui.navigation.Screen
import com.snappet.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
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
                        text = "Settings",
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
                            colors = SnapPetTheme.colors.background
                        )
                    )
                    .padding(padding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Snap Pet",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = SnapPetTheme.colors.primary
                    )
                    Text(
                        text = "Version 1.0.0",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SnapPetTheme.colors.textSecondary.copy(alpha = 0.6f)
                    )

                    Spacer(modifier = Modifier.height(32.dp))

                    AnimatedCard(
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.9f)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "About",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SnapPetTheme.colors.textPrimary
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "A virtual pet app with no ads, no monetization, and no external telemetry. " +
                                        "All data is stored locally on your device.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = SnapPetTheme.colors.textSecondary,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    GlossyButton(
                        onClick = { navController.navigate(Screen.Debug.route) },
                        text = "Debug Logs",
                        backgroundColor = SnapPetTheme.colors.secondary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
