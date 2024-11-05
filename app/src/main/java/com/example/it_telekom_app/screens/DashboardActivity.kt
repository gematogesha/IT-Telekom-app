package com.example.it_telekom_app.screens

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.it_telekom_app.utils.TokenManager
import com.example.it_telekom_app.ui.theme.ITTelekomTheme


class DashboardActivity : ComponentActivity() {
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

                DashboardScreen()
            }
        }
    }
}

@Preview
@Composable
fun DashboardScreen() {
    val navController = rememberNavController()
    val token = TokenManager.getInstance(LocalContext.current).getToken()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(token)
            }
            composable("payment") {
                PaymentScreen()
            }
            composable("chat") {
                ChatScreen()
            }
            composable("statistics") {
                StatisticsScreen()
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

    val items = listOf("Главная", "Оплата", "Чат", "Профиль")
    val labels = listOf("home", "payment", "chat", "profile")
    val selectedIcons = listOf(Icons.Filled.Home, Icons.Filled.AccountBalanceWallet, Icons.Filled.ChatBubble, Icons.Filled.Person)
    val unselectedIcons = listOf(Icons.Outlined.Home, Icons.Outlined.AccountBalanceWallet, Icons.Outlined.ChatBubbleOutline, Icons.Outlined.Person)

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
    ) {
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

@Composable
fun PaymentScreen() {
    Text("Profile Screen", modifier = Modifier.fillMaxSize(), fontSize = 20.sp)
}

@Composable
fun ChatScreen() {
    Text("Chat Screen", modifier = Modifier.fillMaxSize(), fontSize = 20.sp)
}

@Composable
fun StatisticsScreen() {
    Text("Statistics Screen", modifier = Modifier.fillMaxSize(), fontSize = 20.sp)
}