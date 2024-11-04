package com.example.it_telekom_app.screens

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.it_telekom_app.components.PullRefresh
import com.example.it_telekom_app.models.AccountInfo
import com.example.it_telekom_app.viewmodels.HomeViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(token: String?) {
    val homeViewModel: HomeViewModel = viewModel()
    val accountInfo = homeViewModel.accountInfo
    val isLoading = homeViewModel.isLoading
    val errorMessage = homeViewModel.errorMessage
    val isRefreshing = homeViewModel.isRefreshing

    LaunchedEffect(token) {
        if (accountInfo == null && !isLoading && !isRefreshing) {
            homeViewModel.loadAccountInfo(token)
        }
    }

    Scaffold(
        content = {
            PullRefresh(
                refreshing = isRefreshing,
                enabled = true,
                onRefresh = { homeViewModel.refreshAccountInfo(token) },
                modifier = Modifier.fillMaxSize(),
                indicatorPadding = PaddingValues(16.dp)
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(all = 16.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            if (accountInfo != null) {
                                items(listOf(accountInfo)) { info ->
                                    CardTop(accountInfo)
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

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun CardTop(info: AccountInfo) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp) // Фиксированный размер для создания круга
                    .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "User Icon",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp) // Размер иконки
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Абонент №1",
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "№" + info.num_dog, // Используйте вашу строку для номера договора
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            // Правая иконка
            Box(
                modifier = Modifier
                    .size(40.dp) // Фиксированный размер для создания круга
                    .background(MaterialTheme.colorScheme.outlineVariant, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "User Locked Icon",
                    modifier = Modifier.size(20.dp) // Размер иконки
                )
            }
        }
    }
}
