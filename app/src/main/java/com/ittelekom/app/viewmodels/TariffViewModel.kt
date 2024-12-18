package com.ittelekom.app.viewmodels

import android.app.Application
import android.util.Log
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

    fun loadTariffInfo(state: State) {
        if (isDataLoaded && state != State.REFRESHING) return
        if (isDataLoaded && state != State.LOADING_ITEM) return

        fetchData(
            state = state,
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
        loadTariffInfo(state = State.LOADING)
    }

    fun pullToRefreshTariffInfo() {
        isDataLoaded = false // Reset the flag to force data reload
        loadTariffInfo(state = State.REFRESHING)
    }

    fun changeTariff(tariffId: Int) {
        fetchData(
            state = State.LOADING_ITEM,
            requests = listOf { RetrofitInstance.api.setTariff("Bearer ${getToken()}", tariffId) }
        ) { responses ->
            val response = responses[0]
            if (response != null) {
                tariffChangeMessage = "Тариф успешно изменен"
                isTariffChangeSuccessful = true
                loadTariffInfo(State.IDLE) // Обновляем данные после смены тарифа
            } else {
                tariffChangeMessage = "Не удалось изменить тариф"
                isTariffChangeSuccessful = false
            }
        }
    }

    fun undoChangeTariff() {
        fetchData(
            state = State.LOADING_ITEM,
            requests = listOf { RetrofitInstance.api.undoChangeTariff("Bearer ${getToken()}") }
        ) { responses ->
            val response = responses[0]
            if (response != null) {
                tariffChangeMessage = "Смена тарифа отменена"
                isTariffChangeSuccessful = false
                loadTariffInfo(State.LOADING_ITEM) // Обновляем тарифы после отмены смены
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