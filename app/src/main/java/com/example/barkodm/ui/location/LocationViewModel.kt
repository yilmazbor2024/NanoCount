package com.example.barkodm.ui.location

import androidx.lifecycle.*
import com.example.barkodm.data.database.dao.BranchDao
import com.example.barkodm.data.database.dao.LocationDao
import com.example.barkodm.data.database.dao.ShelfDao
import com.example.barkodm.data.database.dao.WarehouseDao
import com.example.barkodm.data.database.entity.BranchEntity
import com.example.barkodm.data.database.entity.LocationEntity
import com.example.barkodm.data.database.entity.ShelfEntity
import com.example.barkodm.data.database.entity.WarehouseEntity
import kotlinx.coroutines.launch

class LocationViewModel(
    private val branchDao: BranchDao? = null,
    private val warehouseDao: WarehouseDao? = null,
    private val locationDao: LocationDao? = null,
    private val shelfDao: ShelfDao? = null
) : ViewModel() {

    // Branch operations
    val allBranches = branchDao?.getAllBranches() ?: MutableLiveData<List<BranchEntity>>(emptyList())
    
    fun getBranchById(id: Int): LiveData<BranchEntity?> {
        val result = MutableLiveData<BranchEntity?>()
        viewModelScope.launch {
            result.value = branchDao?.getBranchById(id)
        }
        return result
    }
    
    fun saveBranch(branch: BranchEntity, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                branchDao?.insert(branch)
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    
    fun deleteBranch(branch: BranchEntity, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val result = branchDao?.delete(branch) ?: 0
                callback(result > 0)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    
    // Warehouse operations
    fun getWarehousesByBranch(branchId: Int): LiveData<List<WarehouseEntity>> {
        return warehouseDao?.getWarehousesByBranch(branchId) ?: MutableLiveData<List<WarehouseEntity>>(emptyList())
    }
    
    fun getWarehouseById(id: Int): LiveData<WarehouseEntity?> {
        val result = MutableLiveData<WarehouseEntity?>()
        viewModelScope.launch {
            result.value = warehouseDao?.getWarehouseById(id)
        }
        return result
    }
    
    fun saveWarehouse(warehouse: WarehouseEntity, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                warehouseDao?.insert(warehouse)
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    
    fun deleteWarehouse(warehouse: WarehouseEntity, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val result = warehouseDao?.delete(warehouse) ?: 0
                callback(result > 0)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    
    // Location operations
    fun getAllLocations(): LiveData<List<LocationEntity>> {
        val result = MutableLiveData<List<LocationEntity>>(emptyList())
        viewModelScope.launch {
            try {
                // For now, just get all locations
                // In a production app, you might want to filter by branch or warehouse
                locationDao?.getAllLocations()?.let { locations ->
                    result.postValue(locations)
                }
            } catch (e: Exception) {
                result.postValue(emptyList())
            }
        }
        return result
    }
    
    fun getLocationsByWarehouse(warehouseId: Int): LiveData<List<LocationEntity>> {
        return locationDao?.getLocationsByWarehouse(warehouseId) ?: MutableLiveData<List<LocationEntity>>(emptyList())
    }
    
    fun getLocationById(id: Int): LiveData<LocationEntity?> {
        val result = MutableLiveData<LocationEntity?>()
        viewModelScope.launch {
            result.value = locationDao?.getLocationById(id)
        }
        return result
    }
    
    fun saveLocation(location: LocationEntity, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                locationDao?.insert(location)
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    
    fun deleteLocation(location: LocationEntity, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val result = locationDao?.delete(location) ?: 0
                callback(result > 0)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    
    // Shelf operations
    fun getShelvesByLocation(locationId: Int): LiveData<List<ShelfEntity>> {
        return shelfDao?.getShelvesByLocation(locationId) ?: MutableLiveData<List<ShelfEntity>>(emptyList())
    }
    
    fun getShelfById(id: Int): LiveData<ShelfEntity?> {
        val result = MutableLiveData<ShelfEntity?>()
        viewModelScope.launch {
            result.value = shelfDao?.getShelfById(id)
        }
        return result
    }
    
    fun saveShelf(shelf: ShelfEntity, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                shelfDao?.insert(shelf)
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    
    fun deleteShelf(shelf: ShelfEntity, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val result = shelfDao?.delete(shelf) ?: 0
                callback(result > 0)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
}

class LocationViewModelFactory(
    private val branchDao: BranchDao? = null,
    private val warehouseDao: WarehouseDao? = null,
    private val locationDao: LocationDao? = null,
    private val shelfDao: ShelfDao? = null
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LocationViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LocationViewModel(branchDao, warehouseDao, locationDao, shelfDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 