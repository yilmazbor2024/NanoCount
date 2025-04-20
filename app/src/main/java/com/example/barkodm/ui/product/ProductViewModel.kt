package com.example.barkodm.ui.product

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barkodm.data.database.entity.ProductEntity
import com.example.barkodm.data.model.Product
import com.example.barkodm.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    // Tüm ürünlerin LiveData nesnesi direkt repository'den gelecek
    val products: LiveData<List<Product>> = productRepository.getAllProductsLiveData()
    
    // Ürün sayısı LiveData nesnesi direkt repository'den gelecek
    val productCount: LiveData<Int> = productRepository.getProductCountLiveData()

    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    init {
        // ViewModel oluşturulduğunda veritabanındaki verileri otomatik yükle
        loadProducts()
    }

    fun loadProducts() {
        // Artık products ve productCount zaten LiveData olarak repository'den geliyor
        // Sadece hata durumları için kontrol yapalım
        viewModelScope.launch {
            try {
                // LiveData'lar otomatik olarak güncellenecek
            } catch (e: Exception) {
                _errorMessage.postValue("Ürünler yüklenirken hata oluştu: ${e.message}")
            }
        }
    }

    fun getProductByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                val product = productRepository.getProductByBarcode(barcode)
                if (product != null) {
                    // Product found
                } else {
                    _errorMessage.postValue("Ürün bulunamadı: $barcode")
                }
            } catch (e: Exception) {
                _errorMessage.postValue("Ürün aranırken hata oluştu: ${e.message}")
            }
        }
    }

    fun saveProduct(product: Product) {
        viewModelScope.launch {
            try {
                Log.d("ProductViewModel", "Saving product: ${product.description}, Barcode: ${product.barcode}")
                
                val productId = productRepository.insertProduct(product)
                Log.d("ProductViewModel", "Product saved with ID: $productId")
                
                _saveStatus.postValue(true)
                // loadProducts() artık gerekli değil, LiveData otomatik güncellenecek
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error saving product", e)
                _errorMessage.postValue("Ürün kaydedilirken hata oluştu: ${e.message}")
                _saveStatus.postValue(false)
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
                // loadProducts() artık gerekli değil, LiveData otomatik güncellenecek
            } catch (e: Exception) {
                _errorMessage.postValue("Ürün silinirken hata oluştu: ${e.message}")
            }
        }
    }

    fun resetErrorMessage() {
        _errorMessage.value = null
    }
} 