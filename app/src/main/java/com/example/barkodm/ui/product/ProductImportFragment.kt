package com.example.barkodm.ui.product

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.barkodm.BarkodApp
import com.example.barkodm.data.model.Product
import com.example.barkodm.databinding.FragmentProductImportBinding
import com.example.barkodm.ui.definitions.ProductImportViewModel
import com.example.barkodm.ui.definitions.ProductImportViewModelFactory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProductImportFragment : Fragment() {

    private var _binding: FragmentProductImportBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductImportViewModel by viewModels { 
        ProductImportViewModelFactory((requireActivity().application as BarkodApp).database.productDao())
    }
    private var selectedFileUri: Uri? = null
    private lateinit var adapter: ProductImportAdapter

    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                displayFileInfo(uri)
                parseFile(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductImportBinding.inflate(inflater, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProductImportAdapter()
        binding.productsList.adapter = adapter
        binding.productsList.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
    }

    private fun setupListeners() {
        binding.selectFileButton.setOnClickListener {
            openFilePicker()
        }

        binding.importButton.setOnClickListener {
            selectedFileUri?.let { uri ->
                viewModel.importProducts(uri, requireContext().contentResolver)
            } ?: run {
                Toast.makeText(requireContext(), "Lütfen önce bir dosya seçin", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun observeViewModel() {
        viewModel.parsedProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            updateUI(products)
        }

        viewModel.importStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is ProductImportViewModel.ImportStatus.Idle -> {
                    binding.progressBar.visibility = View.GONE
                }
                is ProductImportViewModel.ImportStatus.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                }
                is ProductImportViewModel.ImportStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "${status.count} ürün başarıyla içe aktarıldı", Toast.LENGTH_SHORT).show()
                    findNavController().navigateUp()
                }
                is ProductImportViewModel.ImportStatus.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), status.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun parseFile(uri: Uri) {
        viewModel.parseFile(uri, requireContext().contentResolver)
    }

    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                "text/csv"
            ))
        }
        filePickerLauncher.launch(intent)
    }

    private fun displayFileInfo(uri: Uri) {
        val cursor = requireContext().contentResolver.query(
            uri, null, null, null, null
        )
        
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val fileName = if (nameIndex >= 0) it.getString(nameIndex) else "Seçilen dosya"
                binding.filePathText.setText(fileName)
            }
        }
    }

    private fun updateUI(products: List<Product>) {
        binding.productCountText.text = "${products.size} ürün içe aktarılacak"
        binding.importButton.isEnabled = products.isNotEmpty()
        
        if (products.isEmpty() && selectedFileUri != null) {
            binding.emptyView.visibility = View.VISIBLE
            binding.productsList.visibility = View.GONE
            binding.emptyView.text = "Dosyada içe aktarılacak ürün bulunamadı"
        } else if (products.isNotEmpty()) {
            binding.emptyView.visibility = View.GONE
            binding.productsList.visibility = View.VISIBLE
        } else {
            binding.emptyView.visibility = View.VISIBLE
            binding.productsList.visibility = View.GONE
            binding.emptyView.text = "İçe aktarmak için dosya seçin"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 