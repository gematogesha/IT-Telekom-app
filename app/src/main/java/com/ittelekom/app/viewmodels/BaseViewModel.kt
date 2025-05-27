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
import com.ittelekom.app.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    // Текущее состояние
    var currentState by mutableStateOf(State.IDLE)

    var errorMessage by mutableStateOf<String?>(null)
        private set

    data class ErrorMessage(
        val message: String?
    )

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

                val allSuccessful = responses.all { it.isSuccessful }

                if (allSuccessful) {
                    onSuccess(responses.map { it.body() })
                } else {
                    val firstError = responses.firstOrNull { !it.isSuccessful }

                    val errorMsg = firstError?.errorBody()?.string()?.let { errorJson ->
                        try {
                            Gson().fromJson(errorJson, ErrorMessage::class.java).message
                        } catch (e: Exception) {
                            null
                        }
                    }
                    Log.e("BaseViewModel", "Ошибка загрузки: ${firstError?.code()} ${firstError?.message()}")

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

    private fun isInternetAvailable(): Boolean {
        val connectivityManager =
            getApplication<Application>().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
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

    //fun isIdleState(): Boolean = currentState == State.IDLE
    fun isLoadingState(): Boolean = currentState == State.LOADING
    fun isLoadingItemState(): Boolean = currentState == State.LOADING_ITEM
    fun isRefreshingState(): Boolean = currentState == State.REFRESHING
}
