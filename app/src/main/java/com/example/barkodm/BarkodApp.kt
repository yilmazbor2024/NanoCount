package com.example.barkodm

import android.app.Application
import com.example.barkodm.data.database.AppDatabase
import com.example.barkodm.utils.DatabaseInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class BarkodApp : Application() {

    // Uygulama veritabanı
    val database by lazy { AppDatabase.getDatabase(this) }
    
    // Uygulama başlatma işlemleri
    override fun onCreate() {
        super.onCreate()
        
        // Veritabanını başlat
        CoroutineScope(Dispatchers.IO).launch {
            val databaseInitializer = DatabaseInitializer(applicationContext)
            databaseInitializer.initialize()
        }
    }
} 