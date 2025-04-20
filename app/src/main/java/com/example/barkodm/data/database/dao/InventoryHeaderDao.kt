package com.example.barkodm.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.barkodm.data.database.entity.InventoryHeaderEntity

@Dao
interface InventoryHeaderDao {
    
    @Insert
    suspend fun insert(inventory: InventoryHeaderEntity): Long
    
    @Update
    suspend fun update(inventory: InventoryHeaderEntity)
    
    @Query("SELECT * FROM inventory_headers ORDER BY createdAt DESC")
    fun getAllInventories(): LiveData<List<InventoryHeaderEntity>>
    
    @Query("SELECT * FROM inventory_headers ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentInventories(limit: Int): LiveData<List<InventoryHeaderEntity>>
    
    @Query("SELECT * FROM inventory_headers WHERE id = :inventoryId")
    fun getInventoryById(inventoryId: Long): LiveData<InventoryHeaderEntity>
    
    @Query("SELECT COUNT(*) FROM inventory_headers")
    suspend fun getInventoryCount(): Int
    
    @Query("SELECT COUNT(*) FROM inventory_headers WHERE status = 'OPEN'")
    suspend fun getOpenInventoryCount(): Int
    
    @Query("SELECT COUNT(*) FROM inventory_headers WHERE status = 'OPEN'")
    fun getOpenInventoriesCount(): LiveData<Int>
    
    @Query("UPDATE inventory_headers SET status = :status WHERE id = :inventoryId")
    suspend fun updateStatus(inventoryId: Long, status: String)
} 