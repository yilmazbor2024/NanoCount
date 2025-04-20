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
import com.example.barkodm.databinding.FragmentBranchListBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton

class BranchListFragment : Fragment() {

    private var _binding: FragmentBranchListBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: LocationViewModel by viewModels {
        LocationViewModelFactory((requireActivity().application as BarkodApp).database.branchDao())
    }
    
    private lateinit var adapter: BranchAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBranchListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeBranches()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = BranchAdapter { branch ->
            // Navigate to edit fragment
            val action = BranchListFragmentDirections.actionBranchListToBranchEdit(branch.id)
            findNavController().navigate(action)
        }
        
        binding.recyclerViewBranches.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = this@BranchListFragment.adapter
        }
    }
    
    private fun setupListeners() {
        binding.fabAddBranch.setOnClickListener {
            // Navigate to add fragment
            val action = BranchListFragmentDirections.actionBranchListToBranchEdit(0)
            findNavController().navigate(action)
        }
    }
    
    private fun observeBranches() {
        viewModel.allBranches.observe(viewLifecycleOwner) { branches ->
            adapter.submitList(branches)
            
            // Show/hide empty state
            if (branches.isEmpty()) {
                binding.textEmptyState.visibility = View.VISIBLE
                binding.recyclerViewBranches.visibility = View.GONE
            } else {
                binding.textEmptyState.visibility = View.GONE
                binding.recyclerViewBranches.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 