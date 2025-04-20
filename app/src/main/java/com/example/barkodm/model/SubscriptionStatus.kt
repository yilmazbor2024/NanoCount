package com.example.barkodm.model

import java.util.Date

data class SubscriptionStatus(
    val plan: SubscriptionPlan,
    val expiryDate: Date? = null,
    val productCount: Int = 0,
    val inventoryCount: Int = 0,
    val isActive: Boolean = true,
    val remainingProducts: Int = plan.productLimit - productCount,
    val remainingInventories: Int = plan.inventoryLimit - inventoryCount
) {
    val isProductLimitReached: Boolean
        get() = productCount >= plan.productLimit
        
    val isInventoryLimitReached: Boolean
        get() = inventoryCount >= plan.inventoryLimit
        
    val productLimitMessage: String
        get() = when (plan) {
            SubscriptionPlan.FREE -> "Deneme sürümünde $productCount/${plan.productLimit} ürün kullanıldı"
            SubscriptionPlan.PREMIUM -> "Sınırsız ürün ekleyebilirsiniz"
        }

    val inventoryLimitMessage: String
        get() = when (plan) {
            SubscriptionPlan.FREE -> "Deneme sürümünde $inventoryCount/${plan.inventoryLimit} sayım kullanıldı"
            SubscriptionPlan.PREMIUM -> "Sınırsız sayım oluşturabilirsiniz"
        }
    
    companion object {
        const val TRIAL_PRODUCT_LIMIT = 1000
        const val TRIAL_INVENTORY_LIMIT = 10
        
        // Geriye uyumluluk için
        const val FREE_MAX_PRODUCTS = TRIAL_PRODUCT_LIMIT
        const val FREE_MAX_INVENTORIES = TRIAL_INVENTORY_LIMIT
    }
} 