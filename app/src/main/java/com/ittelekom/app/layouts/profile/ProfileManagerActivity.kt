package com.ittelekom.app.layouts.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ittelekom.app.components.ButtonLoadingIndicator
import com.ittelekom.app.layouts.LoginActivity
import com.ittelekom.app.ui.theme.ITTelekomTheme
import com.ittelekom.app.utils.TokenManager
import com.ittelekom.app.viewmodels.LoginLogoutModel

class ProfileManagerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ITTelekomTheme(window = window) {
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

    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: LoginLogoutModel = viewModel()
    val isLoading = viewModel.isLoadingItemState()

    val tokenManager = TokenManager.getInstance(context)
    val accounts = tokenManager.getAllAccounts()
    val activeAccount = tokenManager.getActiveAccount()

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
                            viewModel.logout(account)
                        }) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Удалить аккаунт")
                        }
                    }
                }

                Button(
                    onClick = {
                        viewModel.logout(activeAccount)
                    },
                    enabled = !isLoading
                ) {
                    if (isLoading) {
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
