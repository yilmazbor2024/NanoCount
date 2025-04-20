package com.example.barkodm.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.barkodm.BarkodApp
import com.example.barkodm.databinding.FragmentWarehouseListBinding

class WarehouseListFragment : Fragment() {

    private var _binding: FragmentWarehouseListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LocationViewModel by viewModels {
        LocationViewModelFactory(
            warehouseDao = (requireActivity().application as BarkodApp).database.warehouseDao()
        )
    }
    
    private lateinit var adapter: WarehouseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWarehouseListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeWarehouses()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = WarehouseAdapter { warehouse ->
            // Navigate to edit fragment
            val action = WarehouseListFragmentDirections.actionWarehouseListToWarehouseEdit(warehouse.id)
            findNavController().navigate(action)
        }
        
        binding.recyclerViewWarehouses.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = this@WarehouseListFragment.adapter
        }
    }
    
    private fun setupListeners() {
        binding.fabAddWarehouse.setOnClickListener {
            // Navigate to add fragment
            val action = WarehouseListFragmentDirections.actionWarehouseListToWarehouseEdit(0)
            findNavController().navigate(action)
        }
    }
    
    private fun observeWarehouses() {
        // TODO: Implement branch selection functionality
        // For now, just show all warehouses from branch ID 1
        val branchId = 1
        
        viewModel.getWarehousesByBranch(branchId).observe(viewLifecycleOwner) { warehouses ->
            adapter.submitList(warehouses)
            
            // Show/hide empty state
            if (warehouses.isEmpty()) {
                binding.textEmptyState.visibility = View.VISIBLE
                binding.recyclerViewWarehouses.visibility = View.GONE
            } else {
                binding.textEmptyState.visibility = View.GONE
                binding.recyclerViewWarehouses.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 