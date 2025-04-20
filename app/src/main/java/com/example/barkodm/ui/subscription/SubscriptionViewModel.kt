package com.example.barkodm.ui.subscription

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barkodm.data.repository.SubscriptionRepository
import com.example.barkodm.model.SubscriptionPlan
import com.example.barkodm.model.SubscriptionStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Date
import javax.inject.Inject

enum class BillingPeriod {
    MONTHLY,
    YEARLY
}

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {
    
    private val _subscriptionStatus = MutableLiveData<SubscriptionStatus>()
    val subscriptionStatus: LiveData<SubscriptionStatus> = _subscriptionStatus
    
    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _selectedBillingPeriod = MutableLiveData<BillingPeriod>(BillingPeriod.MONTHLY)
    val selectedBillingPeriod: LiveData<BillingPeriod> = _selectedBillingPeriod
    
    // Google Play product IDs
    companion object {
        const val PREMIUM_MONTHLY_PRODUCT_ID = "com.example.barkodm.premium_monthly"
        const val PREMIUM_YEARLY_PRODUCT_ID = "com.example.barkodm.premium_yearly"
    }
    
    init {
        loadSubscriptionStatus()
    }
    
    private fun loadSubscriptionStatus() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Mock SubscriptionStatus oluşturalım
                val productCount = 50 // Test için sabit değer
                val inventoryCount = 5 // Test için sabit değer
                
                val mockStatus = SubscriptionStatus(
                    plan = SubscriptionPlan.FREE,
                    expiryDate = null,
                    productCount = productCount,
                    inventoryCount = inventoryCount,
                    isActive = true
                )
                
                _subscriptionStatus.value = mockStatus
                _errorMessage.value = null
            } catch (e: Exception) {
                _errorMessage.value = "Abonelik bilgileri alınırken hata oluştu: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    fun setBillingPeriod(period: BillingPeriod) {
        _selectedBillingPeriod.value = period
    }
    
    fun getProductIdForPurchase(): String {
        return when (_selectedBillingPeriod.value) {
            BillingPeriod.MONTHLY -> PREMIUM_MONTHLY_PRODUCT_ID
            BillingPeriod.YEARLY -> PREMIUM_YEARLY_PRODUCT_ID
            else -> PREMIUM_MONTHLY_PRODUCT_ID
        }
    }
    
    fun initiateUpgradeFlow() {
        // Bu fonksiyon gerçek uygulamada Google Play Billing Library'yi kullanacak
        // Şimdi ise sadece test amaçlı aboneliği simüle ediyoruz
        upgradeSubscription()
    }

    fun openManageSubscription(context: Context) {
        // Play Store abonelik yönetim sayfasını aç
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://play.google.com/store/account/subscriptions")
                setPackage("com.android.vending")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Play Store uygulaması yoksa web sayfasına yönlendir
            val webIntent = Intent(Intent.ACTION_VIEW, 
                Uri.parse("https://play.google.com/store/account/subscriptions"))
            context.startActivity(webIntent)
        }
    }

    // Test/demo amaçlı abonelik yükseltme fonksiyonu
    fun upgradeSubscription() {
        val currentStatus = _subscriptionStatus.value ?: return
        
        // PREMIUM plana yükseltme
        val calendar = Calendar.getInstance()
        
        // Seçilen abonelik süresine göre süreyi ayarla
        if (_selectedBillingPeriod.value == BillingPeriod.MONTHLY) {
            calendar.add(Calendar.MONTH, 1)
        } else {
            calendar.add(Calendar.YEAR, 1)
        }
        
        _subscriptionStatus.value = SubscriptionStatus(
            plan = SubscriptionPlan.PREMIUM,
            expiryDate = calendar.time,
            productCount = currentStatus.productCount,
            inventoryCount = currentStatus.inventoryCount,
            isActive = true
        )
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    // Güncel abonelik periyoduna göre fiyatı formatla
    fun getFormattedPrice(): String {
        return when (_selectedBillingPeriod.value) {
            BillingPeriod.MONTHLY -> "${SubscriptionPlan.PREMIUM.monthlyPriceUSD} USD/ay"
            BillingPeriod.YEARLY -> "${SubscriptionPlan.PREMIUM.yearlyPriceUSD} USD/yıl"
            else -> "${SubscriptionPlan.PREMIUM.monthlyPriceUSD} USD/ay"
        }
    }
    
    /**
     * Factory for creating a SubscriptionViewModel with dependency
     */
    class Factory(
        private val subscriptionRepository: SubscriptionRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SubscriptionViewModel::class.java)) {
                return SubscriptionViewModel(subscriptionRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 