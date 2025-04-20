package com.example.barkodm.ui.warehouse

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barkodm.data.database.dao.BranchDao
import com.example.barkodm.data.database.dao.WarehouseDao
import com.example.barkodm.data.database.entity.BranchEntity
import com.example.barkodm.data.database.entity.WarehouseEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WarehouseViewModel @Inject constructor(
    private val warehouseDao: WarehouseDao,
    private val branchDao: BranchDao
) : ViewModel() {

    val warehouses = warehouseDao.getAllWarehouses() ?: MutableLiveData<List<WarehouseEntity>>(emptyList())
    val branches = branchDao.getAllBranches()
    
    private val _warehouse = MutableLiveData<WarehouseEntity>()
    val warehouse: LiveData<WarehouseEntity> = _warehouse
    
    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    fun getWarehouse(id: Long) {
        viewModelScope.launch {
            try {
                val result = warehouseDao.getWarehouseById(id.toInt())
                _warehouse.postValue(result)
            } catch (e: Exception) {
                _errorMessage.postValue("Depo bilgileri alınamadı: ${e.message}")
            }
        }
    }
    
    fun saveWarehouse(warehouse: WarehouseEntity) {
        viewModelScope.launch {
            try {
                Log.d("WarehouseViewModel", "Saving warehouse: ${warehouse.name}, ID: ${warehouse.id}, BranchID: ${warehouse.branchId}")
                
                val id = warehouseDao.insert(warehouse)
                Log.d("WarehouseViewModel", "Warehouse saved with ID: $id")
                
                _saveStatus.postValue(true)
            } catch (e: Exception) {
                Log.e("WarehouseViewModel", "Error saving warehouse", e)
                _errorMessage.postValue("Depo kaydedilemedi: ${e.message}")
                _saveStatus.postValue(false)
            }
        }
    }
    
    fun deleteWarehouse(id: Long) {
        viewModelScope.launch {
            try {
                warehouseDao.deleteWarehouseById(id.toInt())
            } catch (e: Exception) {
                _errorMessage.postValue("Depo silinemedi: ${e.message}")
            }
        }
    }
    
    fun resetErrorMessage() {
        _errorMessage.value = null
    }
    
    fun getWarehousesByBranch(branchId: Long): LiveData<List<WarehouseEntity>> {
        return warehouseDao.getWarehousesByBranch(branchId.toInt())
    }
} 