package com.example.barkodm.ui.inventory

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.data.database.entity.InventoryDetailEntity
import com.example.barkodm.data.database.entity.ProductEntity
import com.example.barkodm.databinding.FragmentInventoryScanBinding
import com.example.barkodm.ui.barcode.BarcodeAnalyzer
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.barcode.BarcodeScanning
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class InventoryScanFragment : Fragment() {

    private var _binding: FragmentInventoryScanBinding? = null
    private val binding get() = _binding!!
    
    private val args: InventoryScanFragmentArgs by navArgs()
    private val viewModel: InventoryScanViewModel by viewModels {
        InventoryScanViewModelFactory(
            (requireActivity().application as BarkodApp).database.productDao(),
            (requireActivity().application as BarkodApp).database.inventoryHeaderDao(),
            (requireActivity().application as BarkodApp).database.inventoryDetailDao()
        )
    }
    
    private lateinit var cameraExecutor: ExecutorService
    private var isCameraActive = false
    private var lastScannedBarcode: String? = null
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Snackbar.make(
                binding.root,
                "Kamera izni olmadan barkod taranamaz",
                Snackbar.LENGTH_LONG
            ).show()
        }
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryScanBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d("InventoryScanFragment", "Fragment created with inventory ID: ${args.inventoryId}")
        
        // Envanter ID'sini ViewModel'e aktar
        viewModel.setInventoryId(args.inventoryId.toLong())
        
        setupToolbar()
        setupRecyclerView()
        setupListeners()
        setupObservers()
        
        cameraExecutor = Executors.newSingleThreadExecutor()
        
        // Başlangıçta ürün bilgisi panelini gizle
        binding.productInfo.visibility = View.GONE
    }
    
    private fun setupToolbar() {
        binding.toolbar.title = "Sayım #${args.inventoryId}"
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupRecyclerView() {
        val adapter = InventoryDetailAdapter()
        binding.recyclerViewScannedItems.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewScannedItems.adapter = adapter
        
        viewModel.inventoryDetails.observe(viewLifecycleOwner) { details ->
            Log.d("InventoryScanFragment", "Inventory details updated, count: ${details.size}")
            adapter.submitList(details)
            
            // Eğer liste boşsa boş durumu göster, değilse listeyi göster
            if (details.isEmpty()) {
                binding.emptyView.visibility = View.VISIBLE
                binding.recyclerViewScannedItems.visibility = View.GONE
            } else {
                binding.emptyView.visibility = View.GONE
                binding.recyclerViewScannedItems.visibility = View.VISIBLE
            }
        }
    }
    
    private fun setupListeners() {
        binding.btnScanBarcode.setOnClickListener {
            if (isCameraActive) {
                stopCamera()
            } else {
                checkCameraPermission()
            }
        }
        
        binding.btnManualEntry.setOnClickListener {
            val barcode = binding.edtBarcode.text.toString().trim()
            if (barcode.isNotEmpty()) {
                if (barcode.startsWith("P:")) {
                    // Ürün kodu ile arama
                    val productCode = barcode.substring(2).trim()
                    if (productCode.isNotEmpty()) {
                        viewModel.lookupProductByCode(productCode)
                    } else {
                        Toast.makeText(requireContext(), "Geçerli bir ürün kodu girin", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    // Barkod ile arama
                    processBarcode(barcode)
                }
                binding.edtBarcode.text?.clear()
            } else {
                Toast.makeText(requireContext(), "Lütfen bir barkod girin", Toast.LENGTH_SHORT).show()
            }
        }
        
        binding.btnFinish.setOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupObservers() {
        viewModel.productFound.observe(viewLifecycleOwner) { product ->
            if (product != null) {
                displayProduct(product)
            } else {
                binding.productInfo.visibility = View.GONE
            }
        }
        
        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            message?.let {
                Snackbar.make(binding.root, it, Snackbar.LENGTH_LONG).show()
                viewModel.resetErrorMessage()
            }
        }
        
        viewModel.scanSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                binding.edtQuantity.text?.clear()
                binding.productInfo.visibility = View.GONE
                lastScannedBarcode = null
                
                // Kamera modunda otomatik devam
                if (isCameraActive) {
                    Toast.makeText(requireContext(), "Ürün başarıyla eklendi", Toast.LENGTH_SHORT).show()
                    viewModel.resetScanSuccess()
                }
            }
        }
    }
    
    private fun checkCameraPermission() {
        val cameraPermission = Manifest.permission.CAMERA
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                cameraPermission
            ) == PackageManager.PERMISSION_GRANTED -> {
                // İzin var, kamera başlatılabilir
                startCamera()
            }
            shouldShowRequestPermissionRationale(cameraPermission) -> {
                Snackbar.make(
                    binding.root,
                    "Barkod tarama için kamera izni gereklidir",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("İzin Ver") {
                    requestPermissionLauncher.launch(cameraPermission)
                }.show()
            }
            else -> {
                requestPermissionLauncher.launch(cameraPermission)
            }
        }
    }
    
    private fun startCamera() {
        isCameraActive = true
        binding.previewView.visibility = View.VISIBLE
        binding.btnScanBarcode.text = "Kamerayı Kapat"
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            
            val imageAnalysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, BarcodeAnalyzer { barcode ->
                        val barcodeValue = barcode.rawValue
                        if (barcodeValue != null && barcodeValue != lastScannedBarcode) {
                            lastScannedBarcode = barcodeValue
                            requireActivity().runOnUiThread {
                                processBarcode(barcodeValue)
                            }
                        }
                    })
                }
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalysis
                )
            } catch (e: Exception) {
                Log.e("InventoryScanFragment", "Camera binding failed", e)
                Snackbar.make(binding.root, "Kamera başlatılamadı", Snackbar.LENGTH_LONG).show()
                stopCamera()
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    
    private fun stopCamera() {
        isCameraActive = false
        binding.previewView.visibility = View.GONE
        binding.btnScanBarcode.text = "Kamerayı Aç"
        
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(requireContext()))
    }
    
    private fun processBarcode(barcodeValue: String) {
        Log.d("InventoryScanFragment", "Processing barcode: $barcodeValue")
        viewModel.lookupProductByBarcode(barcodeValue)
    }
    
    private fun displayProduct(product: ProductEntity) {
        binding.productInfo.visibility = View.VISIBLE
        binding.txtBarcode.text = product.barcode
        binding.txtProductCode.text = product.code
        binding.txtDescription.text = product.description
        binding.txtUnit.text = product.unit
        
        // Clear any previous click listeners to prevent multiple calls
        binding.btnAddProduct.setOnClickListener(null)
        
        // Set new click listener
        binding.btnAddProduct.setOnClickListener {
            try {
                val quantityText = binding.edtQuantity.text.toString()
                val quantity = if (quantityText.isNotEmpty()) quantityText.toDoubleOrNull() ?: 1.0 else 1.0
                
                Log.d("InventoryScanFragment", "Adding product: ${product.barcode} with quantity: $quantity")
                viewModel.addInventoryDetail(product.barcode, quantity)
            } catch (e: Exception) {
                Log.e("InventoryScanFragment", "Error adding product", e)
                Toast.makeText(requireContext(), "Ürün eklenirken hata: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        cameraExecutor.shutdown()
        _binding = null
    }
} 