package com.example.barkodm.utils

import android.content.Context
import com.example.barkodm.data.database.AppDatabase
import com.example.barkodm.data.database.entity.UserEntity
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
     * Varsayılan admin ve kullanıcı hesaplarını ekler.
     */
    private suspend fun initializeUsers() {
        val userDao = database.userDao()
        
        // Always add these users during development until we properly implement user management
        // Admin kullanıcısı
        val adminUser = UserEntity(
            id = 0,
            username = "admin",
            email = "admin@example.com",
            role = "admin",
            password = "admin123",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        userDao.insert(adminUser)
        
        // Normal kullanıcı
        val normalUser = UserEntity(
            id = 0,
            username = "user",
            email = "user@example.com",
            role = "user",
            password = "user123",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        userDao.insert(normalUser)
    }
} 