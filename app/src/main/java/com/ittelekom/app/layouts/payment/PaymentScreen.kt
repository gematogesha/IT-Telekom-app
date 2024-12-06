package com.ittelekom.app.layouts.payment

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
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
import com.ittelekom.app.components.PullRefresh
import com.ittelekom.app.layouts.LoginActivity
import com.ittelekom.app.ui.util.AccountBalanceCard
import com.ittelekom.app.ui.util.AccountSelectCard
import com.ittelekom.app.utils.TokenManager
import com.ittelekom.app.viewmodels.AccountViewModel

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun PaymentScreen(viewModel: AccountViewModel) {
    val context = LocalContext.current
    val tokenManager = TokenManager.getInstance(context)
    val accounts = tokenManager.getAllAccounts().toList()
    var selectedAccount by remember { mutableStateOf(tokenManager.getActiveAccount()) }
    val snackbarHostState = remember { SnackbarHostState() }

    val accountInfo = viewModel.accountInfo
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val isRefreshing = viewModel.isRefreshing

    LaunchedEffect(selectedAccount) {
        if (selectedAccount != null) {
            tokenManager.setActiveAccount(selectedAccount!!)
            if (!isLoading) {
                viewModel.loadAccountInfo()
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
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else if (accountInfo == null) {
                            Text(
                                text = "Нет данных для отображения",
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                item {
                                    AccountSelectCard(
                                        info = accountInfo,
                                        accounts = accounts,
                                        selectedAccount = selectedAccount,
                                        onAccountSelected = { account ->
                                            selectedAccount = account
                                            viewModel.refreshAccountInfo()
                                        }
                                    )
                                    AccountBalanceCard(
                                        info = accountInfo,
                                        showAddOpt = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}
