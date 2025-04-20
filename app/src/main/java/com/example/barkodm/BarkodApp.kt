package com.example.barkodm

import android.app.Application
import com.example.barkodm.data.database.AppDatabase
import com.example.barkodm.data.repository.ProductRepository
import com.example.barkodm.utils.DatabaseInitializer
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context
import android.util.Log
import java.io.File
import androidx.multidex.MultiDex

@HiltAndroidApp
class BarkodApp : Application() {

    // Uygulama veritabanı
    val database by lazy { AppDatabase.getDatabase(this) }
    
    // Ürün repository
    val productRepository by lazy { ProductRepository(database.productDao()) }
    
    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
    
    // Uygulama başlatma işlemleri
    override fun onCreate() {
        super.onCreate()
        
        // Disable AdMob ContentProvider (temporary fix)
        // AdMob initialization will be done manually later when properly configured
        val disableContentProviders = mapOf(
            "com.google.android.gms.ads.MobileAdsInitProvider" to true
        )
        
        // Veritabanını başlat
        CoroutineScope(Dispatchers.IO).launch {
            val databaseInitializer = DatabaseInitializer(applicationContext)
            databaseInitializer.initialize()
        }
    }
    
    /**
     * Geliştirme aşamasında veritabanı şeması değiştiğinde kullanmak için
     * tüm veritabanı dosyalarını siler
     */
    private fun clearDatabaseFiles() {
        try {
            // Veritabanı ana klasörü
            val databases = File(applicationContext.dataDir, "databases")
            Log.d("BarkodApp", "Clearing database files from: ${databases.absolutePath}")
            
            if (databases.exists() && databases.isDirectory) {
                val files = databases.listFiles()
                files?.forEach { file ->
                    if (file.delete()) {
                        Log.d("BarkodApp", "Deleted file: ${file.absolutePath}")
                    } else {
                        Log.d("BarkodApp", "Failed to delete file: ${file.absolutePath}")
                    }
                }
                
                // Klasörün kendisini de sil
                if (databases.delete()) {
                    Log.d("BarkodApp", "Deleted database directory")
                }
            } else {
                Log.d("BarkodApp", "Database directory does not exist")
            }
            
            // Veritabanının ön belleğini de temizle
            applicationContext.deleteDatabase("inventory_database")
            Log.d("BarkodApp", "Cleared database cache")
        } catch (e: Exception) {
            Log.e("BarkodApp", "Error while deleting database files", e)
            e.printStackTrace()
        }
    }
} 