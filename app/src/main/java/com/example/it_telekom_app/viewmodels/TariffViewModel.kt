package com.example.it_telekom_app.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.example.it_telekom_app.models.Tariffs
import com.example.it_telekom_app.network.RetrofitInstance
import com.example.it_telekom_app.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TariffViewModel(application: Application) : BaseViewModel(application) {
    var tariffs by mutableStateOf<Tariffs?>(null)
        private set
    var tariffChangeMessage by mutableStateOf<String?>(null)
        private set
    var isTariffChangeSuccessful by mutableStateOf(false)
        private set

    fun loadTariffInfo(isInitialLoad: Boolean = true) {
        val context = getApplication<Application>().applicationContext
        val tokenManager = TokenManager.getInstance(context)
        val activeAccount = tokenManager.getActiveAccount()
        val token = activeAccount?.let { tokenManager.getToken(it) }

        if (token == null) {
            errorMessage = "Пожалуйста, войдите снова."
            return
        }

        fetchData(
            isInitialLoad = isInitialLoad,
            requests = listOf(
                { RetrofitInstance.api.getTariffs("Bearer $token") }
            )
        ) { responses ->
            val tariffResponse = responses[0] as? Tariffs
            tariffResponse?.let {
                tariffs = it
            }
        }
    }

    fun refreshTariffInfo() {
        loadTariffInfo(isInitialLoad = false)
    }

    fun changeTariff(tariffId: Int) {
        val context = getApplication<Application>().applicationContext
        val tokenManager = TokenManager.getInstance(context)
        val activeAccount = tokenManager.getActiveAccount()
        val token = activeAccount?.let { tokenManager.getToken(it) }

        if (token == null) {
            tariffChangeMessage = "Пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.setTariff("Bearer $token", tariffId)
                }
                if (response.isSuccessful) {
                    tariffChangeMessage = "Тариф успешно изменен"
                    isTariffChangeSuccessful = true
                    loadTariffInfo()
                } else {
                    tariffChangeMessage = "Не удалось изменить тариф"
                    Log.e("TariffViewModel", "Error changing tariff: ${response.errorBody()?.string()}")
                    isTariffChangeSuccessful = false
                }
            } catch (e: Exception) {
                tariffChangeMessage = "Ошибка при смене тарифа"
                Log.e("TariffViewModel", "Error changing tariff", e)
                isTariffChangeSuccessful = false
            } finally {
                isLoading = false
            }
        }
    }

    fun undoChangeTariff() {
        val context = getApplication<Application>().applicationContext
        val tokenManager = TokenManager.getInstance(context)
        val activeAccount = tokenManager.getActiveAccount()
        val token = activeAccount?.let { tokenManager.getToken(it) }

        if (token == null) {
            tariffChangeMessage = "Пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            isLoading = true
            try {
                val response = withContext(Dispatchers.IO) {
                    RetrofitInstance.api.undoChangeTariff("Bearer $token")
                }
                if (response.isSuccessful) {
                    tariffChangeMessage = "Смена тарифа отменена"
                    isTariffChangeSuccessful = false
                    loadTariffInfo()
                } else {
                    tariffChangeMessage = "Не удалось отменить смену тарифа"
                    Log.e("TariffViewModel", "Error canceling tariff change: ${response.errorBody()?.string()}")
                }
            } catch (e: Exception) {
                tariffChangeMessage = "Ошибка при отмене смены тарифа"
                Log.e("TariffViewModel", "Error canceling tariff change", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun clearTariffChangeMessage() {
        viewModelScope.launch {
            tariffChangeMessage = null
            isTariffChangeSuccessful = false
        }
    }
}