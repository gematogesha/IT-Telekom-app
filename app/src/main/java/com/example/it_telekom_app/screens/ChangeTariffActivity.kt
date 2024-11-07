package com.example.it_telekom_app.screens

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.rounded.SignalWifiConnectedNoInternet4
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.it_telekom_app.components.PullRefresh
import com.example.it_telekom_app.ui.theme.ITTelekomTheme
import com.example.it_telekom_app.utils.TokenManager
import com.example.it_telekom_app.viewmodels.HomeViewModel
import com.example.it_telekom_app.viewmodels.TariffViewModel
import java.text.NumberFormat
import java.util.Locale

class ChangeTariffActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ITTelekomTheme {

                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()
                window.navigationBarColor = MaterialTheme.colorScheme.surfaceContainer.toArgb()

                val isLightBackground = MaterialTheme.colorScheme.surface.luminance() > 0.5f
                val isLightNavBackground = MaterialTheme.colorScheme.surfaceContainer.luminance() > 0.5f
                val windowInsetsController = WindowInsetsControllerCompat(window, window.decorView)
                windowInsetsController.isAppearanceLightStatusBars = isLightBackground
                windowInsetsController.isAppearanceLightNavigationBars = isLightNavBackground

                ChangeTariffScreen(onBackPressed = { finish() })
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeTariffScreen(onBackPressed: () -> Unit) {
    val token = TokenManager.getInstance(LocalContext.current).getToken()
    val viewModel: TariffViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val accountInfo = homeViewModel.accountInfo
    val context = LocalContext.current
    val tariffs = viewModel.tariffs
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val isRefreshing = viewModel.isRefreshing
    val snackbarHostState = remember { SnackbarHostState() }
    val tariffChangeMessage = viewModel.tariffChangeMessage
    val isTariffChangeSuccessful = viewModel.isTariffChangeSuccessful

    LaunchedEffect(token) {
        if (viewModel.tariffs == null && !viewModel.isLoading && !viewModel.isRefreshing) {
            token?.let {
                viewModel.loadTariffInfo(context, it)
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    LaunchedEffect(tariffChangeMessage) {
        tariffChangeMessage?.let {
            val result = snackbarHostState.showSnackbar(
                withDismissAction = isTariffChangeSuccessful,
                message = it,
                actionLabel = if (isTariffChangeSuccessful) "Отменить" else null
            )
            if (result == SnackbarResult.ActionPerformed && isTariffChangeSuccessful) {
                viewModel.undoChangeTariff(context, token)
            }
            viewModel.clearTariffChangeMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Изменить тариф")
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Назад"
                        )
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        PullRefresh(
            refreshing = isRefreshing,
            enabled = true,
            onRefresh = {
                token?.let {
                    viewModel.refreshTariffInfo(context, it)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            tariffs?.tariffs?.let { tariffsList ->
                                val radioOptions = tariffsList.map { it.caption }
                                val initialSelectedOption = accountInfo?.tariff_caption.takeIf { it in radioOptions } ?: radioOptions.firstOrNull() ?: ""
                                val (selectedOption, onOptionSelected) = remember { mutableStateOf(initialSelectedOption) }

                                if (tariffsList.isNotEmpty()) {
                                    Column(Modifier.selectableGroup()) {
                                        tariffsList.forEach { tariff ->
                                            val foramttedSpeed = tariff.speed.replace("[.*]+$".toRegex(), "")
                                            val formattedAbonplata = tariff.abonplata
                                                .replace("\u00a0", "")
                                                .replace("руб.", "")
                                                .trim()
                                                .toDoubleOrNull()
                                                ?.let {
                                                    NumberFormat.getNumberInstance(Locale("ru", "RU")).apply {
                                                        maximumFractionDigits = 0
                                                    }.format(it) + " ₽"
                                                } ?: "N/A"

                                            Row(
                                                Modifier
                                                    .fillMaxWidth()
                                                    .height(62.dp)
                                                    .selectable(
                                                        selected = (tariff.caption == selectedOption),
                                                        onClick = {
                                                            onOptionSelected(tariff.caption)
                                                        },
                                                        role = Role.RadioButton
                                                    )
                                                    .padding(horizontal = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RadioButton(
                                                    selected = (tariff.caption == selectedOption),
                                                    onClick = null // null recommended for accessibility with screenreaders
                                                )
                                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                                    Text(
                                                        text = tariff.caption,
                                                        style = MaterialTheme.typography.bodyLarge
                                                    )
                                                    Text(
                                                        text = "Скорость: ${foramttedSpeed}, Абон. плата: ${formattedAbonplata}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.outline,
                                                        modifier = Modifier.padding(top = 4.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(16.dp)
                                    ) {
                                        Button(
                                            modifier = Modifier
                                                .fillMaxWidth(),
                                            onClick = {
                                                tariffsList.find { it.caption == selectedOption }
                                                    ?.let { selectedTariff ->
                                                        viewModel.changeTariff(
                                                            context,
                                                            token,
                                                            selectedTariff.id
                                                        )
                                                    }
                                            },
                                        ) {
                                            Text(
                                                text = "Сменить тариф",
                                            )
                                        }
                                    }
                                } else {
                                    Text(
                                        text = "Нет доступных тарифов",
                                        modifier = Modifier.padding(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (errorMessage != null && tariffs == null) {
                        Icon(
                            imageVector = if (errorMessage == "Нет подключения к интернету") {
                                Icons.Rounded.SignalWifiConnectedNoInternet4
                            } else {
                                Icons.Outlined.CloudOff
                            },
                            tint = MaterialTheme.colorScheme.inverseOnSurface,
                            contentDescription = "User Locked Icon",
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
}
