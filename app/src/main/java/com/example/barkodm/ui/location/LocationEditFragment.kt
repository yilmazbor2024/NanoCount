package com.example.barkodm.ui.location

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
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.data.database.entity.LocationEntity
import com.example.barkodm.data.database.entity.WarehouseEntity
import com.example.barkodm.databinding.FragmentLocationEditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class LocationEditFragment : Fragment() {

    private var _binding: FragmentLocationEditBinding? = null
    private val binding get() = _binding!!
    
    private val args: LocationEditFragmentArgs by navArgs()
    
    private val viewModel: LocationViewModel by viewModels {
        LocationViewModelFactory(
            warehouseDao = (requireActivity().application as BarkodApp).database.warehouseDao(),
            locationDao = (requireActivity().application as BarkodApp).database.locationDao()
        )
    }
    
    private var isEditMode = false
    private var currentLocation: LocationEntity? = null
    private var warehouses: List<WarehouseEntity> = emptyList()
    private var selectedWarehouseId: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        isEditMode = args.locationId > 0
        selectedWarehouseId = if (arguments != null && arguments?.containsKey("warehouseId") == true) {
            arguments?.getInt("warehouseId") ?: 0
        } else {
            0
        }
        
        setupToolbar()
        setupWarehouseSpinner()
        setupListeners()
        
        if (isEditMode) {
            loadLocationData()
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.title = if (isEditMode) {
            getString(R.string.edit_location)
        } else {
            getString(R.string.add_location)
        }
        
        if (isEditMode) {
            binding.toolbar.inflateMenu(R.menu.menu_edit)
            binding.toolbar.setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_delete -> {
                        showDeleteConfirmationDialog()
                        true
                    }
                    else -> false
                }
            }
        }
    }
    
    private fun setupWarehouseSpinner() {
        // Observe warehouses from branch ID 1 (temporary)
        // In a real app, you would get warehouses from all branches or selected branch
        viewModel.getWarehousesByBranch(1).observe(viewLifecycleOwner) { warehouseList ->
            if (warehouseList.isNotEmpty()) {
                warehouses = warehouseList
                val warehouseNames = warehouseList.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, warehouseNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerWarehouse.adapter = adapter
                
                if (selectedWarehouseId > 0) {
                    // Set selected warehouse
                    val warehouseIndex = warehouseList.indexOfFirst { it.id == selectedWarehouseId }
                    if (warehouseIndex >= 0) {
                        binding.spinnerWarehouse.setSelection(warehouseIndex)
                    }
                } else if (isEditMode && currentLocation != null) {
                    // Set warehouse from current location
                    val warehouseIndex = warehouseList.indexOfFirst { it.id == currentLocation?.warehouseId }
                    if (warehouseIndex >= 0) {
                        binding.spinnerWarehouse.setSelection(warehouseIndex)
                    }
                }
                
                binding.spinnerWarehouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedWarehouseId = warehouseList[position].id
                    }
                    
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Do nothing
                    }
                }
            }
        }
    }
    
    private fun setupListeners() {
        binding.btnSaveLocation.setOnClickListener {
            saveLocation()
        }
    }
    
    private fun loadLocationData() {
        viewModel.getLocationById(args.locationId).observe(viewLifecycleOwner) { location ->
            if (location != null) {
                currentLocation = location
                with(binding) {
                    editTextLocationCode.setText(location.code)
                    editTextLocationName.setText(location.name)
                    selectedWarehouseId = location.warehouseId
                }
            } else {
                Toast.makeText(requireContext(), R.string.location_not_found, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }
    
    private fun saveLocation() {
        val code = binding.editTextLocationCode.text.toString().trim()
        val name = binding.editTextLocationName.text.toString().trim()
        
        // Validate inputs
        if (code.isEmpty()) {
            binding.textInputLayoutLocationCode.error = getString(R.string.field_required)
            return
        }
        
        if (name.isEmpty()) {
            binding.textInputLayoutLocationName.error = getString(R.string.field_required)
            return
        }
        
        if (selectedWarehouseId <= 0) {
            Toast.makeText(requireContext(), R.string.select_warehouse, Toast.LENGTH_SHORT).show()
            return
        }
        
        val location = if (isEditMode) {
            currentLocation?.copy(code = code, name = name, warehouseId = selectedWarehouseId)
                ?: LocationEntity(id = args.locationId, code = code, name = name, warehouseId = selectedWarehouseId)
        } else {
            LocationEntity(code = code, name = name, warehouseId = selectedWarehouseId)
        }
        
        viewModel.saveLocation(location) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    if (isEditMode) R.string.location_updated else R.string.location_added,
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), R.string.error_saving_location, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_location)
            .setMessage(R.string.delete_location_confirmation)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                deleteLocation()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
    
    private fun deleteLocation() {
        currentLocation?.let { location ->
            viewModel.deleteLocation(location) { success ->
                if (success) {
                    Toast.makeText(requireContext(), R.string.location_deleted, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), R.string.error_deleting_location, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 