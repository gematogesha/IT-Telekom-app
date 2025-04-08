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

    var message by remember { mutableStateOf("") }

    val accountInfo = viewModel.accountInfo
    val errorMessage = viewModel.errorMessage
    val isRefreshing = viewModel.isRefreshingState()
    val isLoading = viewModel.isLoadingState()
    var state by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadAccountInfo(BaseViewModel.State.LOADING)
    }

    LaunchedEffect(message) {
        if (message.isNotBlank()) {
            snackbarHostState.showSnackbar(message)
            message = ""
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Оплата",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
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
                if (isLoading) {
                    CustomLoadingIndicator()
                } else {
                    if (accountInfo != null) {
                        var phoneNumber by remember { mutableStateOf("") }
                        var sumPay by remember { mutableStateOf("") }

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
                                    modifier = Modifier
                                        .fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                            item {
                                Button(
                                    onClick = {
                                        when {
                                            phoneNumber.isBlank() || sumPay.isBlank() -> {
                                                message = "Заполните все поля"
                                            }

                                            !phoneNumber.matches(Regex("^\\d{10,15}\$")) -> {
                                                message = "Введите корректный номер телефона"
                                            }

                                            !sumPay.matches(Regex("^\\d+(\\.\\d{1,2})?\$")) -> {
                                                message = "Введите корректную сумму"
                                            }

                                            else -> {
                                                state = true
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
                                                            if (response.isSuccessful) {
                                                                state = false
                                                                message = "Перенаправление на страницу оплаты"

                                                                val regex = Regex("url=(https?://[^}]+)")
                                                                val matchResult = regex.find(
                                                                    response.toString()
                                                                )
                                                                val extractedUrl = matchResult?.groupValues?.get(1)

                                                                val intent =
                                                                    Intent(Intent.ACTION_VIEW).setData(
                                                                        Uri.parse(extractedUrl))
                                                                context.startActivity(intent)

                                                            } else {
                                                                state = false
                                                                message = "Ошибка: ${response.message()}"
                                                            }
                                                        }
                                                    } catch (e: Exception) {
                                                        withContext(Dispatchers.Main) {
                                                            Log.e("StatsData", "Ошибка: ${e.message}")
                                                            message = "Ошибка при оплате"
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 24.dp, start = 40.dp, end = 40.dp),
                                    enabled = !state,
                                ) {
                                    if(state) {
                                        ButtonLoadingIndicator()
                                    } else {
                                        Text(
                                            text = "Оплатить",
                                        )
                                    }

                                }
                            }
                        }
                    } else if (errorMessage != null) {
                        ErrorDisplay(
                            refreshFunction = { viewModel.refreshAccountInfo() },
                            errorMessage = errorMessage,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        }
    }
}




