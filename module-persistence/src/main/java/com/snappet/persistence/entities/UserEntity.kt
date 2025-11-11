package com.snappet.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing user data (single row).
 */
@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey
    val id: Int = 1, // Always 1, single user
    val coins: Int,
    val selectedPetId: String,
    val unlockedPetIds: String // Comma-separated list
)
