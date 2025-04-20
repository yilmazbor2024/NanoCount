package com.example.barkodm.ui.shelf

import androidx.lifecycle.*
import com.example.barkodm.data.database.dao.LocationDao
import com.example.barkodm.data.database.dao.ShelfDao
import com.example.barkodm.data.database.entity.LocationEntity
import com.example.barkodm.data.database.entity.ShelfEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ShelfViewModel(
    private val locationDao: LocationDao,
    private val shelfDao: ShelfDao
) : ViewModel() {
    
    private val _locations = MutableStateFlow<List<LocationEntity>>(emptyList())
    val locations: StateFlow<List<LocationEntity>> = _locations
    
    private val _selectedShelf = MutableStateFlow<ShelfEntity?>(null)
    val selectedShelf: StateFlow<ShelfEntity?> = _selectedShelf
    
    init {
        loadLocations()
    }
    
    // Get shelves by location ID
    fun getShelvesByLocation(locationId: Int): LiveData<List<ShelfEntity>> {
        return shelfDao.getShelvesByLocation(locationId)
    }
    
    // Load all locations
    private fun loadLocations() {
        viewModelScope.launch {
            try {
                _locations.value = locationDao.getAllLocations()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Get a shelf by ID
    fun getShelfById(shelfId: Int) {
        viewModelScope.launch {
            try {
                _selectedShelf.value = shelfDao.getShelfById(shelfId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
    
    // Save a shelf (create or update)
    fun saveShelf(shelf: ShelfEntity, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                shelfDao.insert(shelf)
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
    
    // Delete a shelf
    fun deleteShelf(shelf: ShelfEntity, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                shelfDao.delete(shelf)
                callback(true)
            } catch (e: Exception) {
                callback(false)
            }
        }
    }
}

/**
 * ViewModel Factory
 */
class ShelfViewModelFactory(
    private val locationDao: LocationDao,
    private val shelfDao: ShelfDao
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShelfViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ShelfViewModel(locationDao, shelfDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 