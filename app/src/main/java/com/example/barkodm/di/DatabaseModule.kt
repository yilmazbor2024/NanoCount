package com.example.barkodm.di

import android.content.Context
import com.example.barkodm.data.database.AppDatabase
import com.example.barkodm.data.database.dao.BranchDao
import com.example.barkodm.data.database.dao.ProductDao
import com.example.barkodm.data.database.dao.WarehouseDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    fun provideProductDao(database: AppDatabase): ProductDao {
        return database.productDao()
    }
    
    @Provides
    fun provideWarehouseDao(database: AppDatabase): WarehouseDao {
        return database.warehouseDao()
    }
    
    @Provides
    fun provideBranchDao(database: AppDatabase): BranchDao {
        return database.branchDao()
    }
} 