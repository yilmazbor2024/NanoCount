package com.example.barkodm.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.barkodm.data.database.dao.*
import com.example.barkodm.data.database.entity.*

@Database(
    entities = [
        UserEntity::class, 
        ProductEntity::class,
        BranchEntity::class,
        WarehouseEntity::class,
        LocationEntity::class,
        ShelfEntity::class,
        InventoryHeaderEntity::class,
        InventoryDetailEntity::class
    ],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun productDao(): ProductDao
    abstract fun branchDao(): BranchDao
    abstract fun warehouseDao(): WarehouseDao
    abstract fun locationDao(): LocationDao
    abstract fun shelfDao(): ShelfDao
    abstract fun inventoryHeaderDao(): InventoryHeaderDao
    abstract fun inventoryDetailDao(): InventoryDetailDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "inventory_database"
                )
                .fallbackToDestructiveMigration()
                .build()

                INSTANCE = instance
                instance
            }
        }
    }
} 