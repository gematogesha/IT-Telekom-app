package com.example.it_telekom_app.screens

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.it_telekom_app.network.RetrofitInstance
import com.example.it_telekom_app.utils.TokenManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ProfileScreen() {
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var isLoggingOut by remember { mutableStateOf(false) }

    Scaffold(
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
                Text("Profile Screen", fontSize = 20.sp)

                Button(
                    onClick = {
                        isLoggingOut = true
                        logout(context, snackbarHostState) {
                            isLoggingOut = false
                        }
                    },
                    enabled = !isLoggingOut
                ) {
                    if (isLoggingOut) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text("Выход")
                    }
                }
            }
        }
    }
}

fun logout(context: Context, snackbarHostState: SnackbarHostState, onComplete: () -> Unit) {
    val tokenManager = TokenManager.getInstance(context)
    val token = tokenManager.getToken()

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
                    // Успешно удалили токен на сервере
                    tokenManager.clearToken()
                    Log.d("TokenManager", "Token cleared, user logged out")
                    proceedToLogout(context)
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