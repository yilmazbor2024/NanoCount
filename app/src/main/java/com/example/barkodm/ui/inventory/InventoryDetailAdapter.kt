package com.example.barkodm.ui.inventory

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barkodm.data.database.entity.InventoryDetailEntity
import com.example.barkodm.databinding.ItemInventoryDetailBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class InventoryDetailAdapter : ListAdapter<InventoryDetailEntity, InventoryDetailAdapter.ViewHolder>(InventoryDetailDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemInventoryDetailBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ViewHolder(private val binding: ItemInventoryDetailBinding) : RecyclerView.ViewHolder(binding.root) {
        
        private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        
        fun bind(detail: InventoryDetailEntity) {
            binding.textBarcode.text = detail.barcode
            binding.textProductCode.text = detail.productCode
            binding.textDescription.text = detail.description
            binding.textQuantity.text = "${detail.quantity} ${detail.unit}"
            binding.textDateTime.text = dateFormat.format(Date(detail.scannedAt))
        }
    }
}

class InventoryDetailDiffCallback : DiffUtil.ItemCallback<InventoryDetailEntity>() {
    override fun areItemsTheSame(oldItem: InventoryDetailEntity, newItem: InventoryDetailEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: InventoryDetailEntity, newItem: InventoryDetailEntity): Boolean {
        return oldItem == newItem
    }
} 