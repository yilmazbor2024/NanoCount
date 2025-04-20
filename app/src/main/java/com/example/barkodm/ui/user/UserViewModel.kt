package com.example.barkodm.ui.user

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barkodm.data.database.dao.UserDao
import com.example.barkodm.data.database.entity.UserEntity
import kotlinx.coroutines.launch

class UserViewModel(private val userDao: UserDao) : ViewModel() {
    
    val users = userDao.getAllUsers()
    
    private val _saveStatus = MutableLiveData<Boolean>()
    val saveStatus: LiveData<Boolean> = _saveStatus
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    fun saveUser(user: UserEntity) {
        viewModelScope.launch {
            try {
                Log.d("UserViewModel", "Saving user: ${user.username}")
                
                val id = userDao.insert(user)
                Log.d("UserViewModel", "User saved with ID: $id")
                
                _saveStatus.postValue(true)
            } catch (e: Exception) {
                Log.e("UserViewModel", "Error saving user", e)
                _errorMessage.postValue("Kullanıcı kaydedilemedi: ${e.message}")
                _saveStatus.postValue(false)
            }
        }
    }
    
    fun getUser(id: Int) {
        viewModelScope.launch {
            try {
                val user = userDao.getUserById(id)
                // Handle user data if needed
            } catch (e: Exception) {
                _errorMessage.postValue("Kullanıcı bilgileri alınamadı: ${e.message}")
            }
        }
    }
    
    fun deleteUser(id: Int) {
        viewModelScope.launch {
            try {
                userDao.deleteUserById(id)
            } catch (e: Exception) {
                _errorMessage.postValue("Kullanıcı silinemedi: ${e.message}")
            }
        }
    }
    
    fun resetErrorMessage() {
        _errorMessage.value = null
    }
}

class UserViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 