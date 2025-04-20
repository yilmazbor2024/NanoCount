package com.example.barkodm.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.example.barkodm.data.database.dao.ProductDao
import com.example.barkodm.data.database.entity.ProductEntity
import com.example.barkodm.data.model.Product
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    // LiveData olarak tüm ürünleri getir
    fun getAllProductsLiveData(): LiveData<List<Product>> {
        return productDao.getAllProducts().map { entities ->
            entities.map { it.toProduct() }
        }
    }
    
    // Suspend function için direk liste döndür
    suspend fun getAllProducts(): List<Product> {
        // Bu fonksiyon artık kullanılmayacak, sadece geriye dönük uyumluluk için kalacak
        return emptyList()
    }
    
    suspend fun getProductById(id: Long): Product? {
        // Implement if needed
        return null
    }
    
    suspend fun getProductByBarcode(barcode: String): Product? {
        return productDao.getProductByBarcode(barcode)?.toProduct()
    }
    
    suspend fun insertProduct(product: Product): Long {
        val productEntity = ProductEntity.fromProduct(product)
        return productDao.insert(productEntity)
    }
    
    suspend fun updateProduct(product: Product) {
        val productEntity = ProductEntity.fromProduct(product)
        productDao.insert(productEntity)
    }
    
    suspend fun deleteProduct(product: Product) {
        // Delete not implemented in DAO
    }
    
    // Ürün sayısını LiveData olarak döndür
    fun getProductCountLiveData(): LiveData<Int> {
        return productDao.getProductCount()
    }
    
    // Eski fonksiyon, geriye dönük uyumluluk için
    suspend fun getProductCount(): Int {
        return 0
    }
} 