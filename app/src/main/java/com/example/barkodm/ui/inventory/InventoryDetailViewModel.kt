package com.example.barkodm.ui.inventory

import androidx.lifecycle.*
import com.example.barkodm.data.database.dao.InventoryDetailDao
import com.example.barkodm.data.database.dao.InventoryHeaderDao
import com.example.barkodm.data.database.entity.InventoryDetailEntity
import com.example.barkodm.data.database.entity.InventoryHeaderEntity
import kotlinx.coroutines.launch

class InventoryDetailViewModel(
    private val inventoryHeaderDao: InventoryHeaderDao,
    private val inventoryDetailDao: InventoryDetailDao
) : ViewModel() {

    private val _inventoryId = MutableLiveData<Int>()
    
    // ID'si belirtilen sayımın header bilgisi
    val inventoryHeader: LiveData<InventoryHeaderEntity> = _inventoryId.switchMap { id ->
        inventoryHeaderDao.getInventoryWithIdLiveData(id)
    }
    
    // ID'si belirtilen sayımın detayları
    val inventoryDetails: LiveData<List<InventoryDetailEntity>> = _inventoryId.switchMap { id ->
        inventoryDetailDao.getDetailsByHeaderId(id)
    }
    
    // Toplam miktar
    val totalQuantity: LiveData<Double> = _inventoryId.switchMap { id ->
        inventoryDetailDao.getTotalQuantity(id)
    }
    
    // Benzersiz ürün sayısı
    val uniqueProductCount: LiveData<Int> = _inventoryId.switchMap { id ->
        inventoryDetailDao.getUniqueProductCount(id)
    }
    
    // İşlem yapmak için envanter ID'sini ayarla
    fun setInventoryId(id: Int) {
        if (_inventoryId.value != id) {
            _inventoryId.value = id
        }
    }
    
    // Sayım durumunu güncelle (tamamlandı/iptal edildi)
    fun updateInventoryStatus(status: String) {
        _inventoryId.value?.let { id ->
            viewModelScope.launch {
                inventoryHeaderDao.updateStatus(id, status)
            }
        }
    }
}

/**
 * ViewModel üretici fabrikası
 */
class InventoryDetailViewModelFactory(
    private val inventoryHeaderDao: InventoryHeaderDao,
    private val inventoryDetailDao: InventoryDetailDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryDetailViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryDetailViewModel(inventoryHeaderDao, inventoryDetailDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 