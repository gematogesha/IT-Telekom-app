package com.ittelekom.app.layouts

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Leaderboard
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ittelekom.app.layouts.chat.ChatScreen
import com.ittelekom.app.layouts.home.HomeScreen
import com.ittelekom.app.layouts.payment.PaymentScreen
import com.ittelekom.app.layouts.profile.ProfileScreen
import com.ittelekom.app.layouts.statistics.StatisticsScreen
import com.ittelekom.app.ui.theme.ITTelekomTheme
import com.ittelekom.app.utils.TokenManager
import com.ittelekom.app.viewmodels.AccountViewModel


class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val tokenManager = TokenManager.getInstance(this)
        val activeAccount = tokenManager.getActiveAccount()
        val token = activeAccount?.let { tokenManager.getToken(it) }

        if (token == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        } else {

            setContent {
                ITTelekomTheme(window = window) {
                    DashboardScreen()

                }
            }
        }
    }
}

@Composable
fun DashboardScreen() {
    val navController = rememberNavController()

    val viewModel: AccountViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = {fadeIn(animationSpec = tween(200))},
            exitTransition = {fadeOut(animationSpec = tween(200))},
            popEnterTransition = {fadeIn(animationSpec = tween(200))},
            popExitTransition = {fadeOut(animationSpec = tween(200))}
        ) {
            composable("home") {
                HomeScreen(viewModel = viewModel)
            }
            composable("payment") {
                PaymentScreen(viewModel = viewModel)
            }
            /*
            composable("chat") {
                ChatScreen(viewModel = viewModel)
            }
             */
            composable("statistics") {
                StatisticsScreen(viewModel = viewModel)
            }
            composable("profile") {
                ProfileScreen()
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    var selectedItem by remember { mutableIntStateOf(0) }

    val items = listOf("Главная", "Оплата", /*"Чат",*/ "Статистика", "Профиль")
    val labels = listOf("home", "payment", /*"chat",*/ "statistics", "profile")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.AccountBalanceWallet, Icons.Filled.ChatBubble, Icons.Filled.Leaderboard, Icons.Filled.Person)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.AccountBalanceWallet, Icons.Outlined.ChatBubbleOutline, Icons.Outlined.Leaderboard, Icons.Outlined.Person)

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        if (selectedItem == index) selectedIcons[index] else unselectedIcons[index],
                        contentDescription = labels[index]
                    )
                },
                label = {
                    Text(
                        text = item,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                selected = selectedItem == index,

                onClick = {
                    selectedItem = index
                    navController.navigate(labels[index]) {
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        restoreState = true
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}