package com.snappet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = GamePrimary,
    onPrimary = GameOnPrimary,
    primaryContainer = GamePrimaryContainer,
    onPrimaryContainer = Color(0xFF5C3A00),
    secondary = GameSecondary,
    onSecondary = Color.White,
    background = GameBackground,
    onBackground = GameOnSurface,
    surface = GameSurface,
    onSurface = GameOnSurface
)

private val DarkColorScheme = lightColorScheme( // Force light/vibrant theme for game look for now
    primary = GamePrimary,
    onPrimary = GameOnPrimary,
    primaryContainer = GamePrimaryContainer,
    onPrimaryContainer = Color(0xFF5C3A00),
    secondary = GameSecondary,
    onSecondary = Color.White,
    background = GameBackground,
    onBackground = GameOnSurface,
    surface = GameSurface,
    onSurface = GameOnSurface
)

@Composable
fun SnapPetTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
