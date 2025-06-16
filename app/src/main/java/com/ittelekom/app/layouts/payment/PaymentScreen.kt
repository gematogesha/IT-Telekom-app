package com.ittelekom.app.layouts.payment

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CreditCard
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.ittelekom.app.components.CustomLoadingIndicator
import com.ittelekom.app.components.PullRefresh
import com.ittelekom.app.layouts.LoginActivity
import com.ittelekom.app.ui.util.AccountBalanceCard
import com.ittelekom.app.ui.util.AccountSelectCard
import com.ittelekom.app.ui.util.ErrorDisplay
import com.ittelekom.app.utils.TokenManager
import com.ittelekom.app.viewmodels.AccountViewModel
import com.ittelekom.app.viewmodels.BaseViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PaymentScreen(viewModel: AccountViewModel) {
    val context = LocalContext.current
    val tokenManager = TokenManager.getInstance(context)
    val accounts = remember { tokenManager.getAllAccounts().toList() }
    var selectedAccount by remember { mutableStateOf(tokenManager.getActiveAccount()) }
    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage = viewModel.errorMessage

    val accountInfo = viewModel.accountInfo
    val isRefreshing = viewModel.isRefreshingState()
    val isLoading = viewModel.isLoadingState()

    // Обработка ошибок через Flow из ViewModel
    LaunchedEffect(Unit) {
        viewModel.errorFlow.collect { error ->
            if (error.isNotBlank()) {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    // Следим за выбранным аккаунтом
    LaunchedEffect(selectedAccount) {
        if (selectedAccount == null) {
            // Нет аккаунта — переходим на экран логина
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        } else {
            // Загружаем или обновляем информацию по аккаунту
            viewModel.loadAccountInfo(BaseViewModel.State.LOADING)
            tokenManager.setActiveAccount(selectedAccount!!)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        PullRefresh(
            refreshing = isRefreshing,
            enabled = true,
            onRefresh = { viewModel.pullToRefreshAccountInfo() },
            modifier = Modifier.fillMaxSize(),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    when {
                        isLoading -> CustomLoadingIndicator()
                        accountInfo != null -> LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(bottom = 16.dp)
                        ) {
                            item {
                                AccountSelectCard(
                                    info = accountInfo,
                                    accounts = accounts,
                                    selectedAccount = selectedAccount,
                                    onAccountSelected = { selectedAccount = it }
                                )
                            }
                            item {
                                AccountBalanceCard(
                                    info = accountInfo,
                                    showAddOpt = true
                                )
                            }
                            item {
                                PaymentMethodCard(
                                    onCardPaymentClicked = {
                                        startPaymentFieldActivity(context, paymentType = "card")
                                    },
                                    onPhonePaymentClicked = {
                                        startPaymentFieldActivity(context, paymentType = "phone")
                                    }
                                )
                            }
                        }
                        else -> ErrorDisplay(
                            onRefreshClick = { viewModel.refreshAccountInfo() },
                            errorMessage = errorMessage,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

// Вынесем функцию для запуска PaymentFieldActivity с параметром
private fun startPaymentFieldActivity(context: android.content.Context, paymentType: String) {
    val intent = Intent(context, PaymentFieldActivity::class.java).apply {
        putExtra("payment_type", paymentType)
    }
    context.startActivity(intent)
}

@Composable
fun PaymentMethodCard(
    onCardPaymentClicked: () -> Unit = {},
    onPhonePaymentClicked: () -> Unit = {}
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Способ оплаты",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CreditCard,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "По карте",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onCardPaymentClicked, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Smartphone,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "По номеру телефона",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onPhonePaymentClicked, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
