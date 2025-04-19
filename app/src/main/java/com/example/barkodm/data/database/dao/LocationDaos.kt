package com.example.barkodm.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.barkodm.data.database.entity.BranchEntity
import com.example.barkodm.data.database.entity.LocationEntity
import com.example.barkodm.data.database.entity.ShelfEntity
import com.example.barkodm.data.database.entity.WarehouseEntity

@Dao
interface BranchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(branchEntity: BranchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(branches: List<BranchEntity>): List<Long>

    @Query("SELECT * FROM branches ORDER BY name ASC")
    fun getAllBranches(): LiveData<List<BranchEntity>>

    @Query("SELECT * FROM branches WHERE id = :id")
    suspend fun getBranchById(id: Int): BranchEntity?
}

@Dao
interface WarehouseDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(warehouseEntity: WarehouseEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(warehouses: List<WarehouseEntity>): List<Long>

    @Query("SELECT * FROM warehouses WHERE branchId = :branchId ORDER BY name ASC")
    fun getWarehousesByBranch(branchId: Int): LiveData<List<WarehouseEntity>>

    @Query("SELECT * FROM warehouses WHERE id = :id")
    suspend fun getWarehouseById(id: Int): WarehouseEntity?
}

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(locationEntity: LocationEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(locations: List<LocationEntity>): List<Long>

    @Query("SELECT * FROM locations WHERE warehouseId = :warehouseId ORDER BY name ASC")
    fun getLocationsByWarehouse(warehouseId: Int): LiveData<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Int): LocationEntity?
}

@Dao
interface ShelfDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(shelfEntity: ShelfEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shelves: List<ShelfEntity>): List<Long>

    @Query("SELECT * FROM shelves WHERE locationId = :locationId ORDER BY name ASC")
    fun getShelvesByLocation(locationId: Int): LiveData<List<ShelfEntity>>

    @Query("SELECT * FROM shelves WHERE id = :id")
    suspend fun getShelfById(id: Int): ShelfEntity?
} 