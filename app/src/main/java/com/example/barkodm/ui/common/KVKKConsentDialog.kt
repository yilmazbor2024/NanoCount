package com.example.barkodm.ui.common

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.barkodm.R
import com.example.barkodm.data.preferences.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Dialog to show KVKK consent on first app launch
 */
@AndroidEntryPoint
class KVKKConsentDialog : DialogFragment() {
    
    @Inject
    lateinit var userPreferences: UserPreferences
    
    private var onConsentCallback: (() -> Unit)? = null
    
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireContext())
        val view = layoutInflater.inflate(R.layout.dialog_kvkk_consent, null)
        
        val checkBox = view.findViewById<CheckBox>(R.id.checkboxConsent)
        val acceptButton = view.findViewById<Button>(R.id.buttonAccept)
        
        // Enable the accept button only when checkbox is checked
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            acceptButton.isEnabled = isChecked
        }
        
        acceptButton.setOnClickListener {
            userPreferences.setKVKKConsent(true)
            onConsentCallback?.invoke()
            dismiss()
        }
        
        builder.setView(view)
        builder.setCancelable(false) // Prevent dismissing by tapping outside
        
        return builder.create()
    }
    
    fun setOnConsentCallback(callback: () -> Unit) {
        onConsentCallback = callback
    }
    
    companion object {
        const val TAG = "KVKKConsentDialog"
        
        /**
         * Check if KVKK consent dialog needs to be shown and show it if necessary
         * @param context The context
         * @param userPreferences UserPreferences instance
         * @param onConsentGranted Callback for when consent is granted
         * @return true if dialog was shown, false otherwise
         */
        fun showIfNeeded(
            context: Context, 
            fragmentManager: androidx.fragment.app.FragmentManager,
            userPreferences: UserPreferences,
            onConsentGranted: () -> Unit
        ): Boolean {
            // Check if user has already accepted KVKK
            if (userPreferences.hasAcceptedKVKK()) {
                return false
            }
            
            // Show the dialog
            val dialog = KVKKConsentDialog()
            dialog.setOnConsentCallback(onConsentGranted)
            dialog.show(fragmentManager, TAG)
            return true
        }
    }
} 