package com.example.barkodm.ui.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barkodm.data.database.entity.ShelfEntity
import com.example.barkodm.databinding.ItemShelfBinding

class ShelfAdapter(private val onShelfClick: (ShelfEntity) -> Unit) :
    ListAdapter<ShelfEntity, ShelfAdapter.ShelfViewHolder>(ShelfDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShelfViewHolder {
        val binding = ItemShelfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ShelfViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ShelfViewHolder, position: Int) {
        holder.bind(getItem(position), onShelfClick)
    }

    class ShelfViewHolder(private val binding: ItemShelfBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(shelf: ShelfEntity, onShelfClick: (ShelfEntity) -> Unit) {
            binding.apply {
                textShelfCode.text = shelf.code
                textShelfName.text = shelf.name
                
                root.setOnClickListener {
                    onShelfClick(shelf)
                }
            }
        }
    }

    class ShelfDiffCallback : DiffUtil.ItemCallback<ShelfEntity>() {
        override fun areItemsTheSame(oldItem: ShelfEntity, newItem: ShelfEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ShelfEntity, newItem: ShelfEntity): Boolean {
            return oldItem == newItem
        }
    }
} 