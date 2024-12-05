package com.ittelekom.app.viewmodels

import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.ittelekom.app.utils.TokenManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isRefreshing = MutableLiveData<Boolean>()
    val isRefreshing: LiveData<Boolean> = _isRefreshing

    val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    protected fun fetchData(
        isInitialLoad: Boolean,
        requests: List<suspend () -> Response<out Any>>,
        onSuccess: (List<Any?>) -> Unit
    ) {
        val context = getApplication<Application>().applicationContext

        if (!isInternetAvailable(context)) {
            _errorMessage.value = "Нет подключения к интернету"
            return
        }

        val tokenManager = TokenManager.getInstance(context)
        val activeAccount = tokenManager.getActiveAccount()
        val token = activeAccount?.let { tokenManager.getToken(it) }

        if (token == null) {
            _errorMessage.value = "Пожалуйста, войдите снова."
            return
        }

        viewModelScope.launch {
            // Сбросить сообщение об ошибке перед началом нового запроса
            _errorMessage.value = null

            if (isInitialLoad) _isLoading.value = true else _isRefreshing.value = true
            try {
                val responses = withContext(Dispatchers.IO) {
                    requests.map { it() }
                }
                if (responses.all { it.isSuccessful }) {
                    onSuccess(responses.map { it.body() })
                    _errorMessage.value = null
                } else if (responses.any { it.code() == 403 }) {
                    _errorMessage.value = "Нет доступа к данным"
                } else {
                    _errorMessage.value = "Ошибка загрузки данных"
                }
            } catch (e: Exception) {
                Log.e("BaseViewModel", "Error executing requests", e)
                _errorMessage.value = "Ошибка загрузки данных"
            } finally {
                if (isInitialLoad) _isLoading.value = false else _isRefreshing.value = false
            }
        }
    }

    private fun isInternetAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val networkCapabilities =
            connectivityManager.getNetworkCapabilities(network) ?: return false
        return networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}