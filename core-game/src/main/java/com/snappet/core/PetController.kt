package com.snappet.core

import com.snappet.core.models.Interaction
import com.snappet.core.models.Pet
import com.snappet.core.models.PetState
import com.snappet.persistence.PersistenceRepository
import com.snappet.persistence.entities.PetEntity
import com.snappet.utils.Logger
import com.snappet.utils.TimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Controller for managing pet state and game loop.
 */
class PetController(
    private val repository: PersistenceRepository,
    private val coroutineScope: CoroutineScope,
    private val logger: Logger
) {
    private val _currentPetState = MutableStateFlow<PetState?>(null)
    val currentPetState: StateFlow<PetState?> = _currentPetState.asStateFlow()

    private val _currentPet = MutableStateFlow<Pet?>(null)
    val currentPet: StateFlow<Pet?> = _currentPet.asStateFlow()

    private var updateJob: Job? = null
    private var isRunning = false

    companion object {
        private const val UPDATE_INTERVAL_MS = 1000L // Update every second
        private const val TAG = "PetController"
    }

    /**
     * Start the game loop.
     */
    fun start() {
        if (isRunning) return
        isRunning = true

        updateJob = coroutineScope.launch {
            while (isRunning) {
                updatePetState()
                delay(UPDATE_INTERVAL_MS)
            }
        }

        logger.info(TAG, "Game loop started")
    }

    /**
     * Stop the game loop.
     */
    fun stop() {
        isRunning = false
        updateJob?.cancel()
        updateJob = null
        logger.info(TAG, "Game loop stopped")
    }

    /**
     * Load a pet by ID.
     */
    suspend fun loadPet(petId: String) {
        val pet = Pet.DEFAULT_PETS.find { it.id == petId }
        if (pet == null) {
            logger.error(TAG, "Pet not found: $petId")
            return
        }

        _currentPet.value = pet

        // Load or create pet state
        val petEntity = repository.getPet(petId)
        val state = if (petEntity != null) {
            // Pet exists, recalculate state based on elapsed time
            val elapsedSeconds = TimeUtils.elapsedSeconds(petEntity.lastUpdated)
            PetState(
                petId = petEntity.petId,
                hunger = petEntity.hunger,
                happiness = petEntity.happiness,
                energy = petEntity.energy,
                hygiene = petEntity.hygiene,
                lastUpdated = petEntity.lastUpdated,
                equippedHat = petEntity.equippedHat,
                equippedSkin = petEntity.equippedSkin,
                totalInteractions = petEntity.totalInteractions
            ).updateWithElapsedTime(elapsedSeconds)
        } else {
            // Create new pet state
            PetState(petId = petId)
        }

        _currentPetState.value = state
        savePetState(state)
        logger.info(TAG, "Loaded pet: $petId")
    }

    /**
     * Handle an interaction with the pet.
     */
    suspend fun handleInteraction(interaction: Interaction, coinsEarned: Int = 5) {
        val state = _currentPetState.value ?: return

        val newState = when (interaction) {
            Interaction.TAP -> state.pet()
            Interaction.PET -> state.pet()
            Interaction.POKE -> state.poke()
            Interaction.FEED -> state.feed()
            Interaction.PLAY -> state.play()
            Interaction.REST -> state.rest()
            Interaction.CLEAN -> state.clean()
        }

        _currentPetState.value = newState
        savePetState(newState)

        // Award coins for interaction
        awardCoins(coinsEarned)

        logger.info(TAG, "Interaction: $interaction, Coins earned: $coinsEarned")
    }

    /**
     * Equip cosmetics to the pet.
     */
    suspend fun equipCosmetics(hat: String? = null, skin: String? = null) {
        val state = _currentPetState.value ?: return
        val newState = state.equipCosmetics(hat, skin)
        _currentPetState.value = newState
        savePetState(newState)
        logger.info(TAG, "Equipped cosmetics - Hat: $hat, Skin: $skin")
    }

    /**
     * Update pet state based on elapsed time (called by game loop).
     */
    private suspend fun updatePetState() {
        val state = _currentPetState.value ?: return
        val elapsedSeconds = TimeUtils.elapsedSeconds(state.lastUpdated)

        if (elapsedSeconds > 0.5f) { // Only update if significant time has passed
            val updatedState = state.updateWithElapsedTime(elapsedSeconds)
            _currentPetState.value = updatedState
            savePetState(updatedState)
        }
    }

    /**
     * Save pet state to repository.
     */
    private suspend fun savePetState(state: PetState) {
        val entity = PetEntity(
            petId = state.petId,
            hunger = state.hunger,
            happiness = state.happiness,
            energy = state.energy,
            hygiene = state.hygiene,
            lastUpdated = state.lastUpdated,
            equippedHat = state.equippedHat,
            equippedSkin = state.equippedSkin,
            totalInteractions = state.totalInteractions
        )
        repository.savePet(entity)
    }

    /**
     * Award coins to the user.
     */
    private suspend fun awardCoins(amount: Int) {
        val user = repository.getUser() ?: return
        val updatedUser = user.copy(coins = user.coins + amount)
        repository.updateUser(updatedUser)
    }
}
