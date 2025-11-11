package com.snappet.persistence.dao

import androidx.room.*
import com.snappet.persistence.entities.PetEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Pet operations.
 */
@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE petId = :petId")
    suspend fun getPet(petId: String): PetEntity?

    @Query("SELECT * FROM pets WHERE petId = :petId")
    fun observePet(petId: String): Flow<PetEntity?>

    @Query("SELECT * FROM pets")
    suspend fun getAllPets(): List<PetEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity)

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Delete
    suspend fun deletePet(pet: PetEntity)

    @Query("DELETE FROM pets")
    suspend fun deleteAll()
}
