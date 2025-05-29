package com.ittelekom.app.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.ittelekom.app.models.MessageCarrier
import com.ittelekom.app.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    var currentState by mutableStateOf(State.IDLE)

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private val _errorFlow = MutableSharedFlow<String>()
    val errorFlow = _errorFlow.asSharedFlow()

    data class ErrorMessage(val message: String?)

    /**
     * Универсальный метод для выполнения списка запросов с обработкой состояний и ошибок.
     *
     * @param state состояние для установки во время выполнения.
     * @param requests список suspend функций с запросами.
     * @param onSuccess вызывается, если все запросы успешны, с результатами.
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
                    "success" -> "Аккаунт успешно обновлен"
                    else -> errorMessage
                }

                val processedError = when (errorField) {
                    "Нечего разблокировать!" -> "Ошибка загрузки данных"
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


    //TODO: Удалить
    protected fun fetchData(
        state: State,
        requests: List<suspend () -> Response<out Any>>,
        onSuccess: (List<Any?>) -> Unit
    ) {
        if (!isInternetAvailable()) {
            setError("Нет подключения к интернету")
            currentState = State.IDLE
            return
        }

        currentState = state

        viewModelScope.launch {
            resetError()

            try {
                val responses = withContext(Dispatchers.IO) {
                    requests.map { it() }
                }

                val firstError = responses.firstOrNull { !it.isSuccessful }
                if (firstError == null) {
                    onSuccess(responses.map { it.body() })
                } else {
                    val errorMsg = parseError(firstError)
                    Log.e("BaseViewModel", "Ошибка загрузки: ${firstError.code()} ${firstError.message()}")

                    setError(errorMsg ?: "Ошибка загрузки данных")
                }
            } catch (e: Exception) {
                Log.e("BaseViewModel", "Исключение при загрузке данных", e)
                setError("Ошибка загрузки данных")
            } finally {
                currentState = State.IDLE
            }
        }
    }

    private fun parseError(response: Response<*>): String? {
        return try {
            response.errorBody()?.string()?.let { errorJson ->
                Gson().fromJson(errorJson, ErrorMessage::class.java).message
            }
        } catch (e: Exception) {
            Log.e("BaseViewModel", "Ошибка парсинга тела ошибки", e)
            null
        }
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
