package com.example.barkodm.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.databinding.FragmentInventoryBinding
import com.example.barkodm.ui.navigation.NavGraphDirections

class InventoryFragment : Fragment(), InventoryAdapter.InventoryClickListener {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: InventoryViewModel by viewModels {
        InventoryViewModelFactory((requireActivity().application as BarkodApp).database.inventoryHeaderDao())
    }
    
    private lateinit var adapter: InventoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupObservers()
        setupListeners()
    }
    
    private fun setupRecyclerView() {
        adapter = InventoryAdapter(this)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
    }
    
    private fun setupObservers() {
        viewModel.inventoryHeaders.observe(viewLifecycleOwner) { headers ->
            adapter.submitList(headers)
            
            // Eğer liste boşsa boş durumu göster, değilse listeyi göster
            if (headers.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerView.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupListeners() {
        binding.fabAddInventory.setOnClickListener {
            findNavController().navigate(R.id.action_inventory_to_inventory_new)
        }
    }

    override fun onInventoryClick(inventoryId: Int) {
        // Safe Args kullanarak inventoryId değerini geçiriyoruz
        val action = NavGraphDirections.actionGlobalNavigationInventoryDetail(inventoryId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 