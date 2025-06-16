package com.ittelekom.app.viewmodels

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ittelekom.app.models.AccountInfo
import com.ittelekom.app.models.SetBlock
import com.ittelekom.app.network.RetrofitInstance
import kotlinx.coroutines.launch

open class AccountViewModel(application: Application) : BaseViewModel(application) {
    var accountInfo by mutableStateOf<AccountInfo?>(null)
        private set

    var setBlock by mutableStateOf<SetBlock?>(null)
        private set

    var isDataLoaded by mutableStateOf(false)
        private set

    fun loadAccountInfo(state: State) {

        if (!isInternetAvailable()) {
            setError("Нет подключения к интернету")
            currentState = State.IDLE
            return
        }

        if (isDataLoaded && state !in listOf(State.REFRESHING, State.LOADING_ITEM)) return

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
                val accountBody = handleResponse(
                    request = { RetrofitInstance.api.getAccountInfo("Bearer $token") },
                    defaultError = "Ошибка загрузки данных",
                    showError = false,
                    showMessage = false
                )

                val payToDateBody = handleResponse(
                    request = { RetrofitInstance.api.getPayToDate("Bearer $token") },
                    defaultError = "",
                    showError = false,
                    showMessage = false
                )

                val servicesBody = handleResponse(
                    request = { RetrofitInstance.api.getServices("Bearer $token") },
                    defaultError = "",
                    showError = false,
                    showMessage = false
                )

                val paysBody = handleResponse(
                    request = { RetrofitInstance.api.getPays("Bearer $token") },
                    defaultError = "",
                    showError = false,
                    showMessage = false
                )

                accountBody?.let {
                    it.payToDate = payToDateBody
                    it.services = servicesBody?.services.orEmpty()
                    it.pays = paysBody?.pays.orEmpty()
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

    fun loadSetBlock(state: State) {
        if (isDataLoaded && state !in listOf(State.REFRESHING, State.LOADING_ITEM)) return

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
                val setBlockBody = handleResponse(
                    request = { RetrofitInstance.api.setBlock("Bearer $token") },
                    defaultError = "Ошибка загрузки данных"
                )

                setBlockBody?.let {
                    setBlock = it
                    isDataLoaded = true
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error set block", e)
                setError("Ошибка загрузки данных")
            } finally {
                currentState = State.IDLE
            }
        }
    }

    fun changeMac(state: State, mac: String, onSuccess: () -> Unit = {}) {
        if (isDataLoaded && state !in listOf(State.REFRESHING, State.LOADING_ITEM)) return

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
                val setMacBody = handleResponse(
                    request = { RetrofitInstance.api.setMac("Bearer $token", mac) },
                    defaultError = "Ошибка загрузки данных"
                )
                setMacBody?.let {
                    isDataLoaded = true
                    onSuccess()
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Error set block", e)
                setError("Ошибка загрузки данных")
            } finally {
                currentState = State.IDLE
            }
        }
    }

    fun refreshAccountInfo() {
        isDataLoaded = false
        loadAccountInfo(State.LOADING)
    }

    fun pullToRefreshAccountInfo() {
        isDataLoaded = false
        loadAccountInfo(State.REFRESHING)
    }
}
