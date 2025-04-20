package com.example.barkodm.ui.definitions

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.barkodm.data.database.dao.ProductDao
import com.example.barkodm.data.database.entity.ProductEntity

/**
 * Ürün listesi için ViewModel
 */
class ProductsViewModel(private val productDao: ProductDao) : ViewModel() {
    
    // All products from database
    val allProducts: LiveData<List<ProductEntity>> = productDao.getAllProducts()
}

/**
 * Factory for ProductsViewModel
 */
class ProductsViewModelFactory(private val productDao: ProductDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductsViewModel(productDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 