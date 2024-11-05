package com.example.it_telekom_app.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.it_telekom_app.models.AccountInfo
import com.example.it_telekom_app.network.RetrofitInstance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AccountViewModel : ViewModel() {
    var accountInfo by mutableStateOf<AccountInfo?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isRefreshing by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadAccountInfo(token: String?) = fetchAccountInfo(token, true)

    fun refreshAccountInfo(token: String?) = fetchAccountInfo(token, false)

    private fun fetchAccountInfo(token: String?, isInitialLoad: Boolean) {
        if (token == null) {
            errorMessage = "Пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            if (isInitialLoad) isLoading = true else isRefreshing = true
            try {
                val accountResponse = withContext(Dispatchers.IO) { RetrofitInstance.api.getAccountInfo("Bearer $token") }
                val payToDateResponse = withContext(Dispatchers.IO) { RetrofitInstance.api.getPayToDate("Bearer $token") }
                val servicesResponse = withContext(Dispatchers.IO) { RetrofitInstance.api.getServices("Bearer $token") }

                if (accountResponse.isSuccessful && accountResponse.body() != null) {
                    val account = accountResponse.body()!!
                    account.payToDate = payToDateResponse.body()
                    account.services = servicesResponse.body()?.services ?: emptyList()
                    accountInfo = account
                    errorMessage = null
                } else {
                    handleFetchError(isInitialLoad)
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error fetching account info", e)
                handleFetchError(isInitialLoad)
            } finally {
                delay(500)
                if (isInitialLoad) isLoading = false else isRefreshing = false
            }
        }
    }

    private fun handleFetchError(isInitialLoad: Boolean) {
        Log.e("AccountViewModel", "Error fetching account info")
        errorMessage = if (isInitialLoad) "Ошибка получения данных аккаунта" else "Ошибка обновления данных аккаунта"
    }
}
