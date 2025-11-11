package com.snappet.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing cosmetic items.
 */
@Entity(tableName = "items")
data class ItemEntity(
    @PrimaryKey
    val itemId: String,
    val name: String,
    val type: String, // "hat" or "skin"
    val cost: Int,
    val imageAsset: String,
    val owned: Boolean,
    val equipped: Boolean
)
