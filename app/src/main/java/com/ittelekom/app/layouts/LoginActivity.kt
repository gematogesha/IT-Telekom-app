package com.ittelekom.app.layouts

import ThemeManager
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.ittelekom.app.components.ButtonLoadingIndicator
import com.ittelekom.app.network.RetrofitInstance
import com.ittelekom.app.ui.theme.LoginActivityTheme
import com.ittelekom.app.ui.theme.Typography
import com.ittelekom.app.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ThemeManager.loadTheme(this)

        var showSplashScreen = true
        val isAddingAccount = intent.getBooleanExtra("isAddingAccount", false)

        if (!isAddingAccount) {
            val activeAccount = TokenManager.getInstance(this).getActiveAccount()
            val token = activeAccount?.let { TokenManager.getInstance(this).getToken(it) }

            if (token != null) {
                Log.d("LoginActivity", "User is logged in as $activeAccount, navigating to Dashboard")
                startActivity(Intent(this, DashboardActivity::class.java))
                finish()
                return
            }
        }

        lifecycleScope.launch {
            delay(1000) //Simulates checking if the user is logged in
            showSplashScreen = false
        }

        installSplashScreen().apply {
            this.setKeepOnScreenCondition {
                showSplashScreen
            }
        }

        setContent {
            LoginActivityTheme(window = window) {
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()
                window.navigationBarColor = MaterialTheme.colorScheme.surface.toArgb()
                LoginScreen()
            }
        }
    }
}

fun saveToken(context: Context, username: String, token: String) {
    TokenManager.getInstance(context).saveToken(username, token)
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen() {
    val snackbarHostState = remember { SnackbarHostState() }
    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxSize()
        ) {
            var login by remember { mutableStateOf("703462697") }
            var password by remember { mutableStateOf("o538ws8cze") }
            var isLoading by remember { mutableStateOf(false) }
            val context = LocalContext.current

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {

                Text(
                    text = "Вход в личный кабинет",
                    style = Typography.titleLarge,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = login,
                    onValueChange = { login = it },
                    label = { Text("Логин") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "User Icon",
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, start = 40.dp, end = 40.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Lock Icon",
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp, start = 40.dp, end = 40.dp),
                    singleLine = true
                )

                //TODO: Новая функция login

                Button(
                    onClick = {
                        if (login.isBlank() || password.isBlank()) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Заполните все поля")
                            }
                        } else if (TokenManager.getInstance(context).hasAccount(login)) {
                            CoroutineScope(Dispatchers.Main).launch {
                                snackbarHostState.showSnackbar("Этот аккаунт уже добавлен")
                            }
                        } else {
                            isLoading = true
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val response = RetrofitInstance.api.login(login, password)
                                    if (response.isSuccessful && response.body() != null) {
                                        val token = response.body()!!.string().trim()
                                        saveToken(context, login, token)
                                        TokenManager.getInstance(context).setActiveAccount(login)

                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            context.startActivity(
                                                Intent(
                                                    context,
                                                    DashboardActivity::class.java
                                                )
                                            )
                                            (context as? ComponentActivity)?.finish()
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            isLoading = false
                                            snackbarHostState.showSnackbar("Ошибка аутентификации")
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        Log.e("LoginError", "Error: ${e.message}", e)
                                        snackbarHostState.showSnackbar("Ошибка аутентификации")
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp, start = 40.dp, end = 40.dp),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        ButtonLoadingIndicator()
                    } else {
                        Text("Войти")
                    }
                }

                Text(
                    text = "Нет аккаунта? Зарегистрируйтесь",
                    modifier = Modifier
                        .clickable { /* Логика для перехода на экран регистрации */ },
                    fontSize = 16.sp
                )
            }
        }
    }
}