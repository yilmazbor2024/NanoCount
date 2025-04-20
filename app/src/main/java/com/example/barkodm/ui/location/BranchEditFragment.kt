package com.example.barkodm.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.data.database.entity.BranchEntity
import com.example.barkodm.databinding.FragmentBranchEditBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class BranchEditFragment : Fragment() {

    private var _binding: FragmentBranchEditBinding? = null
    private val binding get() = _binding!!
    
    private val args: BranchEditFragmentArgs by navArgs()
    
    private val viewModel: LocationViewModel by viewModels {
        LocationViewModelFactory((requireActivity().application as BarkodApp).database.branchDao())
    }
    
    private var isEditMode = false
    private var currentBranch: BranchEntity? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBranchEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        isEditMode = args.branchId > 0
        
        setupToolbar()
        setupListeners()
        
        if (isEditMode) {
            loadBranchData()
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        binding.toolbar.title = if (isEditMode) {
            getString(R.string.edit_branch)
        } else {
            getString(R.string.add_branch)
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
    
    private fun setupListeners() {
        binding.btnSaveBranch.setOnClickListener {
            saveBranch()
        }
    }
    
    private fun loadBranchData() {
        viewModel.getBranchById(args.branchId).observe(viewLifecycleOwner) { branch ->
            if (branch != null) {
                currentBranch = branch
                with(binding) {
                    editTextBranchCode.setText(branch.code)
                    editTextBranchName.setText(branch.name)
                }
            } else {
                Toast.makeText(requireContext(), R.string.branch_not_found, Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }
    }
    
    private fun saveBranch() {
        val code = binding.editTextBranchCode.text.toString().trim()
        val name = binding.editTextBranchName.text.toString().trim()
        
        // Validate inputs
        if (code.isEmpty()) {
            binding.editTextBranchCode.error = getString(R.string.field_required)
            return
        }
        
        if (name.isEmpty()) {
            binding.editTextBranchName.error = getString(R.string.field_required)
            return
        }
        
        val branch = if (isEditMode) {
            currentBranch?.copy(code = code, name = name)
                ?: BranchEntity(id = args.branchId, code = code, name = name)
        } else {
            BranchEntity(code = code, name = name)
        }
        
        viewModel.saveBranch(branch) { success ->
            if (success) {
                Toast.makeText(
                    requireContext(),
                    if (isEditMode) R.string.branch_updated else R.string.branch_added,
                    Toast.LENGTH_SHORT
                ).show()
                findNavController().navigateUp()
            } else {
                Toast.makeText(requireContext(), R.string.error_saving_branch, Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showDeleteConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_branch)
            .setMessage(R.string.delete_branch_confirmation)
            .setPositiveButton(R.string.btn_delete) { _, _ ->
                deleteBranch()
            }
            .setNegativeButton(R.string.btn_cancel, null)
            .show()
    }
    
    private fun deleteBranch() {
        currentBranch?.let { branch ->
            viewModel.deleteBranch(branch) { success ->
                if (success) {
                    Toast.makeText(requireContext(), R.string.branch_deleted, Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(), R.string.error_deleting_branch, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 