package com.example.barkodm.ui.definitions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.databinding.FragmentUsersBinding
import com.example.barkodm.ui.user.UserAdapter
import com.example.barkodm.ui.user.UserViewModel
import com.example.barkodm.ui.user.UserViewModelFactory
import com.google.android.material.snackbar.Snackbar

class UsersFragment : Fragment() {

    private var _binding: FragmentUsersBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory((requireActivity().application as BarkodApp).database.userDao())
    }
    
    private lateinit var adapter: UserAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUsersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        adapter = UserAdapter { user ->
            // Navigate to user edit
            val bundle = Bundle().apply {
                putInt("userId", user.id)
            }
            findNavController().navigate(R.id.action_users_to_user_edit, bundle)
        }
        
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
            adapter = this@UsersFragment.adapter
        }
    }
    
    private fun setupListeners() {
        binding.fabAddUser.setOnClickListener {
            val bundle = Bundle().apply {
                putInt("userId", 0) // 0 means new user
            }
            findNavController().navigate(R.id.action_users_to_user_edit, bundle)
        }
    }
    
    private fun setupObservers() {
        viewModel.users.observe(viewLifecycleOwner) { users ->
            Log.d("UsersFragment", "Users updated: ${users.size}")
            adapter.submitList(users)
            
            if (users.isEmpty()) {
                binding.textEmptyState.visibility = View.VISIBLE
                binding.recyclerViewUsers.visibility = View.GONE
            } else {
                binding.textEmptyState.visibility = View.GONE
                binding.recyclerViewUsers.visibility = View.VISIBLE
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.resetErrorMessage()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 