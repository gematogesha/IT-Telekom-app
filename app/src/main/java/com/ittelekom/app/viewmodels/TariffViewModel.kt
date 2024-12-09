package com.ittelekom.app.viewmodels

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.it_telekom_app.viewmodels.BaseViewModel
import com.ittelekom.app.models.Tariffs
import com.ittelekom.app.network.RetrofitInstance

class TariffViewModel(application: Application) : BaseViewModel(application) {
    var tariffs by mutableStateOf<Tariffs?>(null)
        private set
    var tariffChangeMessage by mutableStateOf<String?>(null)
        private set
    var isTariffChangeSuccessful by mutableStateOf(false)
        private set
    var isDataLoaded by mutableStateOf(false)
        private set

    fun loadTariffInfo(forceReload: Boolean = false) {
        if (isDataLoaded && !forceReload) return

        fetchData(
            isInitialLoad = !forceReload,
            requests = listOf(
                { RetrofitInstance.api.getTariffs("Bearer ${getToken()}") }
            )
        ) { responses ->
            val tariffResponse = responses[0] as? Tariffs
            tariffResponse?.let {
                tariffs = it
            }
        }
    }

    fun refreshTariffInfo() {
        isDataLoaded = false // Reset the flag to force data reload
        loadTariffInfo(forceReload = false)
    }

    fun pullToRefreshTariffInfo() {
        isDataLoaded = false // Reset the flag to force data reload
        loadTariffInfo(forceReload = true)
    }

    fun changeTariff(tariffId: Int) {
        fetchData(
            isInitialLoad = false,
            requests = listOf(
                { RetrofitInstance.api.setTariff("Bearer ${getToken()}", tariffId) }
            )
        ) { responses ->
            val response = responses[0]
            if (response != null) {
                tariffChangeMessage = "Тариф успешно изменен"
                isTariffChangeSuccessful = true
                loadTariffInfo() // Обновляем данные после смены тарифа
            } else {
                tariffChangeMessage = "Не удалось изменить тариф"
                isTariffChangeSuccessful = false
            }
        }
    }

    fun undoChangeTariff() {
        fetchData(
            isInitialLoad = false,
            requests = listOf(
                { RetrofitInstance.api.undoChangeTariff("Bearer ${getToken()}") }
            )
        ) { responses ->
            val response = responses[0]
            if (response != null) {
                tariffChangeMessage = "Смена тарифа отменена"
                isTariffChangeSuccessful = false
                loadTariffInfo() // Обновляем тарифы после отмены смены
            } else {
                tariffChangeMessage = "Не удалось отменить смену тарифа"
            }
        }
    }

    fun clearTariffChangeMessage() {
        tariffChangeMessage = null
        isTariffChangeSuccessful = false
    }

}