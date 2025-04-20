package com.example.barkodm.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.barkodm.R
import com.example.barkodm.data.model.Product
import com.example.barkodm.databinding.FragmentProductNewBinding
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductNewFragment : Fragment() {

    private var _binding: FragmentProductNewBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupListeners()
        setupObservers()
        setupFragmentResultListener()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupListeners() {
        binding.btnScanBarcode.setOnClickListener {
            findNavController().navigate(R.id.action_productNewFragment_to_barcodeScannerFragment)
        }

        binding.btnSave.setOnClickListener {
            saveProduct()
        }
    }

    private fun setupObservers() {
        viewModel.saveStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Ürün başarıyla kaydedildi", Toast.LENGTH_SHORT).show()
                findNavController().navigateUp()
            }
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.resetErrorMessage()
            }
        }
    }

    private fun setupFragmentResultListener() {
        setFragmentResultListener("barcode_key") { _, bundle ->
            val barcodeResult = bundle.getString("barcode_result")
            barcodeResult?.let {
                binding.edtBarcode.setText(it)
            }
        }
    }

    private fun saveProduct() {
        val barcode = binding.edtBarcode.text.toString().trim()
        val productCode = binding.edtProductCode.text.toString().trim()
        val description = binding.edtDescription.text.toString().trim()
        val unit = binding.edtUnit.text.toString().trim()
        val quantityText = binding.edtStockQuantity.text.toString().trim()

        if (barcode.isEmpty()) {
            binding.edtBarcode.error = "Barkod boş olamaz"
            return
        }

        if (productCode.isEmpty()) {
            binding.edtProductCode.error = "Ürün kodu boş olamaz"
            return
        }

        val stockQuantity = if (quantityText.isNotEmpty()) {
            try {
                quantityText.toDouble()
            } catch (e: NumberFormatException) {
                binding.edtStockQuantity.error = "Geçerli bir sayı giriniz"
                return
            }
        } else {
            0.0
        }

        val product = Product(
            id = 0, // SQLite otomatik ID atayacak
            barcode = barcode,
            code = productCode,
            description = description,
            unit = unit,
            stockQuantity = stockQuantity,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        viewModel.saveProduct(product)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 