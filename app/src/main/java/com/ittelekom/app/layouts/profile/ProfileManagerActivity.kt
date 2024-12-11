package com.ittelekom.app.layouts.profile

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.ittelekom.app.components.ButtonLoadingIndicator
import com.ittelekom.app.layouts.LoginActivity
import com.ittelekom.app.layouts.settings.SettingsActivity
import com.ittelekom.app.layouts.settings.SettingsScreen
import com.ittelekom.app.network.RetrofitInstance
import com.ittelekom.app.ui.theme.ITTelekomTheme
import com.ittelekom.app.ui.util.SetSystemBarsColor
import com.ittelekom.app.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ITTelekomTheme {
                WindowCompat.setDecorFitsSystemWindows(window, false)
                ProfileManagerScreen(onBackPressed = { finish() })
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileManagerScreen(onBackPressed: () -> Unit) {

    SetSystemBarsColor()

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoggingOut by remember { mutableStateOf(false) }

    val tokenManager = TokenManager.getInstance(context)
    val accounts = tokenManager.getAllAccounts()
    val activeAccount = tokenManager.getActiveAccount()

    var expanded by remember { mutableStateOf(false) }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Управление аккаунтами", maxLines = 1, overflow = TextOverflow.Ellipsis) },
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
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {

                Text("Профиль", fontSize = 20.sp)

                Text("Активный аккаунт: $activeAccount", fontSize = 16.sp)
                Text("Доступные аккаунты:", fontSize = 16.sp)

                accounts.forEach { account ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                tokenManager.setActiveAccount(account)
                            }
                    ) {
                        Text(account)
                        IconButton(onClick = {
                            if (account == activeAccount) {
                                val remainingAccounts = tokenManager.getAllAccounts()
                                isLoggingOut = true
                                logout(context, account, snackbarHostState) {
                                    isLoggingOut = false
                                    tokenManager.removeToken(account)
                                }
                                if (remainingAccounts.isNotEmpty()) {
                                    tokenManager.setActiveAccount(remainingAccounts.first())
                                } else {
                                    proceedToLogout(context)
                                }
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Удалить аккаунт")
                        }
                    }
                }

                Button(
                    onClick = {
                        isLoggingOut = true
                        logout(context, activeAccount, snackbarHostState) {
                            isLoggingOut = false
                        }
                    },
                    enabled = !isLoggingOut
                ) {
                    if (isLoggingOut) {
                        ButtonLoadingIndicator()
                    } else {
                        Text("Выход")
                    }
                }

                Button(onClick = {
                    val intent = Intent(context, LoginActivity::class.java)
                    intent.putExtra("isAddingAccount", true)
                    context.startActivity(intent)
                }) {
                    Text("Добавить аккаунт")
                }
            }
        }
    }
}

fun logout(context: Context, account: String?, snackbarHostState: SnackbarHostState, onComplete: () -> Unit) {
    val tokenManager = TokenManager.getInstance(context)
    val token = account?.let { tokenManager.getToken(it) }

    if (token == null) {
        proceedToLogout(context)
        onComplete()
        return
    }

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.logout("Bearer $token")
            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    tokenManager.removeToken(account)
                    Log.d("TokenManager", "Token cleared, $account logged out")
                    val remainingAccounts = tokenManager.getAllAccounts()
                    if (remainingAccounts.isNotEmpty()) {
                        tokenManager.setActiveAccount(remainingAccounts.first())
                    } else {
                        proceedToLogout(context)
                    }
                } else {
                    Log.e("Logout", "Failed to logout: ${response.message()}")
                    snackbarHostState.showSnackbar("Ошибка аутентификации")
                }
                onComplete()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("Logout", "Error during logout request: ${e.message}", e)
                snackbarHostState.showSnackbar("Ошибка аутентификации")
                onComplete()
            }
        }
    }
}
private fun proceedToLogout(context: Context) {
    val intent = Intent(context, LoginActivity::class.java)
    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
    context.startActivity(intent)
}