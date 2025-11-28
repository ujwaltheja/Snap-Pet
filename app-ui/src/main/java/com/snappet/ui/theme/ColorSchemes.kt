package com.snappet.ui.theme

import androidx.compose.ui.graphics.Color

data class SnapPetColorScheme(
    val primary: Color,
    val secondary: Color,
    val tertiary: Color,
    val background: List<Color>, // Gradient
    val surface: Color,
    val accent: Color,
    val glow: Color,
    val textPrimary: Color = Color(0xFF2B2D42),
    val textSecondary: Color = Color(0xFF5D4037)
)

// Cat Theme: Warm oranges and purples
val CatColorScheme = SnapPetColorScheme(
    primary = Color(0xFFFF6B6B),
    secondary = Color(0xFFFFA94D),
    tertiary = Color(0xFFBB86FC),
    background = listOf(Color(0xFFFFF3E0), Color(0xFFFFE0B2)),
    surface = Color(0xFFFFFFFF).copy(alpha = 0.7f),
    accent = Color(0xFFFFD54F),
    glow = Color(0xFFFF6B6B).copy(alpha = 0.3f)
)

// Dog Theme: Playful blues and greens
val DogColorScheme = SnapPetColorScheme(
    primary = Color(0xFF4FC3F7),
    secondary = Color(0xFF81C784),
    tertiary = Color(0xFFFFF176),
    background = listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC)),
    surface = Color(0xFFFFFFFF).copy(alpha = 0.7f),
    accent = Color(0xFFFF8A65),
    glow = Color(0xFF4FC3F7).copy(alpha = 0.3f)
)

// Bunny Theme: Soft pinks and purples
val BunnyColorScheme = SnapPetColorScheme(
    primary = Color(0xFFF06292),
    secondary = Color(0xFFBA68C8),
    tertiary = Color(0xFF4DD0E1),
    background = listOf(Color(0xFFFCE4EC), Color(0xFFF8BBD0)),
    surface = Color(0xFFFFFFFF).copy(alpha = 0.7f),
    accent = Color(0xFFFFD54F),
    glow = Color(0xFFF06292).copy(alpha = 0.3f)
)

// Default fallback
val DefaultColorScheme = CatColorScheme
