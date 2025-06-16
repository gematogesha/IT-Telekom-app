package com.ittelekom.app.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.ittelekom.app.models.Logout
import com.ittelekom.app.network.RetrofitInstance
import com.ittelekom.app.utils.TokenManager
import kotlinx.coroutines.launch

class LoginLogoutModel(application: Application) : BaseViewModel(application) {

    var isDataLoaded by mutableStateOf(false)
        private set

    var logout by mutableStateOf<Logout?>(null)
        private set

    fun login(state: State, login: String, password: String, context: Context, onSuccess: () -> Unit = {}) {
        if (isDataLoaded && state !in listOf(State.REFRESHING, State.LOADING_ITEM)) return

        if (!isInternetAvailable()) {
            setError("Нет подключения к интернету")
            currentState = State.IDLE
            return
        }

        currentState = state
        resetError()

        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.login(login, password)

                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.string().trim()
                    TokenManager.getInstance(context).saveToken(login, token)
                    TokenManager.getInstance(context).setActiveAccount(login)
                    isDataLoaded = true
                    onSuccess()
                } else {
                    setError("Ошибка входа")
                }
            } catch (e: Exception) {
                Log.e("AccountViewModel", "Ошибка входа", e)
                setError("Ошибка загрузки данных")
            } finally {
                currentState = State.IDLE
            }
        }
    }

    fun logout(state: State, account: String?) {
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

            val tokenManager = TokenManager.getInstance(getApplication())

            try {
                val logoutBody = handleResponse(
                    request = { RetrofitInstance.api.logout("Bearer $token") },
                    defaultError = "Не удалось выполнить выход",
                )

                logoutBody?.let {
                    logout = it

                    if (account != null) {
                        tokenManager.removeToken(account)
                    }

                    val remainingAccounts = tokenManager.getAllAccounts()

                    if (remainingAccounts.isNotEmpty()) {
                        tokenManager.setActiveAccount(remainingAccounts.first())
                        AccountViewModel(getApplication()).refreshAccountInfo()
                    } else {
                        proceedToLogout(getApplication())
                    }

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


}