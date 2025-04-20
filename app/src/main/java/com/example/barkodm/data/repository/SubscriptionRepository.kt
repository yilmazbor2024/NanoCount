package com.example.barkodm.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.barkodm.data.database.dao.ProductDao
import com.example.barkodm.model.SubscriptionPlan
import com.example.barkodm.model.SubscriptionStatus
import com.example.barkodm.data.preferences.UserPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository class for managing subscription data
 */
@Singleton
class SubscriptionRepository @Inject constructor(
    private val userPreferences: UserPreferences,
    private val productDao: ProductDao
) {
    // In a real app, this would be stored in SharedPreferences or a remote database
    private val _currentPlan = MutableLiveData<SubscriptionPlan>(SubscriptionPlan.FREE)
    val currentPlan: LiveData<SubscriptionPlan> = _currentPlan

    // In a real app, this would come from Google Play Billing API
    private val _expiryDate = MutableLiveData<Date?>(null)
    val expiryDate: LiveData<Date?> = _expiryDate

    /**
     * Gets the current subscription status including counts and limits
     */
    suspend fun getCurrentSubscriptionStatus(): SubscriptionStatus = withContext(Dispatchers.IO) {
        val currentPlan = userPreferences.getSubscriptionPlan() ?: SubscriptionPlan.FREE
        val expiryDate = userPreferences.getSubscriptionExpiryDate()
        val productCount = 50 // Sabit değer - gerçek uygulamada hesaplanacak
        val inventoryCount = 5 // Sabit değer - gerçek uygulamada hesaplanacak
        
        val isActive = when (currentPlan) {
            SubscriptionPlan.FREE -> true
            SubscriptionPlan.PREMIUM -> {
                val expiry = expiryDate ?: return@withContext createFreeStatus(productCount, inventoryCount)
                expiry.after(Date())
            }
        }
        
        // If Premium subscription expired, return to FREE plan
        if (currentPlan == SubscriptionPlan.PREMIUM && !isActive) {
            userPreferences.setSubscriptionPlan(SubscriptionPlan.FREE)
            userPreferences.setSubscriptionExpiryDate(null)
            return@withContext createFreeStatus(productCount, inventoryCount)
        }
        
        SubscriptionStatus(
            plan = currentPlan,
            expiryDate = expiryDate,
            productCount = productCount,
            inventoryCount = inventoryCount,
            isActive = isActive
        )
    }
    
    private fun createFreeStatus(productCount: Int, inventoryCount: Int): SubscriptionStatus {
        return SubscriptionStatus(
            plan = SubscriptionPlan.FREE,
            expiryDate = null,
            productCount = productCount,
            inventoryCount = inventoryCount,
            isActive = true
        )
    }
    
    /**
     * Updates the subscription plan and calculates new expiry date
     */
    suspend fun updateSubscriptionPlan(plan: SubscriptionPlan, months: Int = 1): Boolean = withContext(Dispatchers.IO) {
        try {
            userPreferences.setSubscriptionPlan(plan)
            
            val expiryDate = if (plan == SubscriptionPlan.PREMIUM) {
                Calendar.getInstance().apply {
                    // If there's an existing expiry date that's in the future, extend from that
                    val currentExpiry = userPreferences.getSubscriptionExpiryDate()
                    if (currentExpiry != null && currentExpiry.after(Date())) {
                        time = currentExpiry
                    }
                    add(Calendar.MONTH, months)
                }.time
            } else {
                null
            }
            
            userPreferences.setSubscriptionExpiryDate(expiryDate)
            
            // LiveData'ları güncelle
            _currentPlan.postValue(plan)
            _expiryDate.postValue(expiryDate)
            
            true
        } catch (e: Exception) {
            false
        }
    }
    
    /**
     * Checks if the current plan allows adding more products
     */
    suspend fun canAddMoreProducts(): Boolean = withContext(Dispatchers.IO) {
        val status = getCurrentSubscriptionStatus()
        !status.isProductLimitReached && status.isActive
    }
    
    /**
     * Checks if the current plan allows creating more inventories
     */
    suspend fun canCreateMoreInventories(): Boolean = withContext(Dispatchers.IO) {
        val status = getCurrentSubscriptionStatus()
        !status.isInventoryLimitReached && status.isActive
    }
} 