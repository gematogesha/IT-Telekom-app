package com.example.it_telekom_app.viewmodels

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
import com.ittelekom.app.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    var isLoading by mutableStateOf(false)
        protected set
    var isRefreshing by mutableStateOf(false)
        protected set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    protected fun fetchData(
        isInitialLoad: Boolean,
        requests: List<suspend () -> Response<out Any>>,
        onSuccess: (List<Any?>) -> Unit
    ) {
        if (!isInternetAvailable()) {
            setError("Нет подключения к интернету")
            return
        }

        viewModelScope.launch {
            resetError()

            if (isInitialLoad) isLoading = true else isRefreshing = true

            try {
                val responses = withContext(Dispatchers.IO) {
                    requests.map { it() }
                }
                Log.d("Base", "Response - $responses")
                if (responses.all { it.isSuccessful }) {
                    onSuccess(responses.map { it.body() })
                } else {
                    setError("Ошибка загрузки данных")
                }
            } catch (e: Exception) {
                Log.e("BaseViewModel", "Error executing requests", e)
                errorMessage = "Ошибка загрузки данных"
            } finally {
                if (isInitialLoad) isLoading = false else isRefreshing = false
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

    private fun setError(message: String) {
        errorMessage = message
    }

    private fun resetError() {
        errorMessage = null
    }
}
