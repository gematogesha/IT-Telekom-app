package com.example.it_telekom_app.viewmodels

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.it_telekom_app.models.AccountInfo
import com.example.it_telekom_app.network.RetrofitInstance
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AccountViewModel : ViewModel() {
    var accountInfo by mutableStateOf<AccountInfo?>(null)
        private set
    var isLoading by mutableStateOf(false)
        private set
    var isRefreshing by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun loadAccountInfo(token: String?) {
        fetchAccountInfo(token, true)
    }

    fun refreshAccountInfo(token: String?) {
        fetchAccountInfo(token, false)
    }

    private fun fetchAccountInfo(token: String?, isInitialLoad: Boolean) {
        if (token == null) {
            errorMessage = "Пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            if (isInitialLoad) {
                isLoading = true
            } else {
                isRefreshing = true
            }

            try {
                coroutineScope {
                    val accountDeferred = async { RetrofitInstance.api.getAccountInfo("Bearer $token") }
                    val payToDateDeferred = async { RetrofitInstance.api.getPayToDate("Bearer $token") }
                    val servicesDeferred = async { RetrofitInstance.api.getServices("Bearer $token") }

                    val accountResponse = accountDeferred.await()
                    val payToDateResponse = payToDateDeferred.await()
                    val servicesResponse = servicesDeferred.await()

                    if (accountResponse.isSuccessful && accountResponse.body() != null) {
                        val account = accountResponse.body()!!

                        if (payToDateResponse.isSuccessful && payToDateResponse.body() != null) {
                            account.payToDate = payToDateResponse.body()
                        }

                        if (servicesResponse.isSuccessful && servicesResponse.body() != null) {
                            account.services = servicesResponse.body()!!.services
                        } else {
                            account.services = emptyList()
                        }

                        accountInfo = account
                        errorMessage = null
                    } else {
                        Log.e("AccountViewModel", "Error fetching account info")
                        errorMessage = "Ошибка получения данных аккаунта"
                    }
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error fetching account info", e)
                errorMessage = "Ошибка получения данных аккаунта"
            }

            isLoading = false
            isRefreshing = false
        }
    }
}