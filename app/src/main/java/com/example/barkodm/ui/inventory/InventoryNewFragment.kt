package com.example.barkodm.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.data.database.entity.InventoryHeaderEntity
import com.example.barkodm.data.model.BarcodeReadMode
import com.example.barkodm.data.model.BarcodeReaderType
import com.example.barkodm.data.model.InventoryType
import com.example.barkodm.databinding.FragmentInventoryNewBinding

class InventoryNewFragment : Fragment() {

    private var _binding: FragmentInventoryNewBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: InventoryViewModel by viewModels {
        InventoryViewModelFactory((requireActivity().application as BarkodApp).database.inventoryHeaderDao())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryNewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupToolbar()
        setupSpinners()
        setupListeners()
    }
    
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }
    
    private fun setupSpinners() {
        // Sayım tipi spinner'ı
        val inventoryTypes = arrayOf("Kontrollü Sayım", "Kontrolsüz Sayım")
        val typeAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, inventoryTypes)
        typeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerInventoryType.adapter = typeAdapter
        
        // Şube spinner'ı
        val branches = arrayOf("Ana Depo", "Şube 1", "Şube 2", "Şube 3")
        val branchAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, branches)
        branchAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerBranch.adapter = branchAdapter
        
        // Lokasyon spinner'ı
        val locations = arrayOf("Tüm Lokasyonlar", "A Bölgesi", "B Bölgesi", "C Bölgesi")
        val locationAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, locations)
        locationAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerLocation.adapter = locationAdapter
    }
    
    private fun setupListeners() {
        binding.btnCreateInventory.setOnClickListener {
            createInventory()
        }
    }
    
    private fun createInventory() {
        // Kullanıcı girişlerini al
        val notes = binding.editTextNotes.text.toString().trim()
        val inventoryTypeName = binding.spinnerInventoryType.selectedItem.toString()
        val branchName = binding.spinnerBranch.selectedItem.toString()
        val locationName = binding.spinnerLocation.selectedItem.toString()
        
        // Değerleri sayısal indekslere çevir
        val inventoryType = when(inventoryTypeName) {
            "Kontrollü Sayım" -> InventoryType.CONTROLLED
            else -> InventoryType.UNCONTROLLED
        }
        
        // Şube ID'sini belirle
        val branchId = when(branchName) {
            "Ana Depo" -> 1
            "Şube 1" -> 2
            "Şube 2" -> 3
            "Şube 3" -> 4
            else -> 1
        }
        
        // Lokasyon ID'sini belirle
        val locationId = when(locationName) {
            "Tüm Lokasyonlar" -> 0
            "A Bölgesi" -> 1
            "B Bölgesi" -> 2
            "C Bölgesi" -> 3
            else -> 0
        }
        
        // Sayım başlığını oluştur - Entity sınıfındaki parametrelere uygun olarak
        val inventoryHeader = InventoryHeaderEntity(
            id = 0, // Room otomatik ID atayacak
            date = System.currentTimeMillis(),
            type = inventoryType.name,
            status = "OPEN",
            branchId = branchId,
            warehouseId = 1,
            locationId = locationId,
            shelfId = 0,
            readMode = BarcodeReadMode.CONTINUOUS.name,
            readerType = BarcodeReaderType.CAMERA.name,
            userId = 1, // Aktif kullanıcı ID
            createdAt = System.currentTimeMillis()
        )
        
        // ViewModel'e kaydet
        viewModel.createInventory(inventoryHeader) { success, id ->
            if (success && id > 0) {
                Toast.makeText(requireContext(), "Sayım oluşturuldu", Toast.LENGTH_SHORT).show()
                // ID ile sayım detay ekranına yönlendir
                val action = InventoryNewFragmentDirections.actionInventoryNewToInventoryScan(id.toInt())
                findNavController().navigate(action)
            } else {
                Toast.makeText(requireContext(), "Sayım oluşturulurken hata oluştu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 