package com.example.barkodm.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.barkodm.data.database.entity.InventoryDetailEntity

@Dao
interface InventoryDetailDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detail: InventoryDetailEntity): Long
    
    @Query("SELECT * FROM inventory_details WHERE headerId = :headerId ORDER BY scannedAt DESC")
    fun getDetailsByHeaderId(headerId: Long): LiveData<List<InventoryDetailEntity>>
    
    @Query("SELECT SUM(quantity) FROM inventory_details WHERE headerId = :headerId")
    fun getTotalQuantityByHeaderId(headerId: Long): LiveData<Double?>
    
    @Query("SELECT COUNT(*) FROM inventory_details WHERE headerId = :headerId")
    fun getProductCountByHeaderId(headerId: Long): LiveData<Int?>
    
    @Query("SELECT COUNT(DISTINCT barcode) FROM inventory_details WHERE headerId = :headerId")
    suspend fun getUniqueProductCountInInventory(headerId: Long): Int
    
    @Query("DELETE FROM inventory_details WHERE headerId = :headerId")
    suspend fun deleteAllByHeaderId(headerId: Long)
    
    @Query("SELECT EXISTS(SELECT 1 FROM inventory_details WHERE headerId = :headerId AND barcode = :barcode LIMIT 1)")
    suspend fun isProductScanned(headerId: Long, barcode: String): Boolean
    
    @Query("SELECT * FROM inventory_details WHERE headerId = :headerId AND barcode = :barcode LIMIT 1")
    suspend fun getDetailByHeaderIdAndBarcode(headerId: Long, barcode: String): InventoryDetailEntity?
} 