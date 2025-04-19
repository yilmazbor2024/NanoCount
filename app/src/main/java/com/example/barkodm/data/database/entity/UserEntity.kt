package com.example.barkodm.data.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.barkodm.data.model.User

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val username: String,
    val password: String,
    val isAdmin: Boolean,
    val createdAt: Long
) {
    fun toUser(): User = User(
        id = id,
        username = username,
        password = password,
        isAdmin = isAdmin,
        createdAt = createdAt
    )

    companion object {
        fun fromUser(user: User): UserEntity = UserEntity(
            id = user.id,
            username = user.username,
            password = user.password,
            isAdmin = user.isAdmin,
            createdAt = user.createdAt
        )
    }
} 