package com.ittelekom.app.layouts.profile

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ittelekom.app.components.ButtonLoadingIndicator
import com.ittelekom.app.layouts.LoginActivity
import com.ittelekom.app.ui.theme.ITTelekomTheme
import com.ittelekom.app.utils.TokenManager
import com.ittelekom.app.viewmodels.BaseViewModel
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

    val tokenManager = TokenManager.getInstance(context)
    val accounts = tokenManager.getAllAccounts()
    val activeAccount = tokenManager.getActiveAccount()

    val isLoadingItem = viewModel.isLoadingItemState()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    LaunchedEffect(Unit) {
        viewModel.errorFlow.collect { error ->
            if (error.isNotBlank()) {
                snackbarHostState.showSnackbar(error)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("Управление аккаунтом", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { innerPadding ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()) {
                    item {
                        AccountsCard(
                            accounts = accounts,
                            activeAccount = activeAccount,
                            onLogout = { account -> viewModel.logout(BaseViewModel.State.LOADING_ITEM, account) },
                            isLoadingItem = isLoadingItem
                        )
                    }
                    item {
                        AddAccountButton {
                            val intent = Intent(context, LoginActivity::class.java).apply {
                                putExtra("isAddingAccount", true)
                            }
                            context.startActivity(intent)
                        }
                    }
                }
            }
        }
    )
}

@Composable
private fun AccountsCard(
    accounts: Set<String>,
    activeAccount: String?,
    onLogout: (String) -> Unit,
    isLoadingItem: Boolean = false
) {
    val accountsList = accounts.toList()
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp)) {
            Text(
                text = "Доступные аккаунты",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            accountsList.forEachIndexed { index, account ->
                AccountRow(
                    account = account,
                    isActive = account == activeAccount,
                    onLogout = { onLogout(account) },
                    isLoading = isLoadingItem
                )
                if (index < accountsList.lastIndex) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                }
            }
        }
    }
}

@Composable
private fun AccountRow(account: String, isActive: Boolean, onLogout: () -> Unit, isLoading: Boolean = false) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = account,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (isActive) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Outlined.Check,
                    contentDescription = "Active Account",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
        IconButton(onClick = onLogout, modifier = Modifier.size(40.dp)) {
            if (isLoading) {
                ButtonLoadingIndicator()
            } else {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Logout",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun AddAccountButton(onClick: () -> Unit) {
    Button(
        modifier = Modifier
            .padding(vertical = 16.dp)
            .fillMaxWidth(),
        onClick = onClick
    ) {
        Text("Добавить аккаунт")
    }
}


