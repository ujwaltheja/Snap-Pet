package com.snappet.persistence.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Room entity for storing pet state.
 */
@Entity(tableName = "pets")
data class PetEntity(
    @PrimaryKey
    val petId: String,
    val hunger: Float,
    val happiness: Float,
    val energy: Float,
    val hygiene: Float = 100f,
    val lastUpdated: Long,
    val equippedHat: String?,
    val equippedSkin: String?,
    val totalInteractions: Int,
    val xp: Long = 0,
    val level: Int = 1
)
