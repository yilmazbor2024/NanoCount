package com.example.barkodm

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.barkodm.data.preferences.UserPreferences
import com.example.barkodm.databinding.ActivityMainBinding
import com.example.barkodm.ui.common.KVKKConsentDialog
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    
    @Inject
    lateinit var userPreferences: UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show KVKK consent dialog if needed
        checkAndShowKVKKConsent()

        val navView = binding.navView
        navController = findNavController(R.id.nav_host_fragment_activity_main)
        
        // Ana ekranlarda bottom navigation görünür, diğer ekranlarda gizli olacak
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.navigation_home,
                R.id.navigation_inventory,
                R.id.navigation_definitions,
                R.id.navigation_reports,
                R.id.navigation_settings -> {
                    navView.visibility = android.view.View.VISIBLE
                }
                else -> {
                    navView.visibility = android.view.View.GONE
                }
            }
        }
        
        // Ana ekranları ayarla
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, 
                R.id.navigation_inventory, 
                R.id.navigation_definitions,
                R.id.navigation_reports,
                R.id.navigation_settings
            )
        )
        
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }
    
    private fun checkAndShowKVKKConsent() {
        // Show KVKK consent dialog if user hasn't accepted it yet
        KVKKConsentDialog.showIfNeeded(
            this,
            supportFragmentManager,
            userPreferences
        ) {
            // This callback will be called when user accepts the consent
            // No need to do anything special, just continue with app initialization
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}