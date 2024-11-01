package com.example.it_telekom_app

import android.content.Context
import android.content.SharedPreferences

class TokenManager private constructor(context: Context) {
    private val preferences: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    companion object {
        private const val PREFS_NAME = "token_prefs"
        private const val TOKEN_KEY = "auth_token"
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            if (instance == null) {
                instance = TokenManager(context)
            }
            return instance!!
        }
    }

    fun saveToken(token: String) {
        preferences.edit().putString(TOKEN_KEY, token).apply()
    }

    fun getToken(): String? {
        return preferences.getString(TOKEN_KEY, null)
    }

    fun clearToken() {
        preferences.edit().remove(TOKEN_KEY).apply()
    }
}
