package com.ittelekom.app.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.ittelekom.app.models.AccountInfo
import com.ittelekom.app.models.PayToDate
import com.ittelekom.app.models.Pays
import com.ittelekom.app.models.Services
import com.ittelekom.app.network.RetrofitInstance

class AccountViewModel(application: Application) : BaseViewModel(application) {
    var accountInfo by mutableStateOf<AccountInfo?>(null)
        private set

    var isDataLoaded by mutableStateOf(false)
        private set

    fun loadAccountInfo(state: State) {
        if (isDataLoaded && state != State.REFRESHING) return
        if (isDataLoaded && state != State.LOADING_ITEM) return

        fetchData(
            state = state,
            requests = listOf(
                { RetrofitInstance.api.getAccountInfo("Bearer ${getToken()}") },
                { RetrofitInstance.api.getPayToDate("Bearer ${getToken()}") },
                { RetrofitInstance.api.getServices("Bearer ${getToken()}") },
                { RetrofitInstance.api.getPays("Bearer ${getToken()}") }
            )
        ) { responses ->
            val accountInfoResponse = responses[0] as? AccountInfo
            val payToDateResponse = responses[1] as? PayToDate
            val servicesResponse = responses[2] as? Services
            val paysResponse = responses[3] as? Pays

            accountInfoResponse?.let {
                it.payToDate = payToDateResponse
                it.services = servicesResponse?.services ?: emptyList()
                it.pays = paysResponse?.pays ?: emptyList()
                accountInfo = it
                isDataLoaded = true
            }
        }
    }

    fun refreshAccountInfo() {
        isDataLoaded = false // Reset the flag to force data reload
        loadAccountInfo(state = State.LOADING)
    }

    fun pullToRefreshAccountInfo() {
        isDataLoaded = false // Reset the flag to force data reload
        loadAccountInfo(state = State.REFRESHING)
    }
}