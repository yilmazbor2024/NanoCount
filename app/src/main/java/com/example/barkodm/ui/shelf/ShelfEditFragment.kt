package com.example.barkodm.ui.shelf

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.data.database.entity.LocationEntity
import com.example.barkodm.data.database.entity.ShelfEntity
import com.example.barkodm.databinding.FragmentShelfEditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ShelfEditFragment : Fragment() {

    private var _binding: FragmentShelfEditBinding? = null
    private val binding get() = _binding!!
    
    private val args by lazy { 
        arguments?.let { ShelfEditFragmentArgs.fromBundle(it) } 
            ?: ShelfEditFragmentArgs()
    }
    
    private val viewModel: ShelfViewModel by viewModels {
        ShelfViewModelFactory(
            locationDao = (requireActivity().application as BarkodApp).database.locationDao(),
            shelfDao = (requireActivity().application as BarkodApp).database.shelfDao()
        )
    }
    
    private var isEditMode = false
    private var shelfId = 0
    private var currentShelf: ShelfEntity? = null
    private var locations: List<LocationEntity> = emptyList()
    private var selectedLocationId = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShelfEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Check if we're editing an existing shelf or creating a new one
        shelfId = args.shelfId
        isEditMode = shelfId > 0
        
        // Set the Location
        selectedLocationId = if (arguments != null && arguments?.containsKey("locationId") == true) {
            arguments?.getInt("locationId") ?: 0
        } else {
            args.locationId
        }
        
        setupToolbar()
        setupLocations()
        setupListeners()
        
        // Observe shelf data if in edit mode
        if (isEditMode) {
            loadShelfData()
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.title = if (isEditMode) {
            getString(R.string.edit_shelf)
        } else {
            getString(R.string.add_shelf)
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
    
    private fun setupLocations() {
        // Observe location data from the ViewModel
        lifecycleScope.launch {
            viewModel.locations.collectLatest { locationList ->
                if (locationList.isNotEmpty()) {
                    locations = locationList
                    val locationNames = locationList.map { it.name }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locationNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerLocation.adapter = adapter
                    
                    if (selectedLocationId > 0) {
                        // Set selected location
                        val locationIndex = locationList.indexOfFirst { it.id == selectedLocationId }
                        if (locationIndex >= 0) {
                            binding.spinnerLocation.setSelection(locationIndex)
                        }
                    }
                    
                    binding.spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                            selectedLocationId = locationList[position].id
                        }
                        
                        override fun onNothingSelected(parent: AdapterView<*>?) {
                            // Do nothing
                        }
                    }
                }
            }
        }
    }
    
    private fun setupListeners() {
        binding.btnSaveShelf.setOnClickListener {
            saveShelf()
        }
    }
    
    private fun loadShelfData() {
        // Get shelf data for editing
        viewModel.getShelfById(shelfId)
        
        // Observe the selected shelf
        lifecycleScope.launch {
            viewModel.selectedShelf.collectLatest { shelf ->
                if (shelf != null) {
                    currentShelf = shelf
                    with(binding) {
                        editTextShelfCode.setText(shelf.code)
                        editTextShelfName.setText(shelf.name)
                        selectedLocationId = shelf.locationId
                    }
                }
            }
        }
    }
    
    private fun saveShelf() {
        val code = binding.editTextShelfCode.text.toString().trim()
        val name = binding.editTextShelfName.text.toString().trim()
        
        // Validate inputs
        if (code.isEmpty()) {
            binding.textInputLayoutShelfCode.error = getString(R.string.field_required)
            return
        }
        
        if (name.isEmpty()) {
            binding.textInputLayoutShelfName.error = getString(R.string.field_required)
            return
        }
        
        if (selectedLocationId <= 0) {
            Toast.makeText(requireContext(), R.string.select_location, Toast.LENGTH_SHORT).show()
            return
        }
        
        val shelf = if (isEditMode) {
            currentShelf?.copy(code = code, name = name, locationId = selectedLocationId)
                ?: ShelfEntity(id = shelfId, code = code, name = name, locationId = selectedLocationId)
        } else {
            ShelfEntity(code = code, name = name, locationId = selectedLocationId)
        }
        
        viewModel.saveShelf(shelf) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    if (isEditMode) R.string.shelf_updated else R.string.shelf_added,
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), R.string.error_saving_shelf, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_shelf)
            .setMessage(R.string.delete_shelf_confirmation)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                deleteShelf()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
    
    private fun deleteShelf() {
        currentShelf?.let { shelf ->
            viewModel.deleteShelf(shelf) { success ->
                if (success) {
                    Toast.makeText(requireContext(), R.string.shelf_deleted, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), R.string.error_deleting_shelf, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 