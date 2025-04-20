package com.example.barkodm.ui.location

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barkodm.data.database.entity.BranchEntity
import com.example.barkodm.databinding.ItemBranchBinding

class BranchAdapter(private val onBranchClick: (BranchEntity) -> Unit) :
    ListAdapter<BranchEntity, BranchAdapter.BranchViewHolder>(BranchDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BranchViewHolder {
        val binding = ItemBranchBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BranchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BranchViewHolder, position: Int) {
        holder.bind(getItem(position), onBranchClick)
    }

    class BranchViewHolder(private val binding: ItemBranchBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(branch: BranchEntity, onBranchClick: (BranchEntity) -> Unit) {
            binding.apply {
                textBranchCode.text = branch.code
                textBranchName.text = branch.name
                
                root.setOnClickListener {
                    onBranchClick(branch)
                }
            }
        }
    }

    class BranchDiffCallback : DiffUtil.ItemCallback<BranchEntity>() {
        override fun areItemsTheSame(oldItem: BranchEntity, newItem: BranchEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: BranchEntity, newItem: BranchEntity): Boolean {
            return oldItem == newItem
        }
    }
} 