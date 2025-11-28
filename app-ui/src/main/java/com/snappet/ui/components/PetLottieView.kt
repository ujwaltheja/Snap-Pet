package com.snappet.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.airbnb.lottie.compose.*
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.snappet.ui.graphics.PetEmotion

@Composable
fun PetLottieView(
    petId: String,
    assetName: String,
    emotion: PetEmotion,
    modifier: Modifier = Modifier,
    onTap: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // Determine animation file based on emotion
    // Expected format: animations/{petId}_{emotion}.json
    val emotionSuffix = when (emotion) {
        PetEmotion.HAPPY -> "happy"
        PetEmotion.SAD -> "sad"
        PetEmotion.SLEEPY -> "sleep"
        PetEmotion.EATING -> "eat"
        PetEmotion.SHOCKED -> "shock"
        PetEmotion.NEUTRAL -> "idle"
    }
    
    val targetAsset = "animations/${petId}_${emotionSuffix}.json"
    
    // State to hold the valid asset to play. Start with the default (idle) asset.
    var finalAsset by remember(assetName) { mutableStateOf(assetName) }

    // Check if specific emotion asset exists in background to avoid Main Thread Disk I/O (Strict Mode violation)
    LaunchedEffect(targetAsset, assetName) {
        withContext(Dispatchers.IO) {
            try {
                // Try to open the target asset to see if it exists
                context.assets.open(targetAsset).close()
                // If successful, update the state
                finalAsset = targetAsset
            } catch (e: Exception) {
                // If failed, fallback to the provided default asset
                finalAsset = assetName
            }
        }
    }

    // Try to load Lottie animation
    val composition by rememberLottieComposition(LottieCompositionSpec.Asset(finalAsset))

    if (composition != null) {
        // Use Lottie animation if available
        val progress by animateLottieCompositionAsState(
            composition = composition,
            iterations = LottieConstants.IterateForever,
            speed = when (emotion) {
                PetEmotion.SLEEPY -> 0.5f
                PetEmotion.EATING -> 1.5f
                PetEmotion.HAPPY -> 1.2f
                else -> 1.0f
            }
        )

        LottieAnimation(
            composition = composition,
            progress = { progress },
            modifier = modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null // No ripple on pet tap to keep it clean
            ) { onTap() }
        )
    } else {
        // Fallback to AnimatedPetView if Lottie fails entirely
        AnimatedPetView(
            petId = petId,
            emotion = emotion,
            modifier = modifier,
            onTap = onTap
        )
    }
}
