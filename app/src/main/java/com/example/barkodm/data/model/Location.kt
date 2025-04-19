package com.example.barkodm.data.model

data class Branch(
    val id: Int,
    val code: String,
    val name: String
)

data class Warehouse(
    val id: Int,
    val code: String,
    val name: String,
    val branchId: Int
)

data class Location(
    val id: Int,
    val code: String,
    val name: String,
    val warehouseId: Int
)

data class Shelf(
    val id: Int,
    val code: String,
    val name: String,
    val locationId: Int
) 