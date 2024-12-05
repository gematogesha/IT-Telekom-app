package com.ittelekom.app.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ittelekom.app.models.AccountInfo
import com.ittelekom.app.models.PayToDate
import com.ittelekom.app.models.Services
import com.ittelekom.app.network.RetrofitInstance
import com.ittelekom.app.utils.TokenManager

class AccountViewModel(application: Application) : BaseViewModel(application) {

    private val _accountInfo = MutableLiveData<AccountInfo?>(null)
    val accountInfo: LiveData<AccountInfo?> = _accountInfo

    private val _isDataLoaded = MutableLiveData(false)
    val isDataLoaded: LiveData<Boolean> = _isDataLoaded

    // Загрузка данных аккаунта
    fun loadAccountInfo(forceReload: Boolean = false) {
        if (_isDataLoaded.value == true && !forceReload) {
            return
        }

        val context = getApplication<Application>().applicationContext
        val tokenManager = TokenManager.getInstance(context)
        val activeAccount = tokenManager.getActiveAccount()
        val token = activeAccount?.let { tokenManager.getToken(it) }

        if (token == null) {
            _errorMessage.value = "Пожалуйста, войдите снова."
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

            accountResponse?.let { account ->
                account.payToDate = payToDateResponse
                account.services = servicesResponse?.services ?: emptyList()
                _accountInfo.postValue(account)
                _isDataLoaded.postValue(true)
                Log.d("AccountViewModel", "AccountInfo: $account")
            }
        }
    }

    // Обновление данных аккаунта
    fun refreshAccountInfo() {
        _isDataLoaded.postValue(false)
        loadAccountInfo(forceReload = true)
    }
}