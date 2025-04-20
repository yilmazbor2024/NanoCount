package com.example.barkodm.ui.inventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.barkodm.BarkodApp
import com.example.barkodm.R
import com.example.barkodm.databinding.FragmentInventoryDetailBinding

class InventoryDetailFragment : Fragment() {

    private var _binding: FragmentInventoryDetailBinding? = null
    private val binding get() = _binding!!
    
    // Fragment arguments - SafeArgs
    private val args: InventoryDetailFragmentArgs by navArgs()
    
    // ViewModel
    private val viewModel: InventoryDetailViewModel by viewModels {
        InventoryDetailViewModelFactory(
            (requireActivity().application as BarkodApp).database.inventoryHeaderDao(),
            (requireActivity().application as BarkodApp).database.inventoryDetailDao()
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryDetailBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Fragment argümanlarından envanter ID'sini al
        val inventoryId = args.inventoryId.toLong()
        
        // ViewModel'e envanter ID'sini aktar
        viewModel.setInventoryId(inventoryId)
        
        setupUi()
        setupObservers()
    }
    
    private fun setupUi() {
        // Geri butonu
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
        
        // Durum değiştirme butonları
        binding.btnComplete.setOnClickListener {
            viewModel.updateInventoryStatus("COMPLETED")
        }
        
        binding.btnCancel.setOnClickListener {
            viewModel.updateInventoryStatus("CANCELLED")
        }
    }
    
    private fun setupObservers() {
        // Envanter başlığını gözlemle
        viewModel.inventoryHeader.observe(viewLifecycleOwner) { header ->
            header?.let {
                binding.textTitle.text = "Sayım #${it.id}"
                binding.textDate.text = "Tarih: ${android.text.format.DateFormat.format("dd/MM/yyyy HH:mm", it.date)}"
                binding.textLocation.text = "Lokasyon: Şube ${it.branchId}, Raf ${it.shelfId}"
                
                // Sayım durumuna göre butonların görünürlüğünü ayarla
                val isOpen = it.status == "OPEN"
                binding.btnComplete.isEnabled = isOpen
                binding.btnCancel.isEnabled = isOpen
                
                // Durum görünümü
                when (it.status) {
                    "OPEN" -> {
                        binding.statusIndicator.setBackgroundResource(android.R.color.holo_green_dark)
                        binding.textStatus.text = "Açık"
                    }
                    "COMPLETED" -> {
                        binding.statusIndicator.setBackgroundResource(android.R.color.holo_blue_dark)
                        binding.textStatus.text = "Tamamlandı"
                    }
                    "CANCELLED" -> {
                        binding.statusIndicator.setBackgroundResource(android.R.color.holo_red_dark)
                        binding.textStatus.text = "İptal Edildi"
                    }
                }
            }
        }
        
        // Envanter detaylarını gözlemle
        viewModel.inventoryDetails.observe(viewLifecycleOwner) { details ->
            // RecyclerView adapter'ını güncelle
            // TODO: Adapter oluştur ve bağla
        }
        
        // Toplam miktarı gözlemle
        viewModel.totalQuantity.observe(viewLifecycleOwner) { quantity ->
            binding.textTotalQuantity.text = "Toplam Miktar: $quantity"
        }
        
        // Ürün sayısını gözlemle
        viewModel.uniqueProductCount.observe(viewLifecycleOwner) { count ->
            binding.textProductCount.text = "Farklı Ürün: $count"
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 