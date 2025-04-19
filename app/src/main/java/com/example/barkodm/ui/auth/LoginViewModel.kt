package com.example.barkodm.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.barkodm.data.database.dao.UserDao
import com.example.barkodm.data.model.User
import kotlinx.coroutines.launch

class LoginViewModel(private val userDao: UserDao) : ViewModel() {

    // Giriş sonucunu izlemek için LiveData
    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult
    
    // Aktif kullanıcı bilgisi
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser
    
    // Giriş işlemi sonuç sınıfı
    sealed class LoginResult {
        data class Success(val user: User) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
    
    /**
     * Kullanıcı giriş işlemini gerçekleştirir
     */
    fun login(username: String, password: String) {
        viewModelScope.launch {
            val user = userDao.login(username, password)
            
            if (user != null) {
                _currentUser.value = user.toUser()
                _loginResult.value = LoginResult.Success(user.toUser())
            } else {
                _loginResult.value = LoginResult.Error("Kullanıcı adı veya şifre hatalı")
            }
        }
    }
    
    /**
     * Kullanıcı çıkış işlemini gerçekleştirir
     */
    fun logout() {
        _currentUser.value = null
    }
}

/**
 * ViewModel Factory sınıfı
 */
class LoginViewModelFactory(private val userDao: UserDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LoginViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LoginViewModel(userDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
} 