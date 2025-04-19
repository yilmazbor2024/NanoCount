package com.example.barkodm.ui.inventory

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barkodm.data.database.dao.InventoryHeaderDao
import com.example.barkodm.data.database.entity.InventoryHeaderEntity
import kotlinx.coroutines.launch

class InventoryViewModel(private val inventoryHeaderDao: InventoryHeaderDao) : ViewModel() {

    // Tüm sayımların listesi
    val inventoryHeaders: LiveData<List<InventoryHeaderEntity>> = inventoryHeaderDao.getAllInventories()
    
    // Sayım durumunu güncelle (Tamamlandı, İptal edildi)
    fun updateInventoryStatus(inventoryId: Int, status: String) {
        viewModelScope.launch {
            inventoryHeaderDao.updateStatus(inventoryId, status)
        }
    }
}

class InventoryViewModelFactory(private val inventoryHeaderDao: InventoryHeaderDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryViewModel(inventoryHeaderDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 