package com.ittelekom.app.layouts.payment

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ittelekom.app.viewmodels.BaseViewModel
import com.ittelekom.app.components.ButtonLoadingIndicator
import com.ittelekom.app.components.CustomLoadingIndicator
import com.ittelekom.app.network.StatsRetrofitInstance
import com.ittelekom.app.ui.theme.ITTelekomTheme
import com.ittelekom.app.ui.util.ErrorDisplay
import com.ittelekom.app.viewmodels.AccountViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PaymentFieldActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ITTelekomTheme(window = window) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                PaymentFieldScreen(onBackPressed = { finish() })
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentFieldScreen(onBackPressed: () -> Unit) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: AccountViewModel = viewModel()
    val context = LocalContext.current

    // Локальное сообщение для ошибок валидации (например, "Заполните все поля")
    var localErrorMessage by remember { mutableStateOf("") }

    val accountInfo = viewModel.accountInfo
    val isLoading = viewModel.isLoadingState()

    // Подписываемся на ошибки из viewModel.errorFlow
    LaunchedEffect(Unit) {
        launch {
            viewModel.errorFlow.collectLatest { msg ->
                if (msg.isNotBlank()) snackbarHostState.showSnackbar(msg)
            }
        }
        // Подписка на локальные ошибки валидации
        launch {
            snapshotFlow { localErrorMessage }
                .filter { it.isNotBlank() }
                .collect {
                    snackbarHostState.showSnackbar(it)
                    localErrorMessage = ""
                }
        }
    }

    // Запрос данных при первом отображении
    LaunchedEffect(Unit) {
        viewModel.loadAccountInfo(BaseViewModel.State.LOADING)
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = "Оплата", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(all = 16.dp),
            ) {
                when {
                    isLoading -> CustomLoadingIndicator()
                    accountInfo != null -> {
                        var phoneNumber by remember { mutableStateOf("") }
                        var sumPay by remember { mutableStateOf("") }
                        var isButtonLoading by remember { mutableStateOf(false) }

                        LazyColumn(
                            contentPadding = innerPadding,
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            item {
                                OutlinedTextField(
                                    value = phoneNumber,
                                    onValueChange = { phoneNumber = it },
                                    label = { Text("Номер телефона") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Phone,
                                            contentDescription = "Phone number",
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 12.dp),
                                    singleLine = true
                                )
                            }
                            item {
                                OutlinedTextField(
                                    value = sumPay,
                                    onValueChange = { sumPay = it },
                                    label = { Text("Сумма оплаты") },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Outlined.Payments,
                                            contentDescription = "Sum pay",
                                        )
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            item {
                                Button(
                                    onClick = {
                                        when {
                                            phoneNumber.isBlank() || sumPay.isBlank() -> {
                                                localErrorMessage = "Заполните все поля"
                                            }
                                            !phoneNumber.matches(Regex("^\\d{10,15}\$")) -> {
                                                localErrorMessage = "Введите корректный номер телефона"
                                            }
                                            !sumPay.matches(Regex("^\\d+(\\.\\d{1,2})?\$")) -> {
                                                localErrorMessage = "Введите корректную сумму"
                                            }
                                            else -> {
                                                isButtonLoading = true
                                                CoroutineScope(Dispatchers.IO).launch {
                                                    try {
                                                        val response = StatsRetrofitInstance.api.submitData(
                                                            pin = "login",
                                                            fio = "name",
                                                            paySumm = sumPay,
                                                            phone = phoneNumber,
                                                            addPayYooKassaBtn = "true"
                                                        )
                                                        withContext(Dispatchers.Main) {
                                                            isButtonLoading = false
                                                            if (response.isSuccessful) {
                                                                localErrorMessage = "Перенаправление на страницу оплаты"

                                                                // Пример парсинга URL из тела (лучше заменить на реальный)
                                                                val regex = Regex("url=(https?://[^}]+)")
                                                                val matchResult = regex.find(response.toString())
                                                                val extractedUrl = matchResult?.groupValues?.get(1)

                                                                extractedUrl?.let { url ->
                                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                                                    context.startActivity(intent)
                                                                } ?: run {
                                                                    localErrorMessage = "Не удалось получить ссылку для оплаты"
                                                                }

                                                            } else {
                                                                localErrorMessage = "Ошибка: ${response.message()}"
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        withContext(Dispatchers.Main) {
                                                            Log.e("StatsData", "Ошибка: ${e.message}")
                                                            localErrorMessage = "Ошибка при оплате"
                                                            isButtonLoading = false
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp, start = 40.dp, end = 40.dp),
                                    enabled = !isButtonLoading,
                                ) {
                                    if (isButtonLoading) {
                                        ButtonLoadingIndicator()
                                    } else {
                                        Text(text = "Оплатить")
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        // Если accountInfo == null и ошибка загрузки (показываем ErrorDisplay с возможностью обновить)
                        ErrorDisplay(
                            onRefreshClick = { viewModel.refreshAccountInfo() },
                            errorMessage = viewModel.errorMessage ?: "Ошибка загрузки данных",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}




