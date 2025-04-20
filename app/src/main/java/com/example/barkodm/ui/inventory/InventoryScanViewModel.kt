package com.example.barkodm.ui.inventory

import android.util.Log
import androidx.lifecycle.*
import com.example.barkodm.data.database.dao.InventoryDetailDao
import com.example.barkodm.data.database.dao.InventoryHeaderDao
import com.example.barkodm.data.database.dao.ProductDao
import com.example.barkodm.data.database.entity.InventoryDetailEntity
import com.example.barkodm.data.database.entity.ProductEntity
import kotlinx.coroutines.launch
import java.util.*

class InventoryScanViewModel(
    private val productDao: ProductDao,
    private val inventoryHeaderDao: InventoryHeaderDao,
    private val inventoryDetailDao: InventoryDetailDao
) : ViewModel() {

    // Sayım ID'si
    private val _inventoryId = MutableLiveData<Long>()
    
    // Sayım detayları
    val inventoryDetails: LiveData<List<InventoryDetailEntity>> = _inventoryId.switchMap { id ->
        inventoryDetailDao.getDetailsByHeaderId(id)
    }
    
    // Bulunan ürün
    private val _productFound = MutableLiveData<ProductEntity?>()
    val productFound: LiveData<ProductEntity?> = _productFound
    
    // Hata mesajı
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    // Tarama başarısı
    private val _scanSuccess = MutableLiveData<Boolean>(false)
    val scanSuccess: LiveData<Boolean> = _scanSuccess
    
    // Sayım ID'sini ayarla
    fun setInventoryId(id: Long) {
        Log.d("InventoryScanViewModel", "Setting inventory ID: $id")
        _inventoryId.value = id
    }
    
    // Barkoda göre ürün ara
    fun lookupProductByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                Log.d("InventoryScanViewModel", "Looking up product with barcode: $barcode")
                
                val headerId = _inventoryId.value
                if (headerId == null) {
                    _errorMessage.value = "Sayım ID bulunamadı"
                    return@launch
                }
                
                val isAlreadyScanned = inventoryDetailDao.isProductScanned(headerId, barcode)
                if (isAlreadyScanned) {
                    _errorMessage.value = "Bu ürün zaten tarandı"
                    _productFound.value = null
                    return@launch
                }

                val product = productDao.getProductByBarcode(barcode)
                if (product != null) {
                    Log.d("InventoryScanViewModel", "Product found: ${product.code} - ${product.description}")
                    _productFound.value = product
                    _errorMessage.value = null
                } else {
                    Log.d("InventoryScanViewModel", "Product not found for barcode: $barcode")
                    _productFound.value = null
                    _errorMessage.value = "Ürün bulunamadı"
                }
            } catch (e: Exception) {
                Log.e("InventoryScanViewModel", "Error looking up product", e)
                _productFound.value = null
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            }
        }
    }
    
    // Sayım detayı ekle
    fun addInventoryDetail(barcode: String, quantity: Double) {
        viewModelScope.launch {
            try {
                val product = _productFound.value
                if (product == null) {
                    _errorMessage.value = "Ürün bilgisi bulunamadı"
                    return@launch
                }
                
                val inventoryId = _inventoryId.value
                if (inventoryId == null) {
                    _errorMessage.value = "Sayım ID bulunamadı"
                    return@launch
                }

                Log.d("InventoryScanViewModel", "Adding inventory detail - Barcode: $barcode, Inventory ID: $inventoryId")

                val inventoryDetail = InventoryDetailEntity(
                    headerId = inventoryId.toInt(),
                    barcode = barcode,
                    productCode = product.code,
                    description = product.description,
                    unit = product.unit,
                    quantity = quantity,
                    scannedAt = System.currentTimeMillis()
                )

                val id = inventoryDetailDao.insert(inventoryDetail)
                Log.d("InventoryScanViewModel", "Inventory detail inserted with ID: $id")
                
                _productFound.value = null
                _scanSuccess.value = true
            } catch (e: Exception) {
                Log.e("InventoryScanViewModel", "Error adding inventory detail", e)
                _errorMessage.value = "Kayıt hatası: ${e.localizedMessage}"
                _scanSuccess.value = false
            }
        }
    }
    
    // Manuel olarak ürün kodu ile ürün ara
    fun lookupProductByCode(code: String) {
        viewModelScope.launch {
            try {
                Log.d("InventoryScanViewModel", "Looking up product with code: $code")
                
                val product = productDao.getProductByCode(code)
                if (product != null) {
                    Log.d("InventoryScanViewModel", "Product found: ${product.code} - ${product.description}")
                    _productFound.value = product
                    _errorMessage.value = null
                } else {
                    Log.d("InventoryScanViewModel", "Product not found for code: $code")
                    _productFound.value = null
                    _errorMessage.value = "Ürün bulunamadı"
                }
            } catch (e: Exception) {
                Log.e("InventoryScanViewModel", "Error looking up product by code", e)
                _productFound.value = null
                _errorMessage.value = "Hata: ${e.localizedMessage}"
            }
        }
    }
    
    // Hata mesajını temizle
    fun resetErrorMessage() {
        _errorMessage.value = null
    }

    fun resetScanSuccess() {
        _scanSuccess.value = false
    }
}

/**
 * ViewModel factory
 */
class InventoryScanViewModelFactory(
    private val productDao: ProductDao,
    private val inventoryHeaderDao: InventoryHeaderDao,
    private val inventoryDetailDao: InventoryDetailDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(InventoryScanViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return InventoryScanViewModel(productDao, inventoryHeaderDao, inventoryDetailDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 