package com.example.barkodm.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.barkodm.data.database.AppDatabase

class HomeViewModel(private val database: AppDatabase) : ViewModel() {

    // Açık sayımların sayısı
    val openInventoryCount: LiveData<Int> = database.inventoryHeaderDao().getOpenInventoriesCount()
    
    // Toplam ürün sayısı
    val productCount: LiveData<Int> = database.productDao().getProductCount()
}

class HomeViewModelFactory(private val database: AppDatabase) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}