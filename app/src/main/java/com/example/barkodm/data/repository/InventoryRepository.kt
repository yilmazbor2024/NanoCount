package com.example.barkodm.data.repository

import androidx.lifecycle.LiveData
import com.example.barkodm.data.database.dao.InventoryDetailDao
import com.example.barkodm.data.database.dao.InventoryHeaderDao
import com.example.barkodm.data.database.entity.InventoryDetailEntity
import com.example.barkodm.data.database.entity.InventoryHeaderEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InventoryRepository @Inject constructor(
    private val inventoryHeaderDao: InventoryHeaderDao,
    private val inventoryDetailDao: InventoryDetailDao
) {
    
    // Envanter başlık işlemleri
    suspend fun createInventory(entity: InventoryHeaderEntity): Long {
        return inventoryHeaderDao.insert(entity)
    }
    
    fun getAllInventories(): LiveData<List<InventoryHeaderEntity>> {
        return inventoryHeaderDao.getAllInventories()
    }
    
    fun getRecentInventories(limit: Int): LiveData<List<InventoryHeaderEntity>> {
        return inventoryHeaderDao.getRecentInventories(limit)
    }
    
    fun getInventoryById(inventoryId: Long): LiveData<InventoryHeaderEntity> {
        return inventoryHeaderDao.getInventoryById(inventoryId)
    }
    
    suspend fun getInventoryCount(): Int {
        return inventoryHeaderDao.getInventoryCount()
    }
    
    suspend fun getOpenInventoryCount(): Int {
        return inventoryHeaderDao.getOpenInventoryCount()
    }
    
    suspend fun completeInventory(inventoryId: Long) {
        inventoryHeaderDao.updateStatus(inventoryId, "COMPLETED")
    }
    
    suspend fun cancelInventory(inventoryId: Long) {
        inventoryHeaderDao.updateStatus(inventoryId, "CANCELLED")
    }
    
    // Envanter detay işlemleri
    suspend fun addInventoryDetail(entity: InventoryDetailEntity): Long {
        return inventoryDetailDao.insert(entity)
    }
    
    fun getInventoryDetails(inventoryId: Long): LiveData<List<InventoryDetailEntity>> {
        return inventoryDetailDao.getDetailsByHeaderId(inventoryId)
    }
    
    fun getProductCount(inventoryId: Long): Int {
        return inventoryDetailDao.getProductCountByHeaderId(inventoryId).value ?: 0
    }
    
    suspend fun getUniqueProductCount(inventoryId: Long): Int {
        return inventoryDetailDao.getUniqueProductCountInInventory(inventoryId)
    }
    
    fun getTotalQuantity(inventoryId: Long): Double {
        return inventoryDetailDao.getTotalQuantityByHeaderId(inventoryId).value ?: 0.0
    }
    
    suspend fun isProductScannedInInventory(inventoryId: Long, barcode: String): Boolean {
        return inventoryDetailDao.isProductScanned(inventoryId, barcode)
    }
} 