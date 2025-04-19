package com.example.barkodm.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barkodm.data.database.entity.InventoryHeaderEntity
import com.example.barkodm.data.model.BarcodeReadMode
import com.example.barkodm.data.model.InventoryType
import com.example.barkodm.databinding.ItemInventoryBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryAdapter(private val listener: InventoryClickListener) : 
    ListAdapter<InventoryHeaderEntity, InventoryAdapter.InventoryViewHolder>(InventoryDiffCallback()) {

    interface InventoryClickListener {
        fun onInventoryClick(inventoryId: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val binding = ItemInventoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return InventoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class InventoryViewHolder(private val binding: ItemInventoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    listener.onInventoryClick(getItem(position).id)
                }
            }
        }

        fun bind(inventoryHeader: InventoryHeaderEntity) {
            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
            val date = Date(inventoryHeader.date)
            
            binding.textDate.text = dateFormat.format(date)
            
            // Sayım tipi
            val type = when (inventoryHeader.type) {
                InventoryType.CONTROLLED.name -> "Kontrollü Sayım"
                InventoryType.UNCONTROLLED.name -> "Kontrolsüz Sayım"
                else -> inventoryHeader.type
            }
            binding.textType.text = type
            
            // Okuma modu
            val readMode = when (inventoryHeader.readMode) {
                BarcodeReadMode.CONTINUOUS.name -> "Devamlı"
                BarcodeReadMode.QUANTITY_CORRECTION.name -> "Miktar Düzeltmeli"
                else -> inventoryHeader.readMode
            }
            
            // Lokasyon bilgisi (Şube, Depo, Lokasyon, Raf)
            binding.textLocation.text = "Şube ID: ${inventoryHeader.branchId}, Raf ID: ${inventoryHeader.shelfId}"
            
            // Durum
            val status = when (inventoryHeader.status) {
                "OPEN" -> "Açık"
                "COMPLETED" -> "Tamamlandı"
                "CANCELLED" -> "İptal Edildi"
                else -> inventoryHeader.status
            }
            binding.textStatus.text = status
            
            // Durum renklerini ayarla
            when (inventoryHeader.status) {
                "OPEN" -> {
                    binding.statusIndicator.setBackgroundResource(android.R.color.holo_green_dark)
                }
                "COMPLETED" -> {
                    binding.statusIndicator.setBackgroundResource(android.R.color.holo_blue_dark)
                }
                "CANCELLED" -> {
                    binding.statusIndicator.setBackgroundResource(android.R.color.holo_red_dark)
                }
            }
        }
    }

    class InventoryDiffCallback : DiffUtil.ItemCallback<InventoryHeaderEntity>() {
        override fun areItemsTheSame(oldItem: InventoryHeaderEntity, newItem: InventoryHeaderEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: InventoryHeaderEntity, newItem: InventoryHeaderEntity): Boolean {
            return oldItem == newItem
        }
    }
} 