package com.ittelekom.app.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ittelekom.app.layouts.LoginActivity
import com.ittelekom.app.models.MessageCarrier
import com.ittelekom.app.utils.TokenManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import retrofit2.Response

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    var currentState by mutableStateOf(State.IDLE)

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    /**
     * Универсальный метод для выполнения запроса с обработкой состояний и ошибок.
     *
     * @param request suspend-функция, выполняющая запрос и возвращающая Response<T>
     * @param defaultError сообщение об ошибке по умолчанию, если запрос не удался
     * @param showMessage флаг, указывающий нужно ли показывать сообщение от сервера (`message`)
     * @param showError флаг, указывающий нужно ли показывать ошибку от сервера (`error`)
     */

    protected inline fun <reified T> handleResponse(
        request: () -> Response<T>,
        defaultError: String,
        showMessage: Boolean = true,
        showError: Boolean = true
    ): T? where T : MessageCarrier {
        val response = try {
            request()
        } catch (e: Exception) {
            setError(defaultError)
            Log.e("handleResponse", "Network call failed", e)
            return null
        }

        if (response.isSuccessful) {
            return response.body()
        }

        val errorBodyString = response.errorBody()?.string()
        if (!errorBodyString.isNullOrEmpty()) {
            try {
                val errorObj = Gson().fromJson(errorBodyString, T::class.java)
                val errorMessage = errorObj.message?.takeIf { it.isNotEmpty() }
                val errorField = errorObj.error?.takeIf { it.isNotEmpty() }

                val processedMessage = when (errorMessage) {
                    "" -> ""
                    else -> errorMessage
                }

                val processedError = when (errorField) {
                    "Нечего разблокировать!" -> "Ошибка загрузки данных"
                    "Forbidden change tariff" -> "Нет доступных тарифов"
                    "You not loggin or token incorrect." -> "Вы не вошли в систему"
                    else -> errorField
                }

                when {
                    processedMessage != null -> {
                        Log.w("handleResponse", "Server message: $processedMessage")
                        if (showMessage) setError(processedMessage)
                        return errorObj
                    }
                    processedError != null -> {
                        Log.e("handleResponse", "Server error: $processedError")
                        if (showError) setError(processedError)
                        return errorObj

                    }
                }
            } catch (e: Exception) {
                setError(defaultError)
                Log.e("handleResponse", "Error parsing error body", e)
            }
        }

        setError(defaultError)
        return null
    }

    fun isInternetAvailable(): Boolean {
        val connectivityManager = getApplication<Application>()
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    fun getToken(): String? {
        val context = getApplication<Application>().applicationContext
        val tokenManager = TokenManager.getInstance(context)
        val activeAccount = tokenManager.getActiveAccount()
        return activeAccount?.let { tokenManager.getToken(it) }
    }

    fun setError(message: String) {
        errorMessage = message
        emitError(message)
    }

    fun proceedToLogout(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

    private fun emitError(message: String) {
        viewModelScope.launch {
            _errorFlow.emit(message)
        }
    }

    fun resetError() {
        errorMessage = null
    }

    enum class State {
        IDLE,
        LOADING,
        LOADING_ITEM,
        REFRESHING
    }

    fun isLoadingState() = currentState == State.LOADING
    fun isLoadingItemState() = currentState == State.LOADING_ITEM
    fun isRefreshingState() = currentState == State.REFRESHING
}
