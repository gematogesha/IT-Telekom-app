package com.example.it_telekom_app.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.example.it_telekom_app.utils.TokenManager
import com.example.it_telekom_app.network.RetrofitInstance
import com.example.it_telekom_app.ui.theme.LoginActivityTheme
import com.example.it_telekom_app.ui.theme.Typography
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Проверяем наличие токена через TokenManager
        val token = TokenManager.getInstance(this).getToken()
        if (token != null) {
            Log.d("LoginActivity", "User is logged in, navigating to Dashboard")
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
            return
        }
        setContent {
            LoginActivityTheme {
                window.statusBarColor = MaterialTheme.colorScheme.surface.toArgb()
                window.navigationBarColor = MaterialTheme.colorScheme.surface.toArgb()
                LoginScreen()
            }
        }
    }
}

fun getEncryptedSharedPreferences(context: Context): SharedPreferences {
    val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

    return EncryptedSharedPreferences.create(
        "encrypted_user_prefs",
        masterKeyAlias,
        context,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )
}

fun saveToken(context: Context, token: String) {
    TokenManager.getInstance(context).saveToken(token)
}

fun isUserLoggedIn(context: Context): Boolean {
    val sharedPreferences = getEncryptedSharedPreferences(context)
    val token = sharedPreferences.getString("user_token", null)
    return token != null
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
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
                    textStyle = Typography.bodyMedium,
                    onValueChange = { login = it },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedTextColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedTextColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedLabelColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedLabelColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    label = { Text("Логин") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Person,
                            contentDescription = "User Icon",
                            tint = if (login.isNotEmpty())
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp, start = 40.dp, end = 40.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = password,
                    textStyle = Typography.bodyMedium,
                    onValueChange = { password = it },
                    label = { Text("Пароль") },
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        unfocusedTextColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedTextColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedBorderColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedBorderColor = MaterialTheme.colorScheme.primaryContainer,
                        unfocusedLabelColor = MaterialTheme.colorScheme.secondaryContainer,
                        focusedLabelColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedLeadingIconColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Lock,
                            contentDescription = "Lock Icon",
                            tint = if (login.isNotEmpty())
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.secondaryContainer
                        )
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 30.dp, start = 40.dp, end = 40.dp),
                    singleLine = true
                )

                Button(
                    onClick = {
                        isLoading = true
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                val response = RetrofitInstance.api.login(login, password)
                                Log.d("Responce", response.body().toString())
                                if (response.isSuccessful && response.body() != null) {
                                    val token = response.body()!!.string().trim()
                                    saveToken(context, token)

                                    withContext(Dispatchers.Main) {
                                        isLoading = false
                                        context.startActivity(Intent(context, DashboardActivity::class.java))
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
                                    snackbarHostState.showSnackbar("Ошибка сети: ${e.message}")
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
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
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