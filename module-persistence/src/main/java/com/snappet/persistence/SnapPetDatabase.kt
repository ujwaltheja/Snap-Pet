package com.snappet.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 3,
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

        /**
         * Migration from version 1 to 2.
         * Adds new columns or tables as needed.
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example: Add loginStreak and lastLoginTime to user table
                database.execSQL(
                    "ALTER TABLE user ADD COLUMN loginStreak INTEGER NOT NULL DEFAULT 0"
                )
                database.execSQL(
                    "ALTER TABLE user ADD COLUMN lastLoginTime INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        /**
         * Migration from version 2 to 3.
         * Adds item system tables.
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create items table if it doesn't exist
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS items (
                        itemId TEXT PRIMARY KEY NOT NULL,
                        name TEXT NOT NULL,
                        type TEXT NOT NULL,
                        cost INTEGER NOT NULL,
                        imageAsset TEXT NOT NULL,
                        owned INTEGER NOT NULL,
                        equipped INTEGER NOT NULL
                    )
                    """.trimIndent()
                )
            }
        }

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
                .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                .fallbackToDestructiveMigration() // Keep as last resort for unhandled migrations
                .build()
        }
    }
}
