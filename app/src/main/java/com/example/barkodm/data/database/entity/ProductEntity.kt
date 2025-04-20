package com.example.barkodm.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.barkodm.data.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val barcode: String,
    val code: String,
    val description: String,
    val unit: String,
    val stockQuantity: Double = 0.0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toProduct(): Product = Product(
        id = id,
        barcode = barcode,
        code = code,
        description = description,
        unit = unit,
        stockQuantity = stockQuantity,
        createdAt = createdAt,
        updatedAt = updatedAt
    )

    companion object {
        fun fromProduct(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            barcode = product.barcode,
            code = product.code,
            description = product.description,
            unit = product.unit,
            stockQuantity = product.stockQuantity,
            createdAt = product.createdAt,
            updatedAt = product.updatedAt
        )
    }
} 