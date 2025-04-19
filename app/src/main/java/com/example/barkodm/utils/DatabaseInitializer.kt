package com.example.barkodm.utils

import android.content.Context
import com.example.barkodm.data.database.AppDatabase
import com.example.barkodm.data.database.entity.UserEntity
import com.example.barkodm.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Uygulama ilk kez başlatıldığında veritabanını başlatmak için kullanılır.
 * Varsayılan kullanıcıları ve diğer gerekli verileri ekler.
 */
class DatabaseInitializer(private val context: Context) {

    private val database = AppDatabase.getDatabase(context)

    /**
     * Veritabanını başlatır ve varsayılan verileri ekler.
     */
    suspend fun initialize() {
        withContext(Dispatchers.IO) {
            initializeUsers()
        }
    }

    /**
     * Kullanıcı tablosunun boş olup olmadığını kontrol eder ve boşsa
     * varsayılan admin ve kullanıcı hesaplarını ekler.
     */
    private suspend fun initializeUsers() {
        val userDao = database.userDao()
        
        // Kullanıcı tablosu boşsa, varsayılan kullanıcıları ekle
        if (!userDao.hasAnyUser()) {
            // Admin kullanıcısı
            val adminUser = User(
                id = 0,
                username = "admin",
                password = "admin123",
                isAdmin = true
            )
            userDao.insert(UserEntity.fromUser(adminUser))
            
            // Normal kullanıcı
            val normalUser = User(
                id = 0,
                username = "user",
                password = "user123",
                isAdmin = false
            )
            userDao.insert(UserEntity.fromUser(normalUser))
        }
    }
} 