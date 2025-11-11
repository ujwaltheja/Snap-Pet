package com.snappet.store

import com.snappet.persistence.PersistenceRepository
import com.snappet.persistence.entities.ItemEntity
import com.snappet.persistence.entities.UserEntity
import com.snappet.utils.Logger
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StoreRepositoryTest {

    private lateinit var persistenceRepository: PersistenceRepository
    private lateinit var logger: Logger
    private lateinit var storeRepository: StoreRepository

    @Before
    fun setup() {
        persistenceRepository = mock()
        logger = mock()
        storeRepository = StoreRepository(persistenceRepository, logger)
    }

    @Test
    fun `purchase item with sufficient funds succeeds`() = runTest {
        val item = ItemEntity("hat1", "Party Hat", "hat", 50, "asset", false, false)
        val user = UserEntity(1, 100, "cat", "cat")

        whenever(persistenceRepository.getItem("hat1")).thenReturn(item)
        whenever(persistenceRepository.getUser()).thenReturn(user)

        val result = storeRepository.purchaseItem("hat1")

        assertTrue(result is StoreRepository.PurchaseResult.Success)
        verify(persistenceRepository).updateUser(user.copy(coins = 50))
        verify(persistenceRepository).updateItem(item.copy(owned = true))
    }

    @Test
    fun `purchase item with insufficient funds fails`() = runTest {
        val item = ItemEntity("hat1", "Party Hat", "hat", 150, "asset", false, false)
        val user = UserEntity(1, 100, "cat", "cat")

        whenever(persistenceRepository.getItem("hat1")).thenReturn(item)
        whenever(persistenceRepository.getUser()).thenReturn(user)

        val result = storeRepository.purchaseItem("hat1")

        assertTrue(result is StoreRepository.PurchaseResult.InsufficientFunds)
        verify(persistenceRepository, never()).updateUser(any())
        verify(persistenceRepository, never()).updateItem(any())
    }

    @Test
    fun `purchase already owned item fails`() = runTest {
        val item = ItemEntity("hat1", "Party Hat", "hat", 50, "asset", true, false)
        val user = UserEntity(1, 100, "cat", "cat")

        whenever(persistenceRepository.getItem("hat1")).thenReturn(item)
        whenever(persistenceRepository.getUser()).thenReturn(user)

        val result = storeRepository.purchaseItem("hat1")

        assertTrue(result is StoreRepository.PurchaseResult.AlreadyOwned)
        verify(persistenceRepository, never()).updateUser(any())
        verify(persistenceRepository, never()).updateItem(any())
    }

    @Test
    fun `purchase non-existent item fails`() = runTest {
        whenever(persistenceRepository.getItem("nonexistent")).thenReturn(null)

        val result = storeRepository.purchaseItem("nonexistent")

        assertTrue(result is StoreRepository.PurchaseResult.ItemNotFound)
    }

    @Test
    fun `equip item unequips other items of same type`() = runTest {
        val item1 = ItemEntity("hat1", "Hat 1", "hat", 50, "asset", true, true)
        val item2 = ItemEntity("hat2", "Hat 2", "hat", 50, "asset", true, false)
        val allItems = listOf(item1, item2)

        whenever(persistenceRepository.getItem("hat2")).thenReturn(item2)
        whenever(persistenceRepository.getAllItems()).thenReturn(allItems)

        val result = storeRepository.equipItem("hat2")

        assertTrue(result)
        verify(persistenceRepository).updateItem(item1.copy(equipped = false))
        verify(persistenceRepository).updateItem(item2.copy(equipped = true))
    }
}
