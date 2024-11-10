package com.example.it_telekom_app.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.it_telekom_app.models.AccountInfo
import com.example.it_telekom_app.models.PayToDate
import com.example.it_telekom_app.models.Services
import com.example.it_telekom_app.network.RetrofitInstance
import com.example.it_telekom_app.utils.TokenManager

class HomeViewModel(application: Application) : BaseViewModel(application) {
    var accountInfo by mutableStateOf<AccountInfo?>(null)
        private set
    var isDataLoaded by mutableStateOf(false)

    fun loadAccountInfo(forceReload: Boolean = false) {
        if (isDataLoaded && !forceReload) {
            return
        }

        val context = getApplication<Application>().applicationContext
        val tokenManager = TokenManager.getInstance(context)
        val activeAccount = tokenManager.getActiveAccount()
        val token = activeAccount?.let { tokenManager.getToken(it) }

        if (token == null) {
            errorMessage = "Пожалуйста, войдите снова."
            return
        }

        fetchData(
            isInitialLoad = !forceReload,
            requests = listOf(
                { RetrofitInstance.api.getAccountInfo("Bearer $token") },
                { RetrofitInstance.api.getPayToDate("Bearer $token") },
                { RetrofitInstance.api.getServices("Bearer $token") }
            )
        ) { responses ->
            val accountResponse = responses[0] as? AccountInfo
            val payToDateResponse = responses[1] as? PayToDate
            val servicesResponse = responses[2] as? Services

            accountResponse?.let {
                it.payToDate = payToDateResponse
                it.services = servicesResponse?.services ?: emptyList()
                accountInfo = it
                isDataLoaded = true // Set flag to true after data is loaded
            }
        }
    }

    fun refreshAccountInfo() {
        isDataLoaded = false // Reset the flag to force data reload
        loadAccountInfo(forceReload = true)
    }
}