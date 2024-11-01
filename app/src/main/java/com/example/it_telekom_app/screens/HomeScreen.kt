package com.example.it_telekom_app.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.it_telekom_app.models.AccountInfo
import com.example.it_telekom_app.network.RetrofitInstance
import com.google.accompanist.swiperefresh.*
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(token: String?) {
    val scope = rememberCoroutineScope()
    var accountInfo by remember { mutableStateOf<AccountInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        if (token != null) {
            scope.launch {
                try {
                    val response = RetrofitInstance.api.getAccountInfo("Bearer $token")
                    if (response.isSuccessful && response.body() != null) {
                        accountInfo = response.body()
                    } else {
                        errorMessage = "Ошибка получения данных аккаунта: ${response.message()}"
                    }
                } catch (e: Exception) {
                    errorMessage = "Ошибка сети: ${e.message}"
                    Log.e("HomeScreen", "Ошибка сети: ${e.message}", e)
                } finally {
                    isLoading = false
                }
            }
        } else {
            errorMessage = "Токен не найден, пожалуйста, войдите снова."
            isLoading = false
        }
    }

    var isRefreshing by remember { mutableStateOf(false) }

    // Use SwipeRefresh from Accompanist
    SwipeRefresh(
        state = rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            // Set refreshing state to true
            isRefreshing = true

            // Perform refresh logic here, e.g., make network call
            // Имитация обновления данных
            // В реальном приложении замените этот вызов на ваш API-запрос
            LaunchedEffect(Unit) {
                // Задержка для имитации обновления
                kotlinx.coroutines.delay(2000)
                // После получения данных устанавливаем isRefreshing обратно в false
                isRefreshing = false
            }
        },
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(48.dp)
                    )
                } else {
                    if (accountInfo != null) {
                        Text(
                            text = "Имя: ${accountInfo!!.name}",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(
                            text = "Логин: ${accountInfo!!.login}",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(
                            text = "Баланс: ${accountInfo!!.balance}",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(
                            text = "Тариф: ${accountInfo!!.tariff_caption}",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    } else {
                        Text(
                            text = errorMessage ?: "Ошибка загрузки данных",
                            fontSize = 18.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }
        }
    }
}