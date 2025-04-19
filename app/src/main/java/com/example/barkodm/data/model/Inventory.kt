package com.example.barkodm.data.model

enum class InventoryType {
    CONTROLLED, // Kontrollü
    UNCONTROLLED // Kontrolsüz
}

enum class BarcodeReadMode {
    CONTINUOUS, // Devamlı
    QUANTITY_CORRECTION // Miktar düzeltmeli
}

enum class BarcodeReaderType {
    CAMERA, // Kamera
    SCANNER // Barkod okuyucu
}

data class InventoryHeader(
    val id: Int,
    val date: Long,
    val type: InventoryType,
    val branchId: Int,
    val warehouseId: Int,
    val locationId: Int,
    val shelfId: Int,
    val readMode: BarcodeReadMode,
    val readerType: BarcodeReaderType,
    val userId: Int,
    val status: String = "OPEN", // OPEN, COMPLETED, CANCELLED
    val createdAt: Long = System.currentTimeMillis()
)

data class InventoryDetail(
    val id: Int,
    val headerId: Int,
    val barcode: String,
    val productCode: String,
    val description: String,
    val unit: String,
    val quantity: Double,
    val scannedAt: Long = System.currentTimeMillis()
) 