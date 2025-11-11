package com.snappet.store

import com.snappet.persistence.PersistenceRepository
import com.snappet.persistence.entities.ItemEntity
import com.snappet.utils.Logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing the local shop and inventory.
 */
class StoreRepository(
    private val persistenceRepository: PersistenceRepository,
    private val logger: Logger
) {
    companion object {
        private const val TAG = "StoreRepository"
    }

    /**
     * Get all shop items.
     */
    suspend fun getAllItems(): List<ItemEntity> {
        return persistenceRepository.getAllItems()
    }

    /**
     * Observe all shop items.
     */
    fun observeAllItems(): Flow<List<ItemEntity>> {
        return persistenceRepository.observeAllItems()
    }

    /**
     * Get owned items.
     */
    suspend fun getOwnedItems(): List<ItemEntity> {
        return persistenceRepository.getOwnedItems()
    }

    /**
     * Observe owned items.
     */
    fun observeOwnedItems(): Flow<List<ItemEntity>> {
        return observeAllItems().map { items ->
            items.filter { it.owned }
        }
    }

    /**
     * Purchase an item if the user has enough coins.
     * Returns true if successful, false otherwise.
     */
    suspend fun purchaseItem(itemId: String): PurchaseResult {
        val item = persistenceRepository.getItem(itemId)
        if (item == null) {
            logger.error(TAG, "Item not found: $itemId")
            return PurchaseResult.ItemNotFound
        }

        if (item.owned) {
            logger.warn(TAG, "Item already owned: $itemId")
            return PurchaseResult.AlreadyOwned
        }

        val user = persistenceRepository.getUser()
        if (user == null) {
            logger.error(TAG, "User not found")
            return PurchaseResult.UserNotFound
        }

        if (user.coins < item.cost) {
            logger.warn(TAG, "Insufficient funds to purchase item: $itemId")
            return PurchaseResult.InsufficientFunds
        }

        // Deduct coins and mark item as owned
        val updatedUser = user.copy(coins = user.coins - item.cost)
        val updatedItem = item.copy(owned = true)

        persistenceRepository.updateUser(updatedUser)
        persistenceRepository.updateItem(updatedItem)

        logger.info(TAG, "Purchased item: $itemId for ${item.cost} coins")
        return PurchaseResult.Success
    }

    /**
     * Equip an item (only if owned).
     */
    suspend fun equipItem(itemId: String): Boolean {
        val item = persistenceRepository.getItem(itemId) ?: return false
        if (!item.owned) return false

        // Unequip other items of the same type
        val allItems = persistenceRepository.getAllItems()
        allItems.filter { it.type == item.type && it.equipped && it.itemId != itemId }
            .forEach {
                persistenceRepository.updateItem(it.copy(equipped = false))
            }

        // Equip this item
        persistenceRepository.updateItem(item.copy(equipped = true))
        logger.info(TAG, "Equipped item: $itemId")
        return true
    }

    /**
     * Unequip an item.
     */
    suspend fun unequipItem(itemId: String): Boolean {
        val item = persistenceRepository.getItem(itemId) ?: return false
        if (!item.equipped) return false

        persistenceRepository.updateItem(item.copy(equipped = false))
        logger.info(TAG, "Unequipped item: $itemId")
        return true
    }

    sealed class PurchaseResult {
        object Success : PurchaseResult()
        object InsufficientFunds : PurchaseResult()
        object AlreadyOwned : PurchaseResult()
        object ItemNotFound : PurchaseResult()
        object UserNotFound : PurchaseResult()
    }
}
