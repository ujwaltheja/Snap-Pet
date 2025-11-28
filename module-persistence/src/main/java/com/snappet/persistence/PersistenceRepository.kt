package com.snappet.persistence

import android.content.Context
import com.snappet.persistence.entities.ItemEntity
import com.snappet.persistence.entities.PetEntity
import com.snappet.persistence.entities.UserEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repository for persistence operations.
 * Abstracts Room database access.
 */
class PersistenceRepository(context: Context) {
    private val database = SnapPetDatabase.getInstance(context)
    private val petDao = database.petDao()
    private val userDao = database.userDao()
    private val itemDao = database.itemDao()

    // Pet operations
    suspend fun savePet(pet: PetEntity) = petDao.insertPet(pet)
    suspend fun getPet(petId: String): PetEntity? = petDao.getPet(petId)
    fun observePet(petId: String): Flow<PetEntity?> = petDao.observePet(petId)
    suspend fun getAllPets(): List<PetEntity> = petDao.getAllPets()
    suspend fun updatePet(pet: PetEntity) = petDao.updatePet(pet)

    // User operations
    suspend fun saveUser(user: UserEntity) = userDao.insertUser(user)
    suspend fun getUser(): UserEntity? = userDao.getUser()
    fun observeUser(): Flow<UserEntity?> = userDao.observeUser()
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    // Item operations
    suspend fun saveItem(item: ItemEntity) = itemDao.insertItem(item)
    suspend fun saveItems(items: List<ItemEntity>) = itemDao.insertAll(items)
    suspend fun getAllItems(): List<ItemEntity> = itemDao.getAllItems()
    fun observeAllItems(): Flow<List<ItemEntity>> = itemDao.observeAllItems()
    suspend fun getOwnedItems(): List<ItemEntity> = itemDao.getOwnedItems()
    suspend fun getItem(itemId: String): ItemEntity? = itemDao.getItem(itemId)
    suspend fun updateItem(item: ItemEntity) = itemDao.updateItem(item)

    /**
     * Unlock a pet by deducting coins from user and adding pet to unlocked list.
     * Returns true if successful, false if insufficient funds or already unlocked.
     */
    suspend fun unlockPet(petId: String, cost: Int): Boolean {
        val user = getUser() ?: return false

        // Check if already unlocked
        val unlockedPets = user.unlockedPetIds.split(",").map { it.trim() }
        if (unlockedPets.contains(petId)) return false

        // Check if enough coins
        if (user.coins < cost) return false

        // Deduct coins and add to unlocked list
        val newUnlockedPets = (unlockedPets + petId).joinToString(",")
        val updatedUser = user.copy(
            coins = user.coins - cost,
            unlockedPetIds = newUnlockedPets
        )
        updateUser(updatedUser)

        // Create default pet state for newly unlocked pet
        savePet(
            PetEntity(
                petId = petId,
                hunger = 100f,
                happiness = 100f,
                energy = 100f,
                lastUpdated = System.currentTimeMillis(),
                equippedHat = null,
                equippedSkin = null,
                totalInteractions = 0
            )
        )

        return true
    }

    /**
     * Initialize default data if database is empty.
     */
    suspend fun initializeDefaults() {
        val user = getUser()
        if (user == null) {
            // Create default user
            saveUser(
                UserEntity(
                    id = 1,
                    coins = 100, // Starting coins
                    selectedPetId = "cat", // Default pet
                    unlockedPetIds = "cat" // Only cat unlocked initially
                )
            )

            // Create default pet state for cat
            savePet(
                PetEntity(
                    petId = "cat",
                    hunger = 100f,
                    happiness = 100f,
                    energy = 100f,
                    lastUpdated = System.currentTimeMillis(),
                    equippedHat = null,
                    equippedSkin = null,
                    totalInteractions = 0
                )
            )

            // Initialize default shop items
            val defaultItems = listOf(
                ItemEntity("hat_party", "Party Hat", "hat", 50, "items/hat_party.png", false, false),
                ItemEntity("hat_cowboy", "Cowboy Hat", "hat", 100, "items/hat_cowboy.png", false, false),
                ItemEntity("hat_wizard", "Wizard Hat", "hat", 150, "items/hat_wizard.png", false, false),
                ItemEntity("skin_golden", "Golden Skin", "skin", 200, "items/skin_golden.png", false, false),
                ItemEntity("skin_rainbow", "Rainbow Skin", "skin", 300, "items/skin_rainbow.png", false, false)
            )
            saveItems(defaultItems)
        }
    }
}
