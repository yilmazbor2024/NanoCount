package com.example.barkodm.ui.definitions

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.barkodm.R
import com.example.barkodm.databinding.FragmentLocationsBinding

class LocationsFragment : Fragment() {

    private var _binding: FragmentLocationsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupButtons()
    }
    
    private fun setupButtons() {
        binding.buttonBranches.setOnClickListener {
            findNavController().navigate(R.id.action_locations_to_branch_list)
        }
        
        binding.buttonWarehouses.setOnClickListener {
            findNavController().navigate(R.id.action_locations_to_warehouse_list)
        }
        
        binding.buttonLocations.setOnClickListener {
            findNavController().navigate(R.id.action_locations_to_location_list)
        }
        
        binding.buttonShelves.setOnClickListener {
            findNavController().navigate(R.id.action_locations_to_shelf_list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 