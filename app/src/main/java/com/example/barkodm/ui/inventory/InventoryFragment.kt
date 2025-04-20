package com.example.barkodm.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.databinding.FragmentInventoryBinding

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
        setupListeners()
        setupObservers()
    }
    
    private fun setupRecyclerView() {
        adapter = InventoryAdapter(this)
        binding.recyclerViewInventories.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewInventories.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.inventoryHeaders.observe(viewLifecycleOwner) { inventories ->
            adapter.submitList(inventories)
            
            // Show/hide empty state
            if (inventories.isEmpty()) {
                binding.textNoInventories.visibility = View.VISIBLE
                binding.recyclerViewInventories.visibility = View.GONE
            } else {
                binding.textNoInventories.visibility = View.GONE
                binding.recyclerViewInventories.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupListeners() {
        binding.fabAddInventory.setOnClickListener {
            findNavController().navigate(R.id.action_inventory_to_inventory_new)
        }
    }
    
    override fun onInventoryClick(inventoryId: Int) {
        val action = InventoryFragmentDirections.actionInventoryToInventoryDetail(inventoryId)
        findNavController().navigate(action)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 