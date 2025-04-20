package com.example.barkodm.model

/**
 * Represents the available subscription plans for the application
 */
enum class SubscriptionPlan {
    /**
     * Free tier with limited products and inventory counts
     */
    FREE,
    
    /**
     * Premium tier with unlimited products and inventory counts
     */
    PREMIUM;
    
    val productLimit: Int
        get() = when (this) {
            FREE -> 1000
            PREMIUM -> Int.MAX_VALUE
        }
        
    val inventoryLimit: Int
        get() = when (this) {
            FREE -> 10
            PREMIUM -> Int.MAX_VALUE
        }
        
    val displayName: String
        get() = when (this) {
            FREE -> "Deneme"
            PREMIUM -> "Premium"
        }
        
    val description: String
        get() = when (this) {
            FREE -> "1000 ürün ve 10 sayım olabilir"
            PREMIUM -> "Sınırsız özellikler"
        }
    
    val monthlyPriceUSD: Double
        get() = when (this) {
            FREE -> 0.0
            PREMIUM -> 9.0
        }
        
    val yearlyPriceUSD: Double
        get() = when (this) {
            FREE -> 0.0
            PREMIUM -> 49.0
        }
    
    val showAds: Boolean
        get() = this == FREE
        
    val planId: String
        get() = name.lowercase()
        
    companion object {
        fun fromPlanId(id: String): SubscriptionPlan {
            return values().find { it.planId == id } ?: FREE
        }
    }
} 