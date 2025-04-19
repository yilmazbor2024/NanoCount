package com.example.barkodm.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.barkodm.data.database.entity.InventoryDetailEntity
import com.example.barkodm.data.database.entity.InventoryHeaderEntity

@Dao
interface InventoryHeaderDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(headerEntity: InventoryHeaderEntity): Long

    @Query("SELECT * FROM inventory_headers ORDER BY date DESC")
    fun getAllInventories(): LiveData<List<InventoryHeaderEntity>>

    @Query("SELECT * FROM inventory_headers WHERE id = :id")
    suspend fun getInventoryById(id: Int): InventoryHeaderEntity?

    @Query("SELECT * FROM inventory_headers WHERE id = :id")
    fun getInventoryWithIdLiveData(id: Int): LiveData<InventoryHeaderEntity>

    @Query("SELECT * FROM inventory_headers WHERE status = 'OPEN'")
    fun getOpenInventories(): LiveData<List<InventoryHeaderEntity>>

    @Query("SELECT COUNT(*) FROM inventory_headers WHERE status = 'OPEN'")
    fun getOpenInventoriesCount(): LiveData<Int>

    @Query("UPDATE inventory_headers SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)
}

@Dao
interface InventoryDetailDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(detailEntity: InventoryDetailEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(details: List<InventoryDetailEntity>): List<Long>

    @Query("SELECT * FROM inventory_details WHERE headerId = :headerId ORDER BY scannedAt DESC")
    fun getDetailsByHeaderId(headerId: Int): LiveData<List<InventoryDetailEntity>>

    @Query("SELECT * FROM inventory_details WHERE headerId = :headerId AND barcode = :barcode LIMIT 1")
    suspend fun getDetailByBarcodeAndHeaderId(headerId: Int, barcode: String): InventoryDetailEntity?

    @Transaction
    suspend fun addOrUpdateQuantity(detailEntity: InventoryDetailEntity) {
        val existingDetail = getDetailByBarcodeAndHeaderId(detailEntity.headerId, detailEntity.barcode)
        if (existingDetail != null) {
            val newQuantity = existingDetail.quantity + detailEntity.quantity
            updateQuantity(existingDetail.id, newQuantity)
        } else {
            insert(detailEntity)
        }
    }

    @Query("UPDATE inventory_details SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Int, quantity: Double)

    @Query("SELECT SUM(quantity) FROM inventory_details WHERE headerId = :headerId")
    fun getTotalQuantity(headerId: Int): LiveData<Double>

    @Query("SELECT COUNT(DISTINCT barcode) FROM inventory_details WHERE headerId = :headerId")
    fun getUniqueProductCount(headerId: Int): LiveData<Int>
} 