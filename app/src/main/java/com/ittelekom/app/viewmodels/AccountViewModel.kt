package com.ittelekom.app.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ittelekom.app.models.AccountInfo
import com.ittelekom.app.models.MessageCarrier
import com.ittelekom.app.network.RetrofitInstance
import kotlinx.coroutines.launch
import retrofit2.Response

open class AccountViewModel(application: Application) : BaseViewModel(application) {
    var accountInfo by mutableStateOf<AccountInfo?>(null)
        private set

    var isDataLoaded by mutableStateOf(false)
        private set

    private inline fun <reified T> handleResponse(
        request: () -> Response<T>,
        defaultError: String
    ): T? where T : MessageCarrier {
        val response = request()

        if (response.isSuccessful) {
            return response.body()
        }

        val errorBodyString = response.errorBody()?.string()
        if (!errorBodyString.isNullOrEmpty()) {
            try {
                val errorObj = Gson().fromJson(errorBodyString, T::class.java)
                val errorMessage = errorObj.message?.takeIf { it.isNotEmpty() }
                val errorField = errorObj.error?.takeIf { it.isNotEmpty() }
                when {
                    errorMessage != null -> {
                        Log.w("handleResponse", "Server message: $errorMessage")
                        return errorObj
                    }
                    errorField != null -> {
                        Log.e("handleResponse", "Server error: $errorField")
                        return errorObj
                    }
                }
            } catch (e: Exception) {
                Log.e("handleResponse", "Error parsing error body", e)
            }
        }

        setError(defaultError)
        return null
    }

    fun loadAccountInfo(state: State) {
        if (isDataLoaded && state != State.REFRESHING && state != State.LOADING_ITEM) return

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
                val accountBody = handleResponse(
                    request = { RetrofitInstance.api.getAccountInfo("Bearer $token") },
                    defaultError = "Ошибка загрузки данных"
                )

                val payToDateBody = handleResponse(
                    request = { RetrofitInstance.api.getPayToDate("Bearer $token") },
                    defaultError = "Ошибка загрузки PayToDate"
                )

                val servicesBody = handleResponse(
                    request = { RetrofitInstance.api.getServices("Bearer $token") },
                    defaultError = "Ошибка загрузки списка услуг"
                )

                val paysBody = handleResponse(
                    request = { RetrofitInstance.api.getPays("Bearer $token") },
                    defaultError = "Ошибка загрузки платежей"
                )

                accountBody?.let {
                    it.payToDate = payToDateBody
                    it.services = servicesBody?.services ?: emptyList()
                    it.pays = paysBody?.pays ?: emptyList()
                    accountInfo = it
                    isDataLoaded = true
                }

            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error loading account info", e)
                setError("Ошибка загрузки данных")
            } finally {
                currentState = State.IDLE
            }
        }
    }

    fun refreshAccountInfo() {
        isDataLoaded = false
        loadAccountInfo(state = State.LOADING)
    }

    fun pullToRefreshAccountInfo() {
        isDataLoaded = false
        loadAccountInfo(state = State.REFRESHING)
    }
}
