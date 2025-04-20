package com.example.barkodm.ui.product

import android.content.Context
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.barkodm.data.model.Product
import com.example.barkodm.data.repository.ProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*

class ProductImportViewModel(private val repository: ProductRepository) : ViewModel() {

    private val _parsedProducts = MutableLiveData<List<Product>>()
    val parsedProducts: LiveData<List<Product>> = _parsedProducts

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val _importComplete = MutableLiveData<Boolean>()
    val importComplete: LiveData<Boolean> = _importComplete

    fun parseFile(context: Context, uri: Uri) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val products = withContext(Dispatchers.IO) {
                    parseCSVFile(context, uri)
                }
                _parsedProducts.value = products
            } catch (e: Exception) {
                _error.value = "Error parsing file: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun parseCSVFile(context: Context, uri: Uri): List<Product> {
        val products = mutableListOf<Product>()
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    // Header satırını oku ve atla
                    var headerLine = reader.readLine()
                    if (headerLine == null) {
                        throw Exception("Dosya boş veya geçersiz formatta")
                    }
                    
                    // Satır satır oku
                    var line: String?
                    while (reader.readLine().also { line = it } != null) {
                        try {
                            val columns = line!!.split(",")
                            if (columns.size >= 2) { // En az barkod ve ürün adı olmalı
                                val barcode = columns[0].trim()
                                val name = columns[1].trim()
                                val price = if (columns.size > 2) columns[2].trim().toDoubleOrNull() ?: 0.0 else 0.0
                                val stock = if (columns.size > 3) columns[3].trim().toDoubleOrNull() ?: 0.0 else 0.0
                                
                                products.add(Product(
                                    id = 0,
                                    barcode = barcode,
                                    code = name,
                                    description = name, // Açıklama için ürün adını kullan
                                    unit = "Adet", // Varsayılan birim
                                    stockQuantity = stock,
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                ))
                            }
                        } catch (e: Exception) {
                            // Hatalı satırları atla
                            continue
                        }
                    }
                }
            }
        } catch (e: Exception) {
            _error.value = "CSV dosya okuma hatası: ${e.message}"
        }
        return products
    }

    fun importProducts() {
        val products = _parsedProducts.value ?: return
        if (products.isEmpty()) {
            _error.value = "No products to import"
            return
        }

        _isLoading.value = true
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    for (product in products) {
                        repository.insertProduct(product)
                    }
                }
                _importComplete.value = true
            } catch (e: Exception) {
                _error.value = "Error importing products: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetState() {
        _parsedProducts.value = emptyList()
        _importComplete.value = false
        _error.value = null
    }
} 