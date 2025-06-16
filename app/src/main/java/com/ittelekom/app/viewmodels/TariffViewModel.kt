package com.ittelekom.app.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ittelekom.app.models.Tariffs
import com.ittelekom.app.network.RetrofitInstance
import kotlinx.coroutines.launch

class TariffViewModel(application: Application) : BaseViewModel(application) {
    var tariffs by mutableStateOf<Tariffs?>(null)
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
                proceedToLogout(getApplication())
                return@launch
            }

            try {
                val tariffBody = handleResponse(
                    request = { RetrofitInstance.api.getTariffs("Bearer $token") },
                    defaultError = "Ошибка загрузки тарифов"
                )

                tariffBody?.let {
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

    //TODO: Добавить проверку на успешность ответа изменения тарифа
    fun changeTariff(tariffId: Int) {

    }

    //TODO: Добавить проверку на успешность ответа отмены изменения тарифа
    fun undoChangeTariff() {

    }

}