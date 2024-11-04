package com.example.it_telekom_app.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.it_telekom_app.models.AccountInfo
import com.example.it_telekom_app.network.RetrofitInstance
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    var accountInfo by mutableStateOf<AccountInfo?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isRefreshing by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadAccountInfo(token: String?) {
        if (token == null) {
            errorMessage = "Токен отсутствует, пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val response = RetrofitInstance.api.getAccountInfo("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    accountInfo = response.body()
                    errorMessage = null
                } else {
                    errorMessage = "Ошибка получения данных аккаунта: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка сети: ${e.message}"
            }
            isLoading = false
        }
    }

    fun refreshAccountInfo(token: String?) {
        if (token == null) {
            errorMessage = "Токен отсутствует, пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            isRefreshing = true
            try {
                val response = RetrofitInstance.api.getAccountInfo("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    accountInfo = response.body()
                    errorMessage = null
                } else {
                    errorMessage = "Ошибка получения данных аккаунта: ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage = "Ошибка сети: ${e.message}"
            }
            isRefreshing = false
        }
    }
}