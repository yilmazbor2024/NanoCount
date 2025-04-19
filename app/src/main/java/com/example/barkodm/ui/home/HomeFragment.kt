package com.example.barkodm.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: HomeViewModel by viewModels {
        HomeViewModelFactory((requireActivity().application as BarkodApp).database)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupObservers()
        setupListeners()
    }
    
    private fun setupObservers() {
        viewModel.openInventoryCount.observe(viewLifecycleOwner) { count ->
            binding.txtInventoryCount.text = count.toString()
        }
        
        viewModel.productCount.observe(viewLifecycleOwner) { count ->
            binding.txtProductCount.text = count.toString()
        }
    }
    
    private fun setupListeners() {
        binding.cardInventory.setOnClickListener {
            findNavController().navigate(R.id.navigation_inventory)
        }
        
        binding.cardNewInventory.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_inventory_new)
        }
        
        binding.cardProducts.setOnClickListener {
            findNavController().navigate(R.id.action_navigation_home_to_navigation_products)
        }
        
        binding.cardReports.setOnClickListener {
            findNavController().navigate(R.id.navigation_reports)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}