package com.example.barkodm.ui.definitions

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.example.barkodm.databinding.FragmentProductImportBinding
import com.google.android.material.button.MaterialButton

/**
 * Ürün import ekranı - CSV veya Excel dosyasından ürün yükleme
 */
class ProductImportFragment : Fragment() {

    private var _binding: FragmentProductImportBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductImportViewModel by viewModels {
        ProductImportViewModelFactory((requireActivity().application as BarkodApp).database.productDao())
    }
    
    private lateinit var adapter: ProductImportAdapter
    private var selectedFileUri: Uri? = null
    
    // Dosya seçim sonuç işleyicisi
    private val selectFileResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedFileUri = uri
                displaySelectedFile(uri)
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
        addTemplateButton()
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
        // Dosya seçme butonunu ayarla
        binding.selectFileButton.setOnClickListener {
            openFilePicker()
        }
        
        // Import butonu
        binding.importButton.setOnClickListener {
            selectedFileUri?.let { uri ->
                viewModel.importProducts(uri, requireContext().contentResolver)
            } ?: run {
                Toast.makeText(requireContext(), "Lütfen önce bir dosya seçin", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addTemplateButton() {
        // Şablon oluşturma butonu ekle
        val templateButton = MaterialButton(requireContext()).apply {
            text = "Şablon Oluştur"
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            setOnClickListener {
                viewModel.createTemplateFile(requireContext())
            }
        }
        
        // fileSelectLayout'a ekleme yap
        binding.fileSelectLayout.addView(templateButton)
    }
    
    private fun observeViewModel() {
        // Ürün listesi
        viewModel.parsedProducts.observe(viewLifecycleOwner) { products ->
            adapter.submitList(products)
            updateUI(products)
        }
        
        // İşlem durumu gözlemle
        viewModel.importStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is ProductImportViewModel.ImportStatus.Idle -> {
                    binding.progressBar.visibility = View.GONE
                    binding.importButton.isEnabled = true
                }
                is ProductImportViewModel.ImportStatus.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.importButton.isEnabled = false
                }
                is ProductImportViewModel.ImportStatus.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.importButton.isEnabled = true
                    Toast.makeText(
                        requireContext(), 
                        "${status.count} ürün başarıyla import edildi", 
                        Toast.LENGTH_LONG
                    ).show()
                    
                    // Başarılı import sonrası ürünler listesine dön
                    findNavController().navigateUp()
                }
                is ProductImportViewModel.ImportStatus.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.importButton.isEnabled = true
                    Toast.makeText(
                        requireContext(), 
                        "Hata: ${status.message}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        
        // Şablon oluşturma durumu gözlemle
        viewModel.templateCreationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is ProductImportViewModel.TemplateStatus.Success -> {
                    Toast.makeText(
                        requireContext(), 
                        "Şablon dosyası oluşturuldu: ${status.filePath}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
                is ProductImportViewModel.TemplateStatus.Error -> {
                    Toast.makeText(
                        requireContext(), 
                        "Şablon oluşturma hatası: ${status.message}", 
                        Toast.LENGTH_LONG
                    ).show()
                }
                else -> { /* İşlem yok */ }
            }
        }
    }
    
    private fun updateUI(products: List<com.example.barkodm.data.model.Product>) {
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
    
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "text/plain"
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "text/csv", 
                "text/comma-separated-values",
                "text/plain",
                "application/csv",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            ))
        }
        selectFileResultLauncher.launch(intent)
    }
    
    private fun displaySelectedFile(uri: Uri) {
        // Dosya adını göster
        requireContext().contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                if (nameIndex >= 0) {
                    val fileName = cursor.getString(nameIndex)
                    binding.filePathText.setText(fileName)
                    binding.importButton.isEnabled = true
                }
            }
        }
        
        // Dosyayı parse et ve ürünleri listele
        viewModel.parseFile(uri, requireContext().contentResolver)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 