package com.example.it_telekom_app.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NoEncryption
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.SignalWifiConnectedNoInternet4
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.it_telekom_app.components.PullRefresh
import com.example.it_telekom_app.models.AccountInfo
import com.example.it_telekom_app.models.ServiceInfo
import com.example.it_telekom_app.utils.TokenManager
import com.example.it_telekom_app.viewmodels.HomeViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val tokenManager = TokenManager.getInstance(context)
    val accounts = tokenManager.getAllAccounts().toList()
    var selectedAccount by remember { mutableStateOf(tokenManager.getActiveAccount()) }
    val viewModel: HomeViewModel = viewModel()
    val accountInfo = viewModel.accountInfo
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val isRefreshing = viewModel.isRefreshing
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(selectedAccount) {
        if (selectedAccount != null) {
            tokenManager.setActiveAccount(selectedAccount!!)
            if (!viewModel.isDataLoaded) {
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
                    viewModel.refreshAccountInfo()
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
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                            ) {
                                if (accountInfo != null) {
                                    item {
                                        CardTop(
                                            info = accountInfo,
                                            accounts = accounts,
                                            selectedAccount = selectedAccount,
                                            onAccountSelected = { account ->
                                                selectedAccount = account
                                                viewModel.isDataLoaded = false
                                            }
                                        )
                                        AccountBalanceCard(accountInfo)
                                    }

                                    accountInfo.services.let { services ->
                                        items(services) { service ->
                                            TariffCard(service)
                                        }
                                    }
                                }
                            }
                        }

                        if (errorMessage != null && accountInfo == null) {
                            Icon(
                                imageVector = if (errorMessage == "Нет подключения к интернету") {
                                    Icons.Rounded.SignalWifiConnectedNoInternet4
                                } else {
                                    Icons.Outlined.CloudOff
                                },
                                tint = MaterialTheme.colorScheme.inverseOnSurface,
                                contentDescription = "Error Icon",
                                modifier = Modifier
                                    .size(170.dp)
                                    .align(Alignment.Center)
                            )
                            Text(
                                modifier = Modifier.align(Alignment.Center),
                                text = errorMessage ?: "Ошибка загрузки данных"
                            )
                        }
                    }
                }
            }
        }
    )
}


@Composable
fun CardTop(
    info: AccountInfo,
    accounts: List<String>,
    selectedAccount: String?,
    onAccountSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val userAbbreviatedName = abbreviateName(info.name)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 11.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Person,
                    contentDescription = "User Icon",
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(32.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text(
                            text = userAbbreviatedName ?: "Выберите аккаунт",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "№" + info.num_dog,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.KeyboardArrowDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    accounts.forEach { account ->
                        DropdownMenuItem(
                            enabled = selectedAccount != account,
                            leadingIcon = {
                              Icon(
                                  imageVector = Icons.Rounded.Person,
                                  contentDescription = "User Icon",
                                  modifier = Modifier.size(32.dp)
                              )
                            },
                            text = {
                                Column {
                                    if (selectedAccount != null) {
                                        Text(
                                            text = info.name,
                                            style = MaterialTheme.typography.bodyLarge,
                                        )
                                    }
                                    Text(
                                        text = "№$account",
                                        style = MaterialTheme.typography.bodySmall,
                                    )
                                }
                            },
                            onClick = {
                                expanded = false
                                onAccountSelected(account)
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(MaterialTheme.colorScheme.inverseOnSurface, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (info.userblock) {
                        Icons.Outlined.Lock
                    } else {
                        Icons.Outlined.NoEncryption
                    },
                    tint = MaterialTheme.colorScheme.onSurface,
                    contentDescription = "User Locked Icon",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

fun abbreviateName(fullName: String): String {
    val parts = fullName.trim().split("\\s+".toRegex())
    if (parts.isEmpty()) return fullName

    val surname = parts[0]
    val initials = parts.drop(1).mapNotNull { part ->
        part.firstOrNull()?.let { "${it.uppercaseChar()}." }
    }.joinToString("")

    return if (initials.isNotEmpty()) "$surname $initials" else surname
}
@Composable
fun AccountBalanceCard(accountInfo: AccountInfo) {
    val formatter = DateTimeFormatter.ofPattern("dd.MM")
    val current = LocalDateTime.now().format(formatter)

    val formatterDate = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    val payToDate = accountInfo.payToDate?.to_date?.let { LocalDate.parse(it, formatterDate) }
    val currentDate = LocalDate.now()
    val isPayToDateExpired = payToDate?.isBefore(currentDate) == true

    val balance = accountInfo.balance.replace("\u00a0", "").replace("руб.", "").trim().toDoubleOrNull()

    val formattedBalance = balance?.let {
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            groupingSeparator = ' ' // Используем пробел в качестве разделителя
        }
        val decimalFormat = DecimalFormat("#,###", symbols)
        "${decimalFormat.format(it)} \u20BD" // Добавляем символ рубля
    } ?: "0 \u20BD"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    horizontalAlignment = Alignment.Start,
                ) {
                    Text(
                        text = "Лицевой счет",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = accountInfo.num_dog,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = if (isPayToDateExpired) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 26.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (isPayToDateExpired) "Просрочен" else "Активен",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = ("до " + accountInfo.payToDate?.to_date) ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Баланс на $current",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedBalance,
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.AccountBalanceWallet,
                    contentDescription = "Wallet Icon",
                    modifier = Modifier
                        .size(60.dp)
                        .background(MaterialTheme.colorScheme.secondary, shape = CircleShape)
                        .padding(12.dp),
                    tint = MaterialTheme.colorScheme.onSecondary
                )
            }
        }
    }
}

@Composable
fun TariffCard(service: ServiceInfo) {
    val context = LocalContext.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .wrapContentHeight(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 18.dp, vertical = 16.dp),
        ) {
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
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = service.svc_name.substringAfter(": ").trim(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = service.svc_descr.substringAfter(": ").removeSuffix(".").trim(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Баланс на",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Баланс на",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Изменить тариф",
                    style = MaterialTheme.typography.bodySmall,
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
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}
