package com.example.barkodm.data.database.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.barkodm.data.database.entity.ProductEntity

@Dao
interface ProductDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(productEntity: ProductEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>): List<Long>

    @Query("SELECT * FROM products WHERE barcode = :barcode LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): ProductEntity?

    @Query("SELECT * FROM products WHERE code = :code LIMIT 1")
    suspend fun getProductByCode(code: String): ProductEntity?

    @Query("SELECT * FROM products ORDER BY description ASC")
    fun getAllProducts(): LiveData<List<ProductEntity>>

    @Query("SELECT COUNT(*) FROM products")
    fun getProductCount(): LiveData<Int>

    @Query("SELECT * FROM products WHERE code LIKE '%' || :searchQuery || '%' OR description LIKE '%' || :searchQuery || '%' OR barcode LIKE '%' || :searchQuery || '%'")
    fun searchProducts(searchQuery: String): LiveData<List<ProductEntity>>
} 