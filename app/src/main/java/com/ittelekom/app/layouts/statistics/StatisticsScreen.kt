package com.ittelekom.app.layouts.statistics

import android.annotation.SuppressLint
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.it_telekom_app.viewmodels.BaseViewModel
import com.ittelekom.app.components.CustomLoadingIndicator
import com.ittelekom.app.components.PullRefresh
import com.ittelekom.app.layouts.LoginActivity
import com.ittelekom.app.models.groupPayments
import com.ittelekom.app.ui.util.ErrorDisplay
import com.ittelekom.app.utils.TokenManager
import com.ittelekom.app.viewmodels.AccountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun StatisticsScreen(viewModel: AccountViewModel) {
    val context = LocalContext.current
    val tokenManager = TokenManager.getInstance(context)
    var selectedAccount by remember { mutableStateOf(tokenManager.getActiveAccount()) }
    val snackbarHostState = remember { SnackbarHostState() }

    val accountInfo = viewModel.accountInfo
    val groupedPayments = accountInfo?.let { groupPayments(it.pays) }
    val errorMessage = viewModel.errorMessage
    val isRefreshing = viewModel.isRefreshingState()
    val isLoading = viewModel.isLoadingState()
    var isInitialLoad by remember { mutableStateOf(true) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

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
            Log.e("StatisticsScreen", "Error: $errorMessage")
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Статистика", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { innerPadding ->
            PullRefresh(
                refreshing = isRefreshing,
                enabled = true,
                onRefresh = {
                    viewModel.pullToRefreshAccountInfo()
                },
                modifier = Modifier.fillMaxSize(),
                indicatorPadding = PaddingValues(30.dp)
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
                            if (groupedPayments != null) {
                                LazyColumn(
                                    contentPadding = innerPadding,
                                    modifier = Modifier.fillMaxSize(),
                                ) {
                                    item {
                                        CircularChartCard(groupedPayments)
                                    }
                                    groupedPayments.forEach { (caption, subgroups) ->
                                        item {
                                            Text(
                                                text = caption,
                                                style = MaterialTheme.typography.titleMedium,
                                                modifier = Modifier.padding(vertical = 8.dp)
                                            )
                                        }
                                        subgroups.forEach { (remark, totalVolume) ->
                                            item {
                                                PaymentItem(
                                                    remark = remark,
                                                    totalVolume = totalVolume
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
    )
}

@Composable
fun PaymentItem(remark: String, totalVolume: Double) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(10.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = remark,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "%.2f ₽".format(totalVolume),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun CircularChartCard(groupedPayments: Map<String, Map<String, Double>>) {
    val spendingPayments = groupedPayments["Снятие"] ?: emptyMap()
    Log.d("CircularChartCard", "spendingPayments: $spendingPayments")

    val totalAmount = spendingPayments.values.sum().toFloat()

    val colors = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.primaryContainer,
        MaterialTheme.colorScheme.secondaryContainer,
        MaterialTheme.colorScheme.error
    )
    val chartData = spendingPayments.entries.mapIndexed { index, (remark, value) ->
        remark to (value.toFloat() to colors[index % colors.size])
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularChart(
                    total = totalAmount,
                    parts = chartData.map { it.second },
                    centerText = "%.2f ₽".format(totalAmount),
                    gapAngle = 2f
                )
            }
            Column(
                modifier = Modifier.padding(top = 16.dp)
            ) {
                chartData.forEach { (remark, pair) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .background(pair.second, shape = CircleShape)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = remark,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CircularChart(
    total: Float,
    parts: List<Pair<Float, Color>>,
    centerText: String,
    gapAngle: Float
) {

    val adjustedGapAngle = if (parts.size == 1) 0f else gapAngle

    Canvas(modifier = Modifier.size(150.dp)) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val radius = minOf(canvasWidth, canvasHeight) / 2
        val center = Offset(x = canvasWidth / 2, y = canvasHeight / 2)
        val totalAngle = 360f - (adjustedGapAngle * parts.size)

        var startAngle = 0f

        parts.forEach { (value, color) ->
            val sweepAngle = (value / total) * totalAngle
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = Stroke(width = 15.dp.toPx(), cap = StrokeCap.Butt),
                topLeft = Offset(
                    center.x - radius + 20.dp.toPx(),
                    center.y - radius + 20.dp.toPx()
                ),
                size = Size(2 * (radius - 20.dp.toPx()), 2 * (radius - 20.dp.toPx()))
            )
            startAngle += sweepAngle + adjustedGapAngle
        }
    }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(150.dp)) {
        Text(
            text = centerText,
            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
