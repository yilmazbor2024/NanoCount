package com.example.barkodm.data.model

data class User(
    val id: Int,
    val username: String,
    val password: String,
    val isAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) 