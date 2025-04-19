package com.example.barkodm.ui.navigation

import androidx.navigation.NavDirections
import com.example.barkodm.R

/**
 * Global navigation yönlendirmeleri için yardımcı sınıf.
 * Safe Args plugin'i tarafından otomatik oluşturulan kodun yerine 
 * geçici bir çözüm olarak kullanılabilir.
 */
class NavGraphDirections {
    companion object {
        /**
         * Herhangi bir ekrandan envanter detay ekranına direkt geçiş için global aksiyon.
         * 
         * @param inventoryId Gösterilecek envanter ID'si
         * @return Navigasyon yönergesi
         */
        fun actionGlobalNavigationInventoryDetail(inventoryId: Int): NavDirections {
            return object : NavDirections {
                override val actionId: Int = R.id.action_global_navigation_inventory_detail
                
                override val arguments = androidx.core.os.bundleOf(
                    "inventoryId" to inventoryId
                )
            }
        }
    }
} 