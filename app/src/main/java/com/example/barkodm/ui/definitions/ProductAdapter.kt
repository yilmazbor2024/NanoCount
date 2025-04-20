package com.example.barkodm.ui.definitions

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barkodm.data.database.entity.ProductEntity
import com.example.barkodm.databinding.ItemProductBinding

/**
 * Ürün listesi için RecyclerView adapter'ı
 */
class ProductAdapter : ListAdapter<ProductEntity, ProductAdapter.ProductViewHolder>(ProductDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding = ItemProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ProductViewHolder(private val binding: ItemProductBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(product: ProductEntity) {
            binding.textBarcode.text = product.barcode
            binding.textCode.text = product.code
            binding.textDescription.text = product.description
            binding.textUnit.text = product.unit
            
            // Stok durumunu göster
            if (product.stockQuantity > 0) {
                binding.textStockInfo.text = "Stok: ${product.stockQuantity} ${product.unit}"
            } else {
                binding.textStockInfo.text = "Stok yok"
            }
        }
    }

    /**
     * RecyclerView için DiffUtil callback
     */
    class ProductDiffCallback : DiffUtil.ItemCallback<ProductEntity>() {
        override fun areItemsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ProductEntity, newItem: ProductEntity): Boolean {
            return oldItem == newItem
        }
    }
} 