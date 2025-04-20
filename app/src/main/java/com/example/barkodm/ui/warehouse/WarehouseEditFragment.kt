package com.example.barkodm.ui.warehouse

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.barkodm.R
import com.example.barkodm.databinding.FragmentWarehouseEditBinding
import com.example.barkodm.data.database.entity.BranchEntity
import com.example.barkodm.data.database.entity.WarehouseEntity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WarehouseEditFragment : Fragment() {

    private var _binding: FragmentWarehouseEditBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: WarehouseViewModel by viewModels()
    
    // Use a manually created bundle instead of the navArgs()
    private var warehouseId: Long = -1
    
    private var selectedBranchId: Long = -1
    private var branches = listOf<BranchEntity>()
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWarehouseEditBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get the warehouseId from arguments
        warehouseId = arguments?.getLong("warehouseId") ?: -1L
        
        setupToolbar()
        setupObservers()
        loadBranches()
        
        if (warehouseId > 0) {
            viewModel.getWarehouse(warehouseId)
        }
        
        binding.buttonSaveWarehouse.setOnClickListener {
            saveWarehouse()
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = if (warehouseId > 0) {
            getString(R.string.edit_warehouse)
        } else {
            getString(R.string.add_warehouse)
        }
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupObservers() {
        viewModel.warehouse.observe(viewLifecycleOwner) { warehouse ->
            warehouse?.let {
                binding.editTextWarehouseName.setText(it.name)
                // Use null-safe access for description
                binding.editTextWarehouseDescription.setText(it.description ?: "")
                selectedBranchId = it.branchId.toLong()
                updateBranchSpinner()
            }
        }
        
        viewModel.saveStatus.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                Toast.makeText(requireContext(), R.string.warehouse_updated, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_LONG).show()
                viewModel.resetErrorMessage()
            }
        }
    }
    
    private fun loadBranches() {
        viewModel.branches.observe(viewLifecycleOwner) { branchList ->
            branches = branchList
            updateBranchSpinner()
        }
    }
    
    private fun updateBranchSpinner() {
        val branchNames = branches.map { it.name }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, branchNames)
        binding.spinnerBranch.adapter = adapter
        
        if (selectedBranchId > 0) {
            val selectedIndex = branches.indexOfFirst { it.id.toLong() == selectedBranchId }
            if (selectedIndex >= 0) {
                binding.spinnerBranch.setSelection(selectedIndex)
            }
        }
        
        binding.spinnerBranch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedBranchId = branches[position].id.toLong()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedBranchId = -1
            }
        }
    }
    
    private fun saveWarehouse() {
        val name = binding.editTextWarehouseName.text.toString().trim()
        val description = binding.editTextWarehouseDescription.text.toString().trim()
        
        if (name.isEmpty()) {
            binding.textInputLayoutWarehouseName.error = getString(R.string.field_required)
            return
        }
        
        if (selectedBranchId <= 0) {
            Toast.makeText(requireContext(), R.string.field_required, Toast.LENGTH_SHORT).show()
            return
        }
        
        val warehouse = WarehouseEntity(
            id = if (warehouseId > 0) warehouseId.toInt() else 0,
            name = name,
            code = "", // Add a default value for code
            branchId = selectedBranchId.toInt(),
            description = description
        )
        
        viewModel.saveWarehouse(warehouse)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 