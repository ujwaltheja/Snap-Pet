package com.snappet.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.snappet.persistence.dao.ItemDao
import com.snappet.persistence.dao.PetDao
import com.snappet.persistence.dao.UserDao
import com.snappet.persistence.entities.ItemEntity
import com.snappet.persistence.entities.PetEntity
import com.snappet.persistence.entities.UserEntity

/**
 * Room database for Snap Pet app.
 */
@Database(
    entities = [PetEntity::class, UserEntity::class, ItemEntity::class],
    version = 1,
    exportSchema = true
)
abstract class SnapPetDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun userDao(): UserDao
    abstract fun itemDao(): ItemDao

    companion object {
        private const val DATABASE_NAME = "snap_pet_database"

        @Volatile
        private var instance: SnapPetDatabase? = null

        fun getInstance(context: Context): SnapPetDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): SnapPetDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                SnapPetDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration() // For MVP, migrations not required
                .build()
        }
    }
}
