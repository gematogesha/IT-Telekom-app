package com.example.it_telekom_app.screens

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.rounded.SignalWifiConnectedNoInternet4
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    val context = LocalContext.current
    val tokenManager = TokenManager.getInstance(context)
    val activeAccount = tokenManager.getActiveAccount()

    val viewModel: TariffViewModel = viewModel()
    val homeViewModel: HomeViewModel = viewModel()
    val accountInfo = homeViewModel.accountInfo
    val tariffs = viewModel.tariffs
    val isLoading = viewModel.isLoading
    val errorMessage = viewModel.errorMessage
    val isRefreshing = viewModel.isRefreshing
    val snackbarHostState = remember { SnackbarHostState() }
    val tariffChangeMessage = viewModel.tariffChangeMessage
    val isTariffChangeSuccessful = viewModel.isTariffChangeSuccessful

    // Загрузка данных при первом запуске
    LaunchedEffect(Unit) {
        // Проверяем, есть ли активный аккаунт
        if (activeAccount != null) {
            // Загружаем данные, если еще не загружены
            if (viewModel.tariffs == null && !viewModel.isLoading && !viewModel.isRefreshing) {
                viewModel.loadTariffInfo()
            }
            if (homeViewModel.accountInfo == null && !homeViewModel.isLoading && !homeViewModel.isRefreshing) {
                homeViewModel.loadAccountInfo()
            }
        } else {
            // Если нет активного аккаунта, перенаправляем на экран входа или показываем сообщение
            // Здесь вы можете реализовать логику перенаправления
        }
    }

    // Обработка сообщений об ошибках
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
        }
    }

    // Обработка сообщений о смене тарифа
    LaunchedEffect(tariffChangeMessage) {
        tariffChangeMessage?.let {
            val result = snackbarHostState.showSnackbar(
                withDismissAction = isTariffChangeSuccessful,
                message = it,
                actionLabel = if (isTariffChangeSuccessful) "Отменить" else null
            )
            if (result == SnackbarResult.ActionPerformed && isTariffChangeSuccessful) {
                viewModel.undoChangeTariff()
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
                viewModel.refreshTariffInfo()
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
                    if (tariffs != null && accountInfo != null) {
                        val tariffsList = tariffs.tariffs
                        val radioOptions = tariffsList.map { it.caption }
                        val initialSelectedOption = accountInfo.tariff_caption.takeIf { it in radioOptions } ?: radioOptions.firstOrNull() ?: ""
                        var selectedOption by remember { mutableStateOf(initialSelectedOption) }

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                        ) {
                            item {
                                if (tariffsList.isNotEmpty()) {
                                    Column(Modifier.selectableGroup()) {
                                        tariffsList.forEach { tariff ->
                                            val formattedSpeed = tariff.speed.replace("[.*]+$".toRegex(), "")
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
                                                            selectedOption = tariff.caption
                                                        },
                                                        role = Role.RadioButton
                                                    ),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                ListItem(
                                                    headlineContent = { Text(tariff.caption) },
                                                    supportingContent = { Text("Скорость: ${formattedSpeed}, Абон. плата: ${formattedAbonplata}") },
                                                    leadingContent = {
                                                        RadioButton(
                                                            selected = (tariff.caption == selectedOption),
                                                            onClick = null
                                                        )
                                                    }
                                                )
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
                                                        viewModel.changeTariff(selectedTariff.id)
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
                    } else if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else if (errorMessage != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .align(Alignment.Center),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
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
                            )
                            Text(
                                text = errorMessage ?: "Ошибка загрузки данных"
                            )
                        }
                    }
                }
            }
        }
    }
}
