package com.example.it_telekom_app.viewmodels

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class BaseViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
        protected set
    var isRefreshing by mutableStateOf(false)
        protected set
    var errorMessage by mutableStateOf<String?>(null)
        protected set

    protected fun fetchData(
        context: Context?,
        token: String?,
        isInitialLoad: Boolean,
        requests: List<suspend () -> Response<out Any>>,
        onSuccess: (List<Any?>) -> Unit
    ) {
        if (context == null || !isInternetAvailable(context)) {
            errorMessage = "Нет подключения к интернету"
            return
        }

        if (token == null) {
            errorMessage = "Пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            // Сбросить сообщение об ошибке перед началом нового запроса
            errorMessage = null

            if (isInitialLoad) isLoading = true else isRefreshing = true
            try {
                val responses = withContext(Dispatchers.IO) {
                    requests.map { it() }
                }
                if (responses.all { it.isSuccessful }) {
                    onSuccess(responses.map { it.body() })
                    errorMessage = null
                } else {
                    errorMessage = "Ошибка загрузки данных"
                }
            } catch (e: Exception) {
                Log.e("BaseViewModel", "Error executing requests", e)
                errorMessage = "Ошибка загрузки данных"
            } finally {
                if (isInitialLoad) isLoading = false else isRefreshing = false
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}