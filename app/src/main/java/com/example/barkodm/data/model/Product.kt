package com.example.barkodm.data.model

/**
 * Ürün modeli
 */
data class Product(
    val id: Long = 0,
    val barcode: String,
    val code: String,
    val description: String,
    val unit: String,
    val stockQuantity: Double,
    val createdAt: Long,
    val updatedAt: Long
) 