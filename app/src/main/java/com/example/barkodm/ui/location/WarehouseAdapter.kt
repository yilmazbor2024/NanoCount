package com.example.barkodm.ui.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barkodm.data.database.entity.WarehouseEntity
import com.example.barkodm.databinding.ItemWarehouseBinding

class WarehouseAdapter(private val onWarehouseClick: (WarehouseEntity) -> Unit) :
    ListAdapter<WarehouseEntity, WarehouseAdapter.WarehouseViewHolder>(WarehouseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WarehouseViewHolder {
        val binding = ItemWarehouseBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return WarehouseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: WarehouseViewHolder, position: Int) {
        holder.bind(getItem(position), onWarehouseClick)
    }

    class WarehouseViewHolder(private val binding: ItemWarehouseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(warehouse: WarehouseEntity, onWarehouseClick: (WarehouseEntity) -> Unit) {
            binding.apply {
                tvWarehouseCode.text = warehouse.code
                tvWarehouseName.text = warehouse.name
                
                root.setOnClickListener {
                    onWarehouseClick(warehouse)
                }
            }
        }
    }

    class WarehouseDiffCallback : DiffUtil.ItemCallback<WarehouseEntity>() {
        override fun areItemsTheSame(oldItem: WarehouseEntity, newItem: WarehouseEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WarehouseEntity, newItem: WarehouseEntity): Boolean {
            return oldItem == newItem
        }
    }
} 