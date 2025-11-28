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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

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

    private val _levelUpEvent = MutableSharedFlow<Int>()
    val levelUpEvent: SharedFlow<Int> = _levelUpEvent.asSharedFlow()

    private val _dailyRewardEvent = MutableSharedFlow<DailyReward>()
    val dailyRewardEvent: SharedFlow<DailyReward> = _dailyRewardEvent.asSharedFlow()

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

        coroutineScope.launch {
            checkDailyLogin()
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
                totalInteractions = petEntity.totalInteractions,
                xp = petEntity.xp,
                level = petEntity.level
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

        // Add XP for interaction (e.g., 10 XP per interaction)
        val stateWithXp = newState.addXp(10)

        if (stateWithXp.level > newState.level) {
            // Level Up!
            coroutineScope.launch {
                _levelUpEvent.emit(stateWithXp.level)
                awardCoins(100 * stateWithXp.level) // Bonus coins for leveling up
            }
        }

        _currentPetState.value = stateWithXp
        savePetState(stateWithXp)

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
            totalInteractions = state.totalInteractions,
            xp = state.xp,
            level = state.level
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
    /**
     * Check for daily login reward.
     */
    private suspend fun checkDailyLogin() {
        val user = repository.getUser() ?: return
        val currentTime = System.currentTimeMillis()
        val lastLoginTime = user.lastLoginTime

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = currentTime
        val currentDay = calendar.get(Calendar.DAY_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)

        calendar.timeInMillis = lastLoginTime
        val lastDay = calendar.get(Calendar.DAY_OF_YEAR)
        val lastYear = calendar.get(Calendar.YEAR)

        if (lastLoginTime == 0L) {
            // First login ever
            updateLoginStreak(user, 1, currentTime)
            emitDailyReward(1)
        } else if (currentYear > lastYear || (currentYear == lastYear && currentDay > lastDay)) {
            // New day
            if (currentYear == lastYear && currentDay == lastDay + 1) {
                // Consecutive day
                val newStreak = user.loginStreak + 1
                updateLoginStreak(user, newStreak, currentTime)
                emitDailyReward(newStreak)
            } else {
                // Streak broken (unless it's just the next day across year boundary, ignoring that edge case for MVP)
                // Actually, let's just reset if difference > 1 day
                // Simple check: if more than 24h + buffer passed? No, calendar day is better.
                // If it's not the immediate next day, reset.
                // For MVP, if not (currentDay == lastDay + 1), reset.
                // Handling year boundary properly is annoying, let's assume same year for MVP or reset.
                
                // If we are here, it IS a new day. Check if it's consecutive.
                val isConsecutive = (currentYear == lastYear && currentDay == lastDay + 1) ||
                        (currentYear == lastYear + 1 && currentDay == 1 && lastDay >= 365) // Rough check

                val newStreak = if (isConsecutive) user.loginStreak + 1 else 1
                updateLoginStreak(user, newStreak, currentTime)
                emitDailyReward(newStreak)
            }
        }
    }

    private suspend fun updateLoginStreak(user: com.snappet.persistence.entities.UserEntity, streak: Int, time: Long) {
        val updatedUser = user.copy(
            loginStreak = streak,
            lastLoginTime = time
        )
        repository.updateUser(updatedUser)
    }

    private suspend fun emitDailyReward(streak: Int) {
        val coins = 100 + (streak * 10).coerceAtMost(500)
        awardCoins(coins)
        _dailyRewardEvent.emit(DailyReward(streak, coins))
    }
}

data class DailyReward(
    val streak: Int,
    val coins: Int
)
