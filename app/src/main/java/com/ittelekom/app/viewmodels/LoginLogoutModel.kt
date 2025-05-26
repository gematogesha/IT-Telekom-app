package com.ittelekom.app.viewmodels

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import com.ittelekom.app.layouts.DashboardActivity
import com.ittelekom.app.layouts.LoginActivity
import com.ittelekom.app.network.RetrofitInstance
import com.ittelekom.app.utils.TokenManager

class LoginLogoutModel(application: Application) : BaseViewModel(application) {
    //TODO implement login and logout
    fun login(context: Context, login: String, password: String, onComplete: (Boolean) -> Unit) {
        fetchData(
            state = State.LOADING_ITEM,
            requests = listOf(
                { RetrofitInstance.api.login(login, password) }
            )
        ) { responses ->
            val response = responses[0]

            if (response.toString().isNotEmpty()) {
                TokenManager.getInstance(getApplication()).saveToken(login, response.toString())
                TokenManager.getInstance(context).setActiveAccount(login)

                context.startActivity(
                    Intent(context, DashboardActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    }
                )
                (context as? ComponentActivity)?.finish()
                onComplete(true)
            } else {
                Log.e("LoginError", "Token is null or empty")
                onComplete(false)
            }
        }
    }

    fun logout(account: String?) {
        val tokenManager = TokenManager.getInstance(getApplication())
        val token = account?.let { tokenManager.getToken(it) }

        if (token == null) {
            Log.e("Logout", "Токен отсутствует")
            proceedToLogout(getApplication())
            return
        }

        fetchData(
            state = State.LOADING_ITEM,
            requests = listOf { RetrofitInstance.api.logout("Bearer $token") }
        ) { responses ->
            val response = responses[0]

            if (response != null && account != null) {
                tokenManager.removeToken(account)
                val remainingAccounts = tokenManager.getAllAccounts()

                if (remainingAccounts.isNotEmpty()) {
                    tokenManager.setActiveAccount(remainingAccounts.first())
                    AccountViewModel(getApplication()).refreshAccountInfo()
                } else {
                    proceedToLogout(getApplication())
                }
            } else {
                Log.e("Logout", "Не удалось выполнить выход")
            }
        }
    }

    private fun proceedToLogout(context: Context) {
        val intent = Intent(context, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        context.startActivity(intent)
    }

}