package com.snappet.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.snappet.ui.theme.SnapPetTheme

@Composable
fun AnimatedCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = SnapPetTheme.colors.surface,
    elevation: Dp = 8.dp,
    shape: RoundedCornerShape = RoundedCornerShape(24.dp),
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .shadow(elevation, shape)
            .clip(shape)
            .background(backgroundColor)
    ) {
        content()
    }
}
