package com.ittelekom.app.layouts.profile

import android.annotation.SuppressLint
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.ManageAccounts
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ittelekom.app.components.ButtonLargeLoadingIndicator
import com.ittelekom.app.components.ButtonLoadingIndicator
import com.ittelekom.app.layouts.settings.SettingsActivity
import com.ittelekom.app.utils.TokenManager
import com.ittelekom.app.viewmodels.AccountViewModel
import com.ittelekom.app.viewmodels.BaseViewModel
import com.ittelekom.app.viewmodels.LoginLogoutModel
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen(accountViewModel: AccountViewModel) {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val viewModel: LoginLogoutModel = viewModel()

    val userBlock = accountViewModel.accountInfo?.userblock ?: false
    val userMacBase = accountViewModel.accountInfo?.hw_addr_for_dhcp ?: "00:00:00:00:00"
    val userMac = if (userMacBase.isNullOrBlank()) "00:00:00:00:00" else userMacBase
    val isLoadingLogout = viewModel.isLoadingItemState()
    val isLoadingItem = accountViewModel.isLoadingItemState()
    val isLoadingBlock = accountViewModel.isLoadingItemState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val openDialogMac = remember { mutableStateOf(false) }
    val macAddress = remember { mutableStateOf("") }
    val localErrorMessage = remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        launch {
            accountViewModel.errorFlow.collect { error ->
                if (error.isNotBlank()) {
                    snackbarHostState.showSnackbar(error)
                }
            }
        }
        launch {
            viewModel.errorFlow.collect { error ->
                if (error.isNotBlank()) {
                    snackbarHostState.showSnackbar(error)
                }
            }
        }
        launch {
            snapshotFlow { localErrorMessage.value }
                .filter { it.isNotBlank() }
                .collect {
                    snackbarHostState.showSnackbar(it)
                    localErrorMessage.value = ""
                }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Профиль", maxLines = 1, overflow = TextOverflow.Ellipsis)
                },
                scrollBehavior = scrollBehavior
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        content = { innerPadding ->
            Surface(
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    contentPadding = innerPadding,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Аватар
                    item {
                        Row(
                            modifier = Modifier
                                .padding(bottom = 24.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(104.dp)
                                    .background(MaterialTheme.colorScheme.secondaryContainer, shape = CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Person,
                                    contentDescription = "User Icon",
                                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.size(64.dp)
                                )
                            }
                        }
                    }
                    // Имя пользователя
                    item {
                        val activeAccount = TokenManager.getInstance(context).getActiveAccount()
                        Row(
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            if (activeAccount != null) {
                                Text(
                                    text = activeAccount,
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                    // Блокировка аккаунта
                    item {
                        Card(
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(2.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Rounded.Lock,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Блокировка аккаунта",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                SetBlockSwitch(
                                    isBlocked = userBlock,
                                    isLoading = isLoadingBlock,
                                    onToggle = {
                                        accountViewModel.loadSetBlock(BaseViewModel.State.LOADING_ITEM)
                                    }
                                )
                            }
                        }
                    }
                    // Смена MAC-адреса
                    item {
                        ProfileCard(
                            icon = Icons.Rounded.Settings,
                            text = "Смена MAC-адреса",
                            onClick = {
                                openDialogMac.value = true
                            }
                        )
                        if (openDialogMac.value) {
                            DialogMac(
                                isLoadingItem = isLoadingItem,
                                onDismissRequest = { openDialogMac.value = false },
                                accountViewModel = accountViewModel,
                                userMac = userMac,
                                macAddressState = macAddress, // ✅ передаём state, не value
                                localErrorMessageState = localErrorMessage
                            )
                        }
                    }
                    // Управление аккаунтом
                    item {
                        ProfileCard(
                            icon = Icons.Rounded.ManageAccounts,
                            text = "Управление аккаунтом",
                            onClick = {
                                context.startActivity(Intent(context, ProfileManagerActivity::class.java))
                            }
                        )
                    }
                    // Настройки
                    item {
                        ProfileCard(
                            icon = Icons.Rounded.Settings,
                            text = "Настройки",
                            onClick = {
                                context.startActivity(Intent(context, SettingsActivity::class.java))
                            }
                        )
                    }
                    // Контакты
                    item {
                        ProfileCard(
                            icon = Icons.Outlined.Contacts,
                            text = "Контакты",
                            onClick = {
                                context.startActivity(Intent(context, ContactsScreen::class.java))
                            }
                        )
                    }
                    // Кнопка Выйти
                    item {
                        Row(
                            modifier = Modifier
                                .padding(vertical = 24.dp)
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = {
                                    val activeAccount = TokenManager.getInstance(context).getActiveAccount()
                                    viewModel.logout(BaseViewModel.State.LOADING_ITEM, activeAccount)
                                },
                                enabled = !isLoadingLogout,
                            ) {
                                if (isLoadingLogout) {
                                    ButtonLoadingIndicator()
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                                        contentDescription = "Logout",
                                        tint = MaterialTheme.colorScheme.onPrimary,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Выйти")
                                }
                            }
                        }
                    }
                }
            }
        }
    )
}

@Composable
fun ProfileCard(icon: ImageVector, text: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = text, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            }
            IconButton(onClick = onClick, modifier = Modifier.size(40.dp)) {
                Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun SetBlockSwitch(
    isBlocked: Boolean,
    isLoading: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier.size(40.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            ButtonLargeLoadingIndicator()
        } else {
            Switch(
                checked = isBlocked,
                onCheckedChange = onToggle,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

@Composable
fun DialogMac(
    onDismissRequest: () -> Unit,
    isLoadingItem: Boolean = false,
    accountViewModel: AccountViewModel,
    userMac: String,
    macAddressState: MutableState<String>,
    localErrorMessageState: MutableState<String>
) {
    AlertDialog(
        text = {
            Column() {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Текущий MAC-адрес:",
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(bottom = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = userMac,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    MacAddressInput(
                        macAddress = macAddressState.value,
                        onMacAddressChange = { macAddressState.value = it }
                    )
                }
            }
        },
        onDismissRequest = onDismissRequest,
        confirmButton = {
            TextButton(
                modifier = Modifier.width(120.dp),
                enabled = !isLoadingItem,
                onClick = {
                    when {
                        macAddressState.value.isBlank() -> {
                            localErrorMessageState.value = "Заполните все поля"
                        }
                        macAddressState.value.length != 17 -> {
                            localErrorMessageState.value = "Неправильно заполнен MAC-адрес"
                        }
                        else -> {
                            accountViewModel.changeMac(BaseViewModel.State.LOADING_ITEM, macAddressState.value){
                                onDismissRequest()
                            }
                        }
                    }
                },
            ) {
                if (isLoadingItem) {
                    ButtonLoadingIndicator()
                } else {
                    Text("Изменить")
                }
            }
        },
    )
}

@Composable
fun MacAddressInput(
    macAddress: String,
    onMacAddressChange: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = macAddress,
            onValueChange = { newValue ->
                val clean = newValue.filter { it.isLetterOrDigit() }.uppercase().take(12)

                val formatted = buildString {
                    clean.chunked(2).forEachIndexed { index, chunk ->
                        append(chunk)
                        if (index < 5 && chunk.length == 2) append(":")
                    }
                }

                onMacAddressChange(formatted)
            },
            label = { Text("MAC-адрес") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Ascii
            ),
            visualTransformation = VisualTransformation.None,
            singleLine = true
        )
    }
}
