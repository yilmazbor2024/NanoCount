package com.example.barkodm.ui.definitions

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import androidx.lifecycle.*
import com.example.barkodm.data.database.dao.ProductDao
import com.example.barkodm.data.database.entity.ProductEntity
import com.example.barkodm.data.model.Product
import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

/**
 * Ürün import işlemleri için ViewModel
 */
class ProductImportViewModel(private val productDao: ProductDao) : ViewModel() {

    // Import durumu
    private val _importStatus = MutableLiveData<ImportStatus>(ImportStatus.Idle)
    val importStatus: LiveData<ImportStatus> = _importStatus
    
    // Şablon oluşturma durumu
    private val _templateCreationStatus = MutableLiveData<TemplateStatus>(TemplateStatus.Idle)
    val templateCreationStatus: LiveData<TemplateStatus> = _templateCreationStatus
    
    // Parse edilmiş ürünler
    private val _parsedProducts = MutableLiveData<List<Product>>(emptyList())
    val parsedProducts: LiveData<List<Product>> = _parsedProducts
    
    /**
     * Dosyayı parse et
     */
    fun parseFile(fileUri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _importStatus.value = ImportStatus.Loading
            
            try {
                val products = withContext(Dispatchers.IO) {
                    parseFileInternal(fileUri, contentResolver)
                }
                
                Log.d("ProductImportViewModel", "Parsed ${products.size} products from file")
                _parsedProducts.value = products
                _importStatus.value = ImportStatus.Idle
            } catch (e: Exception) {
                Log.e("ProductImportViewModel", "Error parsing file", e)
                _importStatus.value = ImportStatus.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }
    
    /**
     * Ürünleri import et
     */
    fun importProducts(fileUri: Uri, contentResolver: ContentResolver) {
        viewModelScope.launch {
            _importStatus.value = ImportStatus.Loading
            
            try {
                val products = _parsedProducts.value ?: withContext(Dispatchers.IO) {
                    parseFileInternal(fileUri, contentResolver)
                }
                
                Log.d("ProductImportViewModel", "Importing ${products.size} products")
                
                if (products.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        val entities = products.map { product ->
                            ProductEntity(
                                id = 0, // Room otomatik ID atayacak
                                barcode = product.barcode,
                                code = product.code,
                                description = product.description,
                                unit = product.unit,
                                stockQuantity = product.stockQuantity,
                                createdAt = System.currentTimeMillis(),
                                updatedAt = System.currentTimeMillis()
                            )
                        }
                        
                        // Ürünleri veritabanına ekleyelim
                        val insertedIds = productDao.insertAll(entities)
                        Log.d("ProductImportViewModel", "Inserted ${insertedIds.size} products")
                    }
                    
                    _importStatus.value = ImportStatus.Success(products.size)
                } else {
                    _importStatus.value = ImportStatus.Error("Dosya boş veya uygun formatta değil")
                }
            } catch (e: Exception) {
                Log.e("ProductImportViewModel", "Error importing products", e)
                _importStatus.value = ImportStatus.Error(e.message ?: "Bilinmeyen hata")
            }
        }
    }
    
    /**
     * Dosyayı parse et (CSV veya Excel)
     */
    private fun parseFileInternal(fileUri: Uri, contentResolver: ContentResolver): List<Product> {
        val fileName = fileUri.lastPathSegment ?: ""
        Log.d("ProductImportViewModel", "Parsing file: $fileName")
        
        return when {
            fileName.endsWith(".csv", ignoreCase = true) -> {
                parseCsvFile(fileUri, contentResolver)
            }
            fileName.endsWith(".xls", ignoreCase = true) || fileName.endsWith(".xlsx", ignoreCase = true) -> {
                parseExcelFile(fileUri, contentResolver)
            }
            else -> {
                throw IOException("Desteklenmeyen dosya formatı. CSV veya Excel dosyası yükleyin.")
            }
        }
    }
    
    /**
     * CSV dosyasını parse et
     */
    private fun parseCsvFile(fileUri: Uri, contentResolver: ContentResolver): List<Product> {
        val products = mutableListOf<Product>()
        
        try {
            contentResolver.openInputStream(fileUri)?.use { inputStream ->
                // Use the kotlincsv library to parse CSV
                csvReader().open(inputStream) {
                    // Skip header row
                    readAllWithHeaderAsSequence().forEach { row ->
                        try {
                            val barcode = row["Barkod"] ?: row["barcode"] ?: row["BARKOD"] ?: ""
                            val code = row["Kod"] ?: row["code"] ?: row["KOD"] ?: ""
                            val description = row["Açıklama"] ?: row["description"] ?: row["ACIKLAMA"] ?: ""
                            val unit = row["Birim"] ?: row["unit"] ?: row["BIRIM"] ?: ""
                            
                            // Parse stock quantity
                            val stockText = row["Stok Miktarı"] ?: row["stock"] ?: row["STOK"] ?: "0"
                            val stockQuantity = try {
                                stockText.replace(",", ".").toDoubleOrNull() ?: 0.0
                            } catch (e: Exception) {
                                0.0
                            }
                            
                            if (barcode.isNotBlank() && code.isNotBlank()) {
                                products.add(Product(
                                    barcode = barcode,
                                    code = code,
                                    description = description,
                                    unit = unit,
                                    stockQuantity = stockQuantity,
                                    createdAt = System.currentTimeMillis(),
                                    updatedAt = System.currentTimeMillis()
                                ))
                            }
                        } catch (e: Exception) {
                            Log.e("ProductImportViewModel", "Error parsing CSV row", e)
                            // Skip this row and continue
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("ProductImportViewModel", "Error reading CSV file", e)
            throw IOException("CSV dosyası okunurken hata oluştu: ${e.message}")
        }
        
        Log.d("ProductImportViewModel", "Parsed ${products.size} products from CSV")
        return products
    }
    
    /**
     * Excel dosyasını parse et
     */
    private fun parseExcelFile(fileUri: Uri, contentResolver: ContentResolver): List<Product> {
        val products = mutableListOf<Product>()
        
        contentResolver.openInputStream(fileUri)?.use { inputStream ->
            val workbook = WorkbookFactory.create(inputStream)
            val sheet = workbook.getSheetAt(0)
            
            // İlk satır header, atla
            for (i in 1..sheet.lastRowNum) {
                val row = sheet.getRow(i) ?: continue
                
                try {
                    val barcode = row.getCell(0)?.stringCellValue ?: ""
                    val code = row.getCell(1)?.stringCellValue ?: ""
                    val description = row.getCell(2)?.stringCellValue ?: ""
                    val unit = row.getCell(3)?.stringCellValue ?: ""
                    val stockQuantity = row.getCell(4)?.numericCellValue ?: 0.0
                    
                    if (barcode.isNotBlank() && code.isNotBlank()) {
                        val product = Product(
                            id = 0,
                            barcode = barcode,
                            code = code,
                            description = description,
                            unit = unit,
                            stockQuantity = stockQuantity,
                            createdAt = System.currentTimeMillis(),
                            updatedAt = System.currentTimeMillis()
                        )
                        products.add(product)
                    }
                } catch (e: Exception) {
                    Log.e("ProductImportViewModel", "Error parsing Excel row ${i+1}", e)
                    // Satırı atla ve devam et
                    continue
                }
            }
            
            workbook.close()
        }
        
        Log.d("ProductImportViewModel", "Parsed ${products.size} products from Excel")
        return products
    }
    
    /**
     * Örnek şablon dosyası oluştur
     */
    fun createTemplateFile(context: Context) {
        viewModelScope.launch {
            try {
                val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val fileName = "urun_sablonu.csv"
                val csvFile = java.io.File(downloadsDir, fileName)
                
                withContext(Dispatchers.IO) {
                    // Use kotlincsv to write the template
                    csvWriter().open(csvFile) {
                        writeRow("Barkod", "Kod", "Açıklama", "Birim", "Stok Miktarı")
                        writeRow("8690637842134", "P001", "Ürün A", "ADET", "10.0")
                        writeRow("8690637842141", "P002", "Ürün B", "KG", "5.5")
                        writeRow("8690637842158", "P003", "Ürün C", "KUTU", "20.0")
                    }
                }
                
                Log.d("ProductImportViewModel", "Template created at ${csvFile.absolutePath}")
                _templateCreationStatus.value = TemplateStatus.Success(csvFile.absolutePath)
            } catch (e: Exception) {
                Log.e("ProductImportViewModel", "Error creating template", e)
                _templateCreationStatus.value = TemplateStatus.Error(e.message ?: "Şablon oluşturma hatası")
            }
        }
    }
    
    /**
     * Import durumu için sealed class
     */
    sealed class ImportStatus {
        object Idle : ImportStatus()
        object Loading : ImportStatus()
        data class Success(val count: Int) : ImportStatus()
        data class Error(val message: String) : ImportStatus()
    }
    
    /**
     * Şablon oluşturma durumu için sealed class
     */
    sealed class TemplateStatus {
        object Idle : TemplateStatus()
        data class Success(val filePath: String) : TemplateStatus()
        data class Error(val message: String) : TemplateStatus()
    }
}

/**
 * ProductImportViewModel factory
 */
class ProductImportViewModelFactory(private val productDao: ProductDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductImportViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductImportViewModel(productDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 