package com.example.it_telekom_app.viewmodels

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.it_telekom_app.models.AccountInfo
import com.example.it_telekom_app.models.PayToDate
import com.example.it_telekom_app.models.Services
import com.example.it_telekom_app.network.RetrofitInstance

class HomeViewModel : BaseViewModel() {
    var accountInfo by mutableStateOf<AccountInfo?>(null)
        private set

    fun loadAccountInfo(context: Context, token: String?) {
        fetchData(
            context = context,
            token = token,
            isInitialLoad = true,
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
            }
        }
    }

    fun refreshAccountInfo(context: Context, token: String?) {
        fetchData(
            context = context,
            token = token,
            isInitialLoad = false,
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
            }
        }
    }
}
