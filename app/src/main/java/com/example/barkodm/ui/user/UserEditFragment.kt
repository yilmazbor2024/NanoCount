package com.example.barkodm.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.barkodm.BarkodApp
import com.example.barkodm.data.database.entity.UserEntity
import com.example.barkodm.databinding.FragmentUserEditBinding

class UserEditFragment : Fragment() {

    private var _binding: FragmentUserEditBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory((requireActivity().application as BarkodApp).database.userDao())
    }
    
    private var userId: Int = 0
    private var selectedRole: String = "user"
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserEditBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get the userId from arguments (used safe args pattern)
        arguments?.let {
            userId = it.getInt("userId", 0)
        }
        
        setupToolbar()
        setupRoleSpinner()
        setupListeners()
        setupObservers()
        
        if (userId > 0) {
            // Load user for editing
            viewModel.getUser(userId)
        }
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = if (userId > 0) "Kullanıcı Düzenle" else "Yeni Kullanıcı"
        
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRoleSpinner() {
        val roles = arrayOf("Kullanıcı", "Yönetici")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles)
        binding.spinnerRole.adapter = adapter
        
        binding.spinnerRole.setSelection(0) // Default is user
        
        binding.spinnerRole.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedRole = if (position == 0) "user" else "admin"
            }
            
            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                selectedRole = "user"
            }
        }
    }
    
    private fun setupListeners() {
        binding.buttonSaveUser.setOnClickListener {
            saveUser()
        }
    }
    
    private fun setupObservers() {
        viewModel.saveStatus.observe(viewLifecycleOwner) { saved ->
            if (saved) {
                Toast.makeText(requireContext(), "Kullanıcı kaydedildi", Toast.LENGTH_SHORT).show()
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
    
    private fun saveUser() {
        val username = binding.editTextUsername.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        
        if (username.isEmpty()) {
            binding.textInputLayoutUsername.error = "Kullanıcı adı boş olamaz"
            return
        }
        
        if (email.isEmpty()) {
            binding.textInputLayoutEmail.error = "E-posta boş olamaz"
            return
        }
        
        if (password.isEmpty() && userId == 0) {
            binding.textInputLayoutPassword.error = "Şifre boş olamaz"
            return
        }
        
        val user = UserEntity(
            id = if (userId > 0) userId else 0,
            username = username,
            email = email,
            role = selectedRole,
            password = password,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        
        viewModel.saveUser(user)
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 