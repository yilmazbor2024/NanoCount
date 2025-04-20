package com.example.barkodm.ui.warehouse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barkodm.data.database.entity.WarehouseEntity
import com.example.barkodm.databinding.ItemWarehouseBinding

class WarehouseAdapter(
    private val onItemClick: (WarehouseEntity) -> Unit
) : ListAdapter<WarehouseEntity, WarehouseAdapter.ViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemWarehouseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val warehouse = getItem(position)
        holder.bind(warehouse)
    }

    inner class ViewHolder(private val binding: ItemWarehouseBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(getItem(position))
                }
            }
        }
        
        fun bind(warehouse: WarehouseEntity) {
            binding.apply {
                tvWarehouseName.text = warehouse.name
                tvWarehouseCode.text = warehouse.code
                tvBranchName.text = "Branch: ${warehouse.branchId}"
                tvWarehouseDescription.text = ""
            }
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<WarehouseEntity>() {
        override fun areItemsTheSame(oldItem: WarehouseEntity, newItem: WarehouseEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: WarehouseEntity, newItem: WarehouseEntity): Boolean {
            return oldItem == newItem
        }
    }
} 