package com.ittelekom.app.layouts.home

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ittelekom.app.viewmodels.BaseViewModel
import com.ittelekom.app.components.CustomLoadingIndicator
import com.ittelekom.app.components.PullRefresh
import com.ittelekom.app.layouts.LoginActivity
import com.ittelekom.app.models.AccountInfo
import com.ittelekom.app.ui.util.AccountBalanceCard
import com.ittelekom.app.ui.util.AccountSelectCard
import com.ittelekom.app.ui.util.ErrorDisplay
import com.ittelekom.app.utils.TokenManager
import com.ittelekom.app.viewmodels.AccountViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen(viewModel: AccountViewModel) {
    val context = LocalContext.current
    val tokenManager = TokenManager.getInstance(context)
    val accounts = tokenManager.getAllAccounts().toList()
    var selectedAccount by remember { mutableStateOf(tokenManager.getActiveAccount()) }
    val snackbarHostState = remember { SnackbarHostState() }

    val accountInfo = viewModel.accountInfo
    val errorMessage = viewModel.errorMessage
    val isRefreshing = viewModel.isRefreshingState()
    val isLoading = viewModel.isLoadingState()
    var isInitialLoad by remember { mutableStateOf(true) }

    LaunchedEffect(selectedAccount) {
        if (selectedAccount != null) {
            TokenManager.getInstance(context).setActiveAccount(selectedAccount!!)
            if (isInitialLoad) {
                viewModel.loadAccountInfo(BaseViewModel.State.LOADING)
                isInitialLoad = false
            } else {
                viewModel.refreshAccountInfo()
            }
        } else {
            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
        }
    }

    LaunchedEffect(errorMessage) {
        if (errorMessage != null) {
            snackbarHostState.showSnackbar(errorMessage)
            Log.e("HomeScreen", "Error: $errorMessage")
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = {
            PullRefresh(
                refreshing = isRefreshing,
                enabled = true,
                onRefresh = {
                    viewModel.pullToRefreshAccountInfo()
                },
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
                        if (isLoading) {
                            CustomLoadingIndicator()
                        } else {
                            if (accountInfo != null) {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxSize(),
                                ) {
                                    item {
                                        AccountSelectCard(
                                            info = accountInfo,
                                            accounts = accounts,
                                            selectedAccount = selectedAccount,
                                            onAccountSelected = { account ->
                                                selectedAccount = account
                                            }
                                        )
                                    }
                                    item {
                                        AccountBalanceCard(
                                            info = accountInfo,
                                            showAddOpt = false
                                        )
                                    }
                                    item {
                                        TariffCard(accountInfo)
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
    )
}

@Composable
fun TariffCard(info: AccountInfo) {
    val context = LocalContext.current
    val tariffService = info.services.firstOrNull { it.svc_name.contains("Интернет") }
    val additionalServices = info.services.filter { it.svc_name.contains("Дополнительные услуги") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            tariffService?.let {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Тариф",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Normal,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = it.svc_name.substringAfter(": ").trim(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween

                ) {
                    Text(
                        text = it.svc_descr.substringAfter("Скорость(до...): ")
                            .substringAfter(": ").removeSuffix(".").trim(),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    // Дополнительные услуги
                    if (additionalServices.isNotEmpty()) {
                        additionalServices.forEach { service ->
                            Text(
                                text = service.svc_descr,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                    }
                }
            }
            // Изменение тарифа
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Изменить тариф",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Normal,
                    color = MaterialTheme.colorScheme.outline
                )
                IconButton(
                    onClick = {
                        val intent = Intent(context, ChangeTariffActivity::class.java)
                        context.startActivity(intent)
                    },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

        }
    }
}
