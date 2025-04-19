package com.example.barkodm.data.model

data class Product(
    val id: Int,
    val barcode: String,
    val productCode: String,
    val description: String,
    val unit: String,
    val category: String? = null,
    val price: Double? = null,
    val stock: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
) 