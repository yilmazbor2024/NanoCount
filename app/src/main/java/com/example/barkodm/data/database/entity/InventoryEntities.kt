package com.example.barkodm.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.barkodm.data.model.*

@Entity(
    tableName = "inventory_headers",
    foreignKeys = [
        ForeignKey(
            entity = BranchEntity::class,
            parentColumns = ["id"],
            childColumns = ["branchId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = WarehouseEntity::class,
            parentColumns = ["id"],
            childColumns = ["warehouseId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = ShelfEntity::class,
            parentColumns = ["id"],
            childColumns = ["shelfId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("branchId"),
        Index("warehouseId"),
        Index("locationId"),
        Index("shelfId"),
        Index("userId")
    ]
)
data class InventoryHeaderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val date: Long,
    val type: String, // InventoryType string değeri
    val branchId: Int,
    val warehouseId: Int,
    val locationId: Int,
    val shelfId: Int,
    val readMode: String, // BarcodeReadMode string değeri
    val readerType: String, // BarcodeReaderType string değeri
    val userId: Int,
    val status: String,
    val createdAt: Long
) {
    fun toInventoryHeader(): InventoryHeader = InventoryHeader(
        id = id,
        date = date,
        type = InventoryType.valueOf(type),
        branchId = branchId,
        warehouseId = warehouseId,
        locationId = locationId,
        shelfId = shelfId,
        readMode = BarcodeReadMode.valueOf(readMode),
        readerType = BarcodeReaderType.valueOf(readerType),
        userId = userId,
        status = status,
        createdAt = createdAt
    )

    companion object {
        fun fromInventoryHeader(header: InventoryHeader): InventoryHeaderEntity = InventoryHeaderEntity(
            id = header.id,
            date = header.date,
            type = header.type.name,
            branchId = header.branchId,
            warehouseId = header.warehouseId,
            locationId = header.locationId,
            shelfId = header.shelfId,
            readMode = header.readMode.name,
            readerType = header.readerType.name,
            userId = header.userId,
            status = header.status,
            createdAt = header.createdAt
        )
    }
}

@Entity(
    tableName = "inventory_details",
    foreignKeys = [
        ForeignKey(
            entity = InventoryHeaderEntity::class,
            parentColumns = ["id"],
            childColumns = ["headerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("headerId"), Index("barcode")]
)
data class InventoryDetailEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val headerId: Int,
    val barcode: String,
    val productCode: String,
    val description: String,
    val unit: String,
    val quantity: Double,
    val scannedAt: Long
) {
    fun toInventoryDetail(): InventoryDetail = InventoryDetail(
        id = id,
        headerId = headerId,
        barcode = barcode,
        productCode = productCode,
        description = description,
        unit = unit,
        quantity = quantity,
        scannedAt = scannedAt
    )

    companion object {
        fun fromInventoryDetail(detail: InventoryDetail): InventoryDetailEntity = InventoryDetailEntity(
            id = detail.id,
            headerId = detail.headerId,
            barcode = detail.barcode,
            productCode = detail.productCode,
            description = detail.description,
            unit = detail.unit,
            quantity = detail.quantity,
            scannedAt = detail.scannedAt
        )
    }
} 