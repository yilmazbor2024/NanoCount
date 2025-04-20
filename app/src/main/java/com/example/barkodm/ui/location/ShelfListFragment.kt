package com.example.barkodm.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.data.database.entity.LocationEntity
import com.example.barkodm.data.database.entity.WarehouseEntity
import com.example.barkodm.databinding.FragmentShelfListBinding

class ShelfListFragment : Fragment() {

    private var _binding: FragmentShelfListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LocationViewModel by viewModels {
        LocationViewModelFactory(
            branchDao = (requireActivity().application as BarkodApp).database.branchDao(),
            warehouseDao = (requireActivity().application as BarkodApp).database.warehouseDao(),
            locationDao = (requireActivity().application as BarkodApp).database.locationDao(),
            shelfDao = (requireActivity().application as BarkodApp).database.shelfDao()
        )
    }
    
    private lateinit var adapter: ShelfAdapter
    private var selectedBranchId: Int = 1 // Default branch
    private var selectedWarehouseId: Int = 0
    private var selectedLocationId: Int = 0
    private var warehouses: List<WarehouseEntity> = emptyList()
    private var locations: List<LocationEntity> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentShelfListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupSpinners()
        setupListeners()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = ShelfAdapter { shelf ->
            // Navigate to edit fragment
            val action = ShelfListFragmentDirections.actionShelfListToShelfEdit(shelf.id)
            findNavController().navigate(action)
        }
        
        binding.recyclerViewShelves.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = this@ShelfListFragment.adapter
        }
    }
    
    private fun setupSpinners() {
        // Branch spinner
        viewModel.allBranches.observe(viewLifecycleOwner) { branches ->
            if (branches.isNotEmpty()) {
                val branchNames = branches.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, branchNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerBranch.adapter = adapter
                
                binding.spinnerBranch.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedBranchId = branches[position].id
                        loadWarehouses()
                    }
                    
                    override fun onNothingSelected(parent: AdapterView<*>?) {
                        // Nothing to do
                    }
                }
                
                // Set first branch as default selection
                binding.spinnerBranch.setSelection(0)
            }
        }
        
        // Warehouse spinner
        binding.spinnerWarehouse.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (warehouses.isNotEmpty() && position < warehouses.size) {
                    selectedWarehouseId = warehouses[position].id
                    loadLocations()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nothing to do
            }
        }
        
        // Location spinner
        binding.spinnerLocation.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (locations.isNotEmpty() && position < locations.size) {
                    selectedLocationId = locations[position].id
                    loadShelves()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Nothing to do
            }
        }
    }
    
    private fun loadWarehouses() {
        viewModel.getWarehousesByBranch(selectedBranchId).observe(viewLifecycleOwner) { warehouseList ->
            warehouses = warehouseList
            
            if (warehouseList.isNotEmpty()) {
                binding.layoutWarehouseSpinner.visibility = View.VISIBLE
                val warehouseNames = warehouseList.map { it.name }
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, warehouseNames)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerWarehouse.adapter = adapter
                
                // Set first warehouse as default selection
                binding.spinnerWarehouse.setSelection(0)
                selectedWarehouseId = warehouseList[0].id
                loadLocations()
            } else {
                binding.layoutWarehouseSpinner.visibility = View.GONE
                binding.layoutLocationSpinner.visibility = View.GONE
                selectedWarehouseId = 0
                selectedLocationId = 0
                adapter.submitList(emptyList())
                binding.textEmptyState.text = getString(R.string.no_warehouses_for_branch)
                binding.textEmptyState.visibility = View.VISIBLE
                binding.recyclerViewShelves.visibility = View.GONE
            }
        }
    }
    
    private fun loadLocations() {
        if (selectedWarehouseId > 0) {
            viewModel.getLocationsByWarehouse(selectedWarehouseId).observe(viewLifecycleOwner) { locationList ->
                locations = locationList
                
                if (locationList.isNotEmpty()) {
                    binding.layoutLocationSpinner.visibility = View.VISIBLE
                    val locationNames = locationList.map { it.name }
                    val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locationNames)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerLocation.adapter = adapter
                    
                    // Set first location as default selection
                    binding.spinnerLocation.setSelection(0)
                    selectedLocationId = locationList[0].id
                    loadShelves()
                } else {
                    binding.layoutLocationSpinner.visibility = View.GONE
                    selectedLocationId = 0
                    adapter.submitList(emptyList())
                    binding.textEmptyState.text = getString(R.string.no_locations_for_warehouse)
                    binding.textEmptyState.visibility = View.VISIBLE
                    binding.recyclerViewShelves.visibility = View.GONE
                }
            }
        } else {
            binding.layoutLocationSpinner.visibility = View.GONE
            selectedLocationId = 0
            adapter.submitList(emptyList())
        }
    }
    
    private fun loadShelves() {
        if (selectedLocationId > 0) {
            viewModel.getShelvesByLocation(selectedLocationId).observe(viewLifecycleOwner) { shelves ->
                adapter.submitList(shelves)
                
                // Show/hide empty state
                if (shelves.isEmpty()) {
                    binding.textEmptyState.text = getString(R.string.no_shelves_added)
                    binding.textEmptyState.visibility = View.VISIBLE
                    binding.recyclerViewShelves.visibility = View.GONE
                } else {
                    binding.textEmptyState.visibility = View.GONE
                    binding.recyclerViewShelves.visibility = View.VISIBLE
                }
            }
        } else {
            adapter.submitList(emptyList())
            binding.textEmptyState.text = getString(R.string.select_location)
            binding.textEmptyState.visibility = View.VISIBLE
            binding.recyclerViewShelves.visibility = View.GONE
        }
    }
    
    private fun setupListeners() {
        binding.fabAddShelf.setOnClickListener {
            if (selectedLocationId > 0) {
                // Navigate to add fragment with selected location
                val action = ShelfListFragmentDirections.actionShelfListToShelfEdit(
                    shelfId = 0
                )
                // The locationId will be passed via a bundle
                val bundle = action.arguments
                bundle.putInt("locationId", selectedLocationId)
                findNavController().navigate(action.actionId, bundle)
            } else {
                // Show error message
                if (locations.isEmpty() && selectedWarehouseId > 0) {
                    // No locations available
                    // Ask to create a location first
                    findNavController().navigate(R.id.navigation_location_list)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 