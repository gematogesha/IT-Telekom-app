package com.ittelekom.app.layouts.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ittelekom.app.components.ButtonLoadingIndicator
import com.ittelekom.app.components.CustomLoadingIndicator
import com.ittelekom.app.components.PullRefresh
import com.ittelekom.app.ui.theme.ITTelekomTheme
import com.ittelekom.app.ui.util.ErrorDisplay
import com.ittelekom.app.viewmodels.AccountViewModel
import com.ittelekom.app.viewmodels.BaseViewModel
import com.ittelekom.app.viewmodels.TariffViewModel
import java.text.NumberFormat
import java.util.Locale

class ChangeTariffActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ITTelekomTheme(window = window) {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                ChangeTariffScreen(onBackPressed = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeTariffScreen(onBackPressed: () -> Unit) {

    val viewModel: TariffViewModel = viewModel()
    val accountViewModel: AccountViewModel = viewModel()

    val tariffs = viewModel.tariffs
    val accountInfo = accountViewModel.accountInfo

    val snackbarHostState = remember { SnackbarHostState() }

    val errorMessage = viewModel.errorMessage
    val isRefreshing = viewModel.isRefreshingState()
    val isLoading = viewModel.isLoadingState()
    val isLoadingItem = viewModel.isLoadingItemState()

    // Загружаем данные при первом запуске
    LaunchedEffect(Unit) {
        if (tariffs == null) viewModel.loadTariffInfo(BaseViewModel.State.LOADING)
        if (accountInfo == null) accountViewModel.loadAccountInfo(BaseViewModel.State.LOADING)

        // Отслеживаем поток ошибок и показываем снэкбар
        viewModel.errorFlow.collect { error ->
            if (error.isNotBlank()) {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    // TODO: Добавить отображение ошибки изменения тарифа
    /*LaunchedEffect(tariffChangeMessage) {
        tariffChangeMessage?.let {
            val result = snackbarHostState.showSnackbar(
                withDismissAction = isTariffChangeSuccessful,
                message = it,
                actionLabel = if (isTariffChangeSuccessful) "Отменить" else null
            )
            if (result == SnackbarResult.ActionPerformed && isTariffChangeSuccessful) {
                viewModel.undoChangeTariff()
            }
        }
    }*/

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Изменить тариф",
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

        PullRefresh(
            refreshing = isRefreshing,
            enabled = true,
            onRefresh = { viewModel.pullToRefreshTariffInfo() },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {

            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    when {
                        isLoading -> CustomLoadingIndicator()

                        tariffs != null && accountInfo != null && tariffs.error == null -> {

                            val tariffsList = tariffs.tariffs
                            val radioOptions = tariffsList.map { it.caption }

                            // Запоминаем выбранный тариф
                            var selectedOption by remember {
                                mutableStateOf(
                                    accountInfo.tariff_caption.takeIf { it in radioOptions }
                                        ?: radioOptions.firstOrNull().orEmpty()
                                )
                            }

                            if (tariffsList.isEmpty()) {
                                Text(
                                    text = "Нет доступных тарифов",
                                    modifier = Modifier.padding(16.dp)
                                )
                            } else {
                                LazyColumn(modifier = Modifier.fillMaxSize()) {
                                    item {
                                        Column(Modifier.selectableGroup()) {
                                            tariffsList.forEach { tariff ->

                                                val isCurrentTariff = tariff.caption == accountInfo.tariff_caption

                                                // Форматируем скорость и абонплату
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
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .selectable(
                                                            selected = tariff.caption == selectedOption,
                                                            onClick = {
                                                                if (!isCurrentTariff) selectedOption = tariff.caption
                                                            },
                                                            enabled = !isCurrentTariff,
                                                            role = Role.RadioButton
                                                        ),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    ListItem(
                                                        headlineContent = {
                                                            Text(
                                                                tariff.caption + if (isCurrentTariff) " (Активный)" else ""
                                                            )
                                                        },
                                                        supportingContent = {
                                                            Text("Скорость: $formattedSpeed, Абон. плата: $formattedAbonplata")
                                                        },
                                                        leadingContent = {
                                                            RadioButton(
                                                                selected = tariff.caption == selectedOption,
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
                                                .padding(top = 16.dp, start = 40.dp, end = 40.dp),
                                        ) {
                                            Button(
                                                modifier = Modifier.fillMaxWidth(),
                                                enabled = !isLoadingItem,
                                                onClick = {
                                                    tariffsList.find { it.caption == selectedOption }?.let { selectedTariff ->
                                                        viewModel.changeTariff(selectedTariff.id)
                                                    }
                                                }
                                            ) {
                                                if (isLoadingItem) {
                                                    ButtonLoadingIndicator()
                                                } else {
                                                    Text("Сменить тариф")
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        errorMessage != null -> ErrorDisplay(
                            onRefreshClick = { viewModel.refreshTariffInfo() },
                            errorMessage = errorMessage,
                            modifier = Modifier.align(Alignment.Center),
                        )
                    }
                }
            }
        }
    }
}



