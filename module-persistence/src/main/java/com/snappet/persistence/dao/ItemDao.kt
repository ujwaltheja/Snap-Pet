package com.snappet.persistence.dao

import androidx.room.*
import com.snappet.persistence.entities.ItemEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Item operations.
 */
@Dao
interface ItemDao {
    @Query("SELECT * FROM items")
    suspend fun getAllItems(): List<ItemEntity>

    @Query("SELECT * FROM items")
    fun observeAllItems(): Flow<List<ItemEntity>>

    @Query("SELECT * FROM items WHERE owned = 1")
    suspend fun getOwnedItems(): List<ItemEntity>

    @Query("SELECT * FROM items WHERE itemId = :itemId")
    suspend fun getItem(itemId: String): ItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<ItemEntity>)

    @Update
    suspend fun updateItem(item: ItemEntity)

    @Query("DELETE FROM items")
    suspend fun deleteAll()
}
