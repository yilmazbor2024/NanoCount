package com.example.barkodm.data.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.barkodm.data.model.Branch
import com.example.barkodm.data.model.Location
import com.example.barkodm.data.model.Shelf
import com.example.barkodm.data.model.Warehouse

@Entity(tableName = "branches")
data class BranchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: String,
    val name: String
) {
    fun toBranch(): Branch = Branch(
        id = id,
        code = code,
        name = name
    )

    companion object {
        fun fromBranch(branch: Branch): BranchEntity = BranchEntity(
            id = branch.id,
            code = branch.code,
            name = branch.name
        )
    }
}

@Entity(
    tableName = "warehouses",
    foreignKeys = [
        ForeignKey(
            entity = BranchEntity::class,
            parentColumns = ["id"],
            childColumns = ["branchId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("branchId")]
)
data class WarehouseEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: String,
    val name: String,
    val branchId: Int
) {
    fun toWarehouse(): Warehouse = Warehouse(
        id = id,
        code = code,
        name = name,
        branchId = branchId
    )

    companion object {
        fun fromWarehouse(warehouse: Warehouse): WarehouseEntity = WarehouseEntity(
            id = warehouse.id,
            code = warehouse.code,
            name = warehouse.name,
            branchId = warehouse.branchId
        )
    }
}

@Entity(
    tableName = "locations",
    foreignKeys = [
        ForeignKey(
            entity = WarehouseEntity::class,
            parentColumns = ["id"],
            childColumns = ["warehouseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("warehouseId")]
)
data class LocationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: String,
    val name: String,
    val warehouseId: Int
) {
    fun toLocation(): Location = Location(
        id = id,
        code = code,
        name = name,
        warehouseId = warehouseId
    )

    companion object {
        fun fromLocation(location: Location): LocationEntity = LocationEntity(
            id = location.id,
            code = location.code,
            name = location.name,
            warehouseId = location.warehouseId
        )
    }
}

@Entity(
    tableName = "shelves",
    foreignKeys = [
        ForeignKey(
            entity = LocationEntity::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("locationId")]
)
data class ShelfEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val code: String,
    val name: String,
    val locationId: Int
) {
    fun toShelf(): Shelf = Shelf(
        id = id,
        code = code,
        name = name,
        locationId = locationId
    )

    companion object {
        fun fromShelf(shelf: Shelf): ShelfEntity = ShelfEntity(
            id = shelf.id,
            code = shelf.code,
            name = shelf.name,
            locationId = shelf.locationId
        )
    }
} 