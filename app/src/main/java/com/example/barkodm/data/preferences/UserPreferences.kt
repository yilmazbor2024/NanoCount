package com.example.barkodm.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.example.barkodm.model.SubscriptionPlan
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val sharedPreferences: SharedPreferences by lazy {
        context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
    }

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // User data
    fun getUsername(): String? {
        return sharedPreferences.getString(KEY_USERNAME, null)
    }

    fun setUsername(username: String) {
        sharedPreferences.edit().putString(KEY_USERNAME, username).apply()
    }

    fun getEmail(): String? {
        return sharedPreferences.getString(KEY_EMAIL, null)
    }

    fun setEmail(email: String) {
        sharedPreferences.edit().putString(KEY_EMAIL, email).apply()
    }

    // KVKK consent status
    fun hasAcceptedKVKK(): Boolean {
        return sharedPreferences.getBoolean(KEY_KVKK_CONSENT, false)
    }
    
    fun setKVKKConsent(accepted: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_KVKK_CONSENT, accepted).apply()
    }

    // Subscription data
    fun getSubscriptionPlan(): SubscriptionPlan? {
        val planId = sharedPreferences.getString(KEY_SUBSCRIPTION_PLAN, null)
        return if (planId != null) {
            SubscriptionPlan.fromPlanId(planId)
        } else {
            null
        }
    }

    fun setSubscriptionPlan(plan: SubscriptionPlan) {
        sharedPreferences.edit().putString(KEY_SUBSCRIPTION_PLAN, plan.planId).apply()
    }

    fun getSubscriptionExpiryDate(): Date? {
        val dateString = sharedPreferences.getString(KEY_SUBSCRIPTION_EXPIRY, null)
        return if (dateString != null) {
            try {
                dateFormat.parse(dateString)
            } catch (e: Exception) {
                null
            }
        } else {
            null
        }
    }

    fun setSubscriptionExpiryDate(date: Date?) {
        if (date != null) {
            sharedPreferences.edit().putString(KEY_SUBSCRIPTION_EXPIRY, dateFormat.format(date)).apply()
        } else {
            sharedPreferences.edit().remove(KEY_SUBSCRIPTION_EXPIRY).apply()
        }
    }

    fun isSubscriptionActive(): Boolean {
        val plan = getSubscriptionPlan()
        
        // Free plan is always active
        if (plan == SubscriptionPlan.FREE) {
            return true
        }
        
        // Premium plan needs to check expiry date
        val expiryDate = getSubscriptionExpiryDate()
        return expiryDate != null && expiryDate.after(Date())
    }

    // App settings
    fun isDarkModeEnabled(): Boolean {
        return sharedPreferences.getBoolean(KEY_DARK_MODE, false)
    }

    fun setDarkModeEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_DARK_MODE, enabled).apply()
    }

    fun getDefaultBranchId(): Long? {
        val id = sharedPreferences.getLong(KEY_DEFAULT_BRANCH, -1)
        return if (id == -1L) null else id
    }

    fun setDefaultBranchId(branchId: Long?) {
        sharedPreferences.edit().apply {
            if (branchId == null) {
                remove(KEY_DEFAULT_BRANCH)
            } else {
                putLong(KEY_DEFAULT_BRANCH, branchId)
            }
        }.apply()
    }

    fun getDefaultWarehouseId(): Long? {
        val id = sharedPreferences.getLong(KEY_DEFAULT_WAREHOUSE, -1)
        return if (id == -1L) null else id
    }

    fun setDefaultWarehouseId(warehouseId: Long?) {
        sharedPreferences.edit().apply {
            if (warehouseId == null) {
                remove(KEY_DEFAULT_WAREHOUSE)
            } else {
                putLong(KEY_DEFAULT_WAREHOUSE, warehouseId)
            }
        }.apply()
    }

    fun clearUserData() {
        sharedPreferences.edit().apply {
            remove(KEY_USERNAME)
            remove(KEY_EMAIL)
            remove(KEY_SUBSCRIPTION_PLAN)
            remove(KEY_SUBSCRIPTION_EXPIRY)
        }.apply()
    }

    companion object {
        private const val PREFERENCES_NAME = "barkodm_preferences"
        
        // User keys
        private const val KEY_USERNAME = "username"
        private const val KEY_EMAIL = "email"
        private const val KEY_KVKK_CONSENT = "kvkk_consent"
        
        // Subscription keys
        private const val KEY_SUBSCRIPTION_PLAN = "subscription_plan"
        private const val KEY_SUBSCRIPTION_EXPIRY = "subscription_expiry"
        
        // Settings keys
        private const val KEY_DARK_MODE = "dark_mode"
        private const val KEY_DEFAULT_BRANCH = "default_branch"
        private const val KEY_DEFAULT_WAREHOUSE = "default_warehouse"
    }
} 