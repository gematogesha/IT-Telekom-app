package com.ittelekom.app.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ittelekom.app.models.MessageCarrier
import com.ittelekom.app.models.SetTariffResponse
import com.ittelekom.app.models.Tariffs
import com.ittelekom.app.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Response

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
        if (isDataLoaded && state != State.REFRESHING && state != State.LOADING_ITEM) return

        if (!isInternetAvailable()) {
            setError("Нет подключения к интернету")
            currentState = State.IDLE
            return
        }

        currentState = state
        resetError()

        viewModelScope.launch {
            val token = getToken()
            if (token.isNullOrEmpty()) {
                setError("Токен не найден")
                currentState = State.IDLE
                return@launch
            }

            try {
                val tarrifBody = handleResponse(
                    request = { RetrofitInstance.api.getTariffs("Bearer $token") },
                    defaultError = "Ошибка загрузки тарифов"
                )

                tarrifBody?.let {
                    tariffs = it
                    isDataLoaded = true
                }

            } catch (e: Exception) {
                Log.e("TariffViewModel", "Error loading tariff info", e)
                setError("Ошибка загрузки данных")
            } finally {
                currentState = State.IDLE
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

    //TODO: Переделать на изменение тарифа
    fun changeTariff(tariffId: Int) {
        fetchData(
            state = State.LOADING_ITEM,
            requests = listOf { RetrofitInstance.api.setTariff("Bearer ${getToken()}", tariffId) }
        ) { responses ->
            val response = responses[0] as? SetTariffResponse
            if (response != null) {
                if (response.success) {
                    tariffChangeMessage = "Тариф успешно изменен"
                    isTariffChangeSuccessful = true
                } else {
                    tariffChangeMessage = "Не удалось изменить тариф"
                    isTariffChangeSuccessful = false
                }
                loadTariffInfo(State.IDLE)
            } else {
                tariffChangeMessage = "Не удалось изменить тариф"
                isTariffChangeSuccessful = false
            }
        }
    }

    //TODO: Добавить проверку на успешность ответа
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