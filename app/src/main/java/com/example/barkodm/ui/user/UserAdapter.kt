package com.example.barkodm.ui.user

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.barkodm.data.database.entity.UserEntity
import com.example.barkodm.databinding.ItemUserBinding

class UserAdapter(private val onUserClick: (UserEntity) -> Unit) : 
    ListAdapter<UserEntity, UserAdapter.UserViewHolder>(UserDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding, onUserClick)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class UserViewHolder(
        private val binding: ItemUserBinding,
        private val onUserClick: (UserEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(user: UserEntity) {
            binding.textUsername.text = user.username
            binding.textUserEmail.text = user.email
            binding.textUserRole.text = "Rol: ${user.role}"
            
            binding.root.setOnClickListener {
                onUserClick(user)
            }
        }
    }

    class UserDiffCallback : DiffUtil.ItemCallback<UserEntity>() {
        override fun areItemsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserEntity, newItem: UserEntity): Boolean {
            return oldItem == newItem
        }
    }
} 