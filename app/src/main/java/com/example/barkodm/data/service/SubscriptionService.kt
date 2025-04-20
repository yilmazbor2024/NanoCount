package com.example.barkodm.data.service

import android.content.Context
import android.content.SharedPreferences
import com.example.barkodm.model.SubscriptionPlan
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service to manage user subscriptions
 */
@Singleton
class SubscriptionService @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "subscription_prefs"
        private const val KEY_CURRENT_PLAN = "current_plan"
        private const val KEY_EXPIRY_DATE = "expiry_date"
        private const val KEY_PRODUCT_COUNT = "product_count"
        private const val KEY_INVENTORY_COUNT = "inventory_count"
        private const val DATE_FORMAT = "yyyy-MM-dd"
    }

    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    private val dateFormat = SimpleDateFormat(DATE_FORMAT, Locale.getDefault())

    /**
     * Get the current subscription plan
     */
    suspend fun getCurrentPlan(): SubscriptionPlan = withContext(Dispatchers.IO) {
        val planId = sharedPreferences.getString(KEY_CURRENT_PLAN, SubscriptionPlan.FREE.planId) ?: SubscriptionPlan.FREE.planId
        return@withContext SubscriptionPlan.fromPlanId(planId)
    }

    /**
     * Get the expiry date of the current subscription
     * @return Date or null if no expiry date is set (free plan)
     */
    suspend fun getExpiryDate(): Date? = withContext(Dispatchers.IO) {
        val dateStr = sharedPreferences.getString(KEY_EXPIRY_DATE, null)
        return@withContext dateStr?.let { dateFormat.parse(it) }
    }

    /**
     * Update the user's subscription
     * @param plan The new subscription plan
     * @param expiryDate The expiry date of the subscription (null for free plan)
     */
    suspend fun updateSubscription(plan: SubscriptionPlan, expiryDate: Date? = null) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().apply {
            putString(KEY_CURRENT_PLAN, plan.planId)
            if (expiryDate != null) {
                putString(KEY_EXPIRY_DATE, dateFormat.format(expiryDate))
            } else {
                remove(KEY_EXPIRY_DATE)
            }
        }.apply()
    }

    /**
     * Check if the current subscription is valid
     */
    suspend fun isSubscriptionValid(): Boolean = withContext(Dispatchers.IO) {
        val plan = getCurrentPlan()
        
        // Free plan is always valid
        if (plan == SubscriptionPlan.FREE) {
            return@withContext true
        }
        
        // Check if premium plan has a valid expiry date
        val expiryDate = getExpiryDate() ?: return@withContext false
        return@withContext expiryDate.after(Date())
    }

    /**
     * Get current product count
     */
    suspend fun getProductCount(): Int = withContext(Dispatchers.IO) {
        return@withContext sharedPreferences.getInt(KEY_PRODUCT_COUNT, 0)
    }

    /**
     * Update product count
     */
    suspend fun updateProductCount(count: Int) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putInt(KEY_PRODUCT_COUNT, count).apply()
    }

    /**
     * Get current inventory count
     */
    suspend fun getInventoryCount(): Int = withContext(Dispatchers.IO) {
        return@withContext sharedPreferences.getInt(KEY_INVENTORY_COUNT, 0)
    }

    /**
     * Update inventory count
     */
    suspend fun updateInventoryCount(count: Int) = withContext(Dispatchers.IO) {
        sharedPreferences.edit().putInt(KEY_INVENTORY_COUNT, count).apply()
    }

    /**
     * Check if user can add more products based on their subscription
     */
    suspend fun canAddMoreProducts(): Boolean = withContext(Dispatchers.IO) {
        val plan = getCurrentPlan()
        val currentCount = getProductCount()
        
        // Check if subscription is valid
        if (!isSubscriptionValid() && plan != SubscriptionPlan.FREE) {
            resetToFreePlan()
            return@withContext currentCount < SubscriptionPlan.FREE.productLimit
        }
        
        return@withContext currentCount < plan.productLimit
    }

    /**
     * Check if user can create more inventories based on their subscription
     */
    suspend fun canCreateMoreInventories(): Boolean = withContext(Dispatchers.IO) {
        val plan = getCurrentPlan()
        val currentCount = getInventoryCount()
        
        // Check if subscription is valid
        if (!isSubscriptionValid() && plan != SubscriptionPlan.FREE) {
            resetToFreePlan()
            return@withContext currentCount < SubscriptionPlan.FREE.inventoryLimit
        }
        
        return@withContext currentCount < plan.inventoryLimit
    }

    /**
     * Reset to free plan if subscription has expired
     */
    private suspend fun resetToFreePlan() = withContext(Dispatchers.IO) {
        updateSubscription(SubscriptionPlan.FREE)
    }
} 