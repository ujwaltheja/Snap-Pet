package com.snappet.core.models

/**
 * Represents a pet type with its characteristics and behaviors.
 */
data class Pet(
    val id: String,
    val name: String,
    val species: String,
    val description: String,
    val animationAsset: String, // Path to Lottie JSON or sprite sheet
    val soundAsset: String?, // Path to sound file
    val voiceEffect: VoiceEffect,
    val unlockCost: Int = 0, // Cost to unlock, 0 means available by default
    val rarity: Rarity = Rarity.COMMON
) {
    enum class Rarity {
        COMMON, RARE, EPIC, LEGENDARY
    }

    /**
     * Voice effect configuration for talk-back feature.
     */
    data class VoiceEffect(
        val pitchShift: Float = 1.0f, // 0.5 = lower, 2.0 = higher
        val tempoMultiplier: Float = 1.0f, // 0.5 = slower, 2.0 = faster
        val effectName: String = "Normal"
    )

    companion object {
        // Default pets available in the app
        val DEFAULT_PETS = listOf(
            Pet(
                id = "cat",
                name = "Whiskers",
                species = "Cat",
                description = "A playful and curious cat",
                animationAsset = "animations/cat_idle.json",
                soundAsset = "sounds/cat_meow.mp3",
                voiceEffect = VoiceEffect(pitchShift = 1.5f, tempoMultiplier = 1.2f, effectName = "Squeaky"),
                unlockCost = 0
            ),
            Pet(
                id = "dog",
                name = "Buddy",
                species = "Dog",
                description = "A loyal and energetic dog",
                animationAsset = "animations/dog_idle.json",
                soundAsset = "sounds/dog_bark.mp3",
                voiceEffect = VoiceEffect(pitchShift = 0.8f, tempoMultiplier = 0.9f, effectName = "Deep"),
                unlockCost = 100
            ),
            Pet(
                id = "bunny",
                name = "Fluffy",
                species = "Bunny",
                description = "A cute and gentle bunny",
                animationAsset = "animations/bunny_idle.json",
                soundAsset = "sounds/bunny_squeak.mp3",
                voiceEffect = VoiceEffect(pitchShift = 1.8f, tempoMultiplier = 1.5f, effectName = "High Pitched"),
                unlockCost = 200,
                rarity = Rarity.RARE
            )
        )
    }
}
