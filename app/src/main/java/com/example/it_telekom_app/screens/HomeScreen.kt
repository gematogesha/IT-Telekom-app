package com.example.it_telekom_app.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.example.it_telekom_app.models.AccountInfo
import com.example.it_telekom_app.network.RetrofitInstance
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(token: String?) {
    var accountInfo by remember { mutableStateOf<AccountInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var isRefreshing by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val scope = rememberCoroutineScope()

    // Функция для обновления данных
    val onRefresh: () -> Unit = {
        scope.launch {
            isRefreshing = true
            loadAccountInfo(token) { response, error ->
                accountInfo = response
                errorMessage = error
                isLoading = false
                isRefreshing = false
            }
        }
    }

    // Первоначальная загрузка данных
    LaunchedEffect(Unit) {
        onRefresh()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(pullToRefreshState.nestedScrollConnection),
    ) { innerPadding ->
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {
                // LazyColumn для контента
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    if (accountInfo != null) {
                        items(listOf(accountInfo!!)) { info ->
                            Text(text = "Имя: ${info.name}")
                            Text(text = "Логин: ${info.login}")
                            Text(text = "Баланс: ${info.balance}")
                            Text(text = "Тариф: ${info.tariff_caption}")
                        }
                    } else if (errorMessage != null) {
                        item {
                            Text(text = errorMessage ?: "Ошибка загрузки данных")
                        }
                    }
                }

                // Индикатор загрузки
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                if (pullToRefreshState.isRefreshing) {
                    LaunchedEffect(Unit) {
                        onRefresh()
                    }
                }

                // PullToRefreshContainer для отображения анимации pull-to-refresh
                PullToRefreshContainer(
                    state = pullToRefreshState,
                    modifier = Modifier.align(Alignment.TopCenter),
                )
            }
        }
    }
}
// Функция для загрузки данных аккаунта
private suspend fun loadAccountInfo(
    token: String?,
    onResult: (AccountInfo?, String?) -> Unit
) {
    if (token != null) {
        try {
            val response = RetrofitInstance.api.getAccountInfo("Bearer $token")
            if (response.isSuccessful && response.body() != null) {
                onResult(response.body(), null)
            } else {
                onResult(null, "Ошибка получения данных аккаунта: ${response.message()}")
            }
        } catch (e: Exception) {
            onResult(null, "Ошибка сети: ${e.message}")
        }
    } else {
        onResult(null, "Токен не найден, пожалуйста, войдите снова.")
    }
}