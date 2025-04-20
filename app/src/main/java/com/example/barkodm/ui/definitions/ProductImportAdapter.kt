package com.example.barkodm.ui.definitions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barkodm.data.model.Product
import com.example.barkodm.databinding.ItemProductImportBinding

class ProductImportAdapter : ListAdapter<Product, ProductImportAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductImportBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, 
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(private val binding: ItemProductImportBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(product: Product) {
            binding.product = product
            binding.executePendingBindings()
        }
    }

    private class ProductDiffCallback : DiffUtil.ItemCallback<Product>() {
        override fun areItemsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem.barcode == newItem.barcode
        }

        override fun areContentsTheSame(oldItem: Product, newItem: Product): Boolean {
            return oldItem == newItem
        }
    }
} 