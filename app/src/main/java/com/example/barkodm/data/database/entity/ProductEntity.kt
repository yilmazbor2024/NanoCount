package com.example.barkodm.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.barkodm.data.model.Product

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val barcode: String,
    val productCode: String,
    val description: String,
    val unit: String,
    val category: String?,
    val price: Double?,
    val stock: Double?,
    val createdAt: Long
) {
    fun toProduct(): Product = Product(
        id = id,
        barcode = barcode,
        productCode = productCode,
        description = description,
        unit = unit,
        category = category,
        price = price,
        stock = stock,
        createdAt = createdAt
    )

    companion object {
        fun fromProduct(product: Product): ProductEntity = ProductEntity(
            id = product.id,
            barcode = product.barcode,
            productCode = product.productCode,
            description = product.description,
            unit = product.unit,
            category = product.category,
            price = product.price,
            stock = product.stock,
            createdAt = product.createdAt
        )
    }
} 