package com.example.barkodm.ui.definitions

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.data.repository.ProductRepository
import com.example.barkodm.databinding.FragmentProductsBinding
import com.example.barkodm.ui.product.ProductViewModel
import com.google.android.material.snackbar.Snackbar

/**
 * Ürün listesi ve yönetimi ekranı
 */
class ProductsFragment : Fragment() {

    private var _binding: FragmentProductsBinding? = null
    private val binding get() = _binding!!
    
    private lateinit var adapter: ProductAdapter
    
    private val viewModel: ProductViewModel by viewModels {
        ProductsViewModelFactory((requireActivity().application as BarkodApp).productRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupListeners()
        setupObservers()
    }
    
    private fun setupRecyclerView() {
        adapter = ProductAdapter()
        binding.recyclerViewProducts.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewProducts.adapter = adapter
    }
    
    private fun setupObservers() {
        viewModel.products.observe(viewLifecycleOwner) { products ->
            Log.d("ProductsFragment", "Products updated: ${products.size}")
            
            if (products.isEmpty()) {
                binding.textEmptyProducts.visibility = View.VISIBLE
                binding.recyclerViewProducts.visibility = View.GONE
            } else {
                binding.textEmptyProducts.visibility = View.GONE
                binding.recyclerViewProducts.visibility = View.VISIBLE
                // Convert Product to ProductEntity for the adapter
                val entities = products.map {
                    com.example.barkodm.data.database.entity.ProductEntity(
                        id = it.id,
                        barcode = it.barcode,
                        code = it.code,
                        description = it.description,
                        unit = it.unit,
                        stockQuantity = it.stockQuantity,
                        createdAt = it.createdAt,
                        updatedAt = it.updatedAt
                    )
                }
                adapter.submitList(entities)
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.resetErrorMessage()
            }
        }
    }
    
    private fun setupListeners() {
        // Yeni ürün ekleme ekranına git
        binding.fabAddProduct.setOnClickListener {
            findNavController().navigate(R.id.action_products_to_new)
        }
        
        // Toplu ürün import etme ekranına git
        binding.fabImportProducts.setOnClickListener {
            findNavController().navigate(R.id.action_products_to_import)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    
    /**
     * ProductViewModel için factory
     */
    class ProductsViewModelFactory(private val productRepository: ProductRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return ProductViewModel(productRepository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
} 