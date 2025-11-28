package com.snappet.core.models

/**
 * Represents the current state of a pet including needs and statistics.
 */
data class PetState(
    val petId: String,
    val hunger: Float = 100f, // 0-100, decreases over time
    val happiness: Float = 100f, // 0-100, decreases over time
    val energy: Float = 100f, // 0-100, decreases over time
    val hygiene: Float = 100f, // 0-100, decreases over time
    val lastUpdated: Long = System.currentTimeMillis(),
    val equippedHat: String? = null,
    val equippedSkin: String? = null,
    val totalInteractions: Int = 0
) {
    companion object {
        // Decay rates per second
        const val HUNGER_DECAY_PER_SECOND = 0.01f // ~100 seconds to deplete
        const val HAPPINESS_DECAY_PER_SECOND = 0.008f
        const val ENERGY_DECAY_PER_SECOND = 0.005f
        const val HYGIENE_DECAY_PER_SECOND = 0.007f

        // Interaction effects
        const val FEED_HUNGER_INCREASE = 30f
        const val FEED_HAPPINESS_INCREASE = 5f
        const val PLAY_HAPPINESS_INCREASE = 25f
        const val PLAY_ENERGY_DECREASE = 15f
        const val PET_HAPPINESS_INCREASE = 15f
        const val POKE_HAPPINESS_DECREASE = 5f
        const val REST_ENERGY_INCREASE = 40f
        const val CLEAN_HYGIENE_INCREASE = 40f
        const val CLEAN_HAPPINESS_INCREASE = 10f
    }

    /**
     * Clamp a value between 0 and 100.
     */
    private fun Float.clamp() = coerceIn(0f, 100f)

    /**
     * Update state based on elapsed time.
     */
    fun updateWithElapsedTime(elapsedSeconds: Float): PetState {
        return copy(
            hunger = (hunger - HUNGER_DECAY_PER_SECOND * elapsedSeconds).clamp(),
            happiness = (happiness - HAPPINESS_DECAY_PER_SECOND * elapsedSeconds).clamp(),
            energy = (energy - ENERGY_DECAY_PER_SECOND * elapsedSeconds).clamp(),
            hygiene = (hygiene - HYGIENE_DECAY_PER_SECOND * elapsedSeconds).clamp(),
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Apply feed interaction.
     */
    fun feed(): PetState {
        return copy(
            hunger = (hunger + FEED_HUNGER_INCREASE).clamp(),
            happiness = (happiness + FEED_HAPPINESS_INCREASE).clamp(),
            totalInteractions = totalInteractions + 1,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Apply play interaction.
     */
    fun play(): PetState {
        return copy(
            happiness = (happiness + PLAY_HAPPINESS_INCREASE).clamp(),
            energy = (energy - PLAY_ENERGY_DECREASE).clamp(),
            totalInteractions = totalInteractions + 1,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Apply pet interaction.
     */
    fun pet(): PetState {
        return copy(
            happiness = (happiness + PET_HAPPINESS_INCREASE).clamp(),
            totalInteractions = totalInteractions + 1,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Apply poke interaction.
     */
    fun poke(): PetState {
        return copy(
            happiness = (happiness - POKE_HAPPINESS_DECREASE).clamp(),
            totalInteractions = totalInteractions + 1,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Apply rest (restore energy).
     */
    fun rest(): PetState {
        return copy(
            energy = (energy + REST_ENERGY_INCREASE).clamp(),
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Apply clean (restore hygiene).
     */
    fun clean(): PetState {
        return copy(
            hygiene = (hygiene + CLEAN_HYGIENE_INCREASE).clamp(),
            happiness = (happiness + CLEAN_HAPPINESS_INCREASE).clamp(),
            totalInteractions = totalInteractions + 1,
            lastUpdated = System.currentTimeMillis()
        )
    }

    /**
     * Equip cosmetic items.
     */
    fun equipCosmetics(hat: String? = null, skin: String? = null): PetState {
        return copy(
            equippedHat = hat ?: equippedHat,
            equippedSkin = skin ?: equippedSkin,
            lastUpdated = System.currentTimeMillis()
        )
    }
}
