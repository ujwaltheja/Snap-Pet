package com.snappet.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// CompositionLocal for our custom color scheme
val LocalSnapPetColors = staticCompositionLocalOf { DefaultColorScheme }

// Helper to access colors easily
object SnapPetTheme {
    val colors: SnapPetColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalSnapPetColors.current
}

@Composable
fun SnapPetTheme(
    petId: String? = "cat",
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val snapPetColors = when (petId) {
        "dog" -> DogColorScheme
        "bunny" -> BunnyColorScheme
        else -> CatColorScheme
    }

    // Map to Material3 scheme for compatibility with standard components
    val materialColorScheme = lightColorScheme(
        primary = snapPetColors.primary,
        onPrimary = Color.White,
        primaryContainer = snapPetColors.primary.copy(alpha = 0.2f),
        onPrimaryContainer = snapPetColors.textPrimary,
        secondary = snapPetColors.secondary,
        onSecondary = Color.White,
        background = snapPetColors.background.first(),
        onBackground = snapPetColors.textPrimary,
        surface = snapPetColors.surface,
        onSurface = snapPetColors.textPrimary,
        tertiary = snapPetColors.tertiary
    )

    CompositionLocalProvider(
        LocalSnapPetColors provides snapPetColors
    ) {
        MaterialTheme(
            colorScheme = materialColorScheme,
            typography = Typography,
            content = content
        )
    }
}
