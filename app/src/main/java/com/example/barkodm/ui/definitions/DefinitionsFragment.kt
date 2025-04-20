package com.example.barkodm.ui.definitions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barkodm.R
import com.example.barkodm.databinding.FragmentDefinitionsBinding

/**
 * Tanımlamalar ekranı - Ürün, lokasyon ve kullanıcı tanımlamalarına erişim sağlar
 */
class DefinitionsFragment : Fragment() {

    private var _binding: FragmentDefinitionsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDefinitionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupListeners()
    }
    
    private fun setupListeners() {
        // Ürün tanımları ekranına git
        binding.cardProducts.setOnClickListener {
            findNavController().navigate(R.id.action_definitions_to_products)
        }
        
        // Lokasyon tanımları ekranına git
        binding.cardLocations.setOnClickListener {
            findNavController().navigate(R.id.action_definitions_to_locations)
        }
        
        // Kullanıcı tanımları ekranına git
        binding.cardUsers.setOnClickListener {
            findNavController().navigate(R.id.action_definitions_to_users)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 