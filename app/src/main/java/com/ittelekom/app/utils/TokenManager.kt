package com.ittelekom.app.utils

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TokenManager private constructor(context: Context) {
    private val preferences = createEncryptedPreferences(context)
    private var tokenMap: MutableMap<String, String> = mutableMapOf()

    companion object {
        private const val PREFS_NAME = "secure_token_prefs"
        private const val TOKENS_KEY = "auth_tokens"
        private const val ACTIVE_ACCOUNT_KEY = "active_account"
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            if (instance == null) {
                instance = TokenManager(context)
            }
            return instance!!
        }
    }

    init {
        val jsonString = preferences.getString(TOKENS_KEY, null)
        if (jsonString != null) {
            val type = object : TypeToken<MutableMap<String, String>>() {}.type
            tokenMap = Gson().fromJson(jsonString, type)
        }
    }

    private fun saveTokens() {
        val jsonString = Gson().toJson(tokenMap)
        preferences.edit().putString(TOKENS_KEY, jsonString).apply()
    }

    fun saveToken(username: String, token: String) {
        tokenMap[username] = token
        saveTokens()
    }

    fun getToken(username: String): String? {
        return tokenMap[username]
    }

    fun removeToken(username: String) {
        tokenMap.remove(username)
        saveTokens()
    }

    fun getAllAccounts(): Set<String> {
        return tokenMap.keys
    }

    fun hasAccount(username: String): Boolean {
        return tokenMap.containsKey(username)
    }

    fun clearAllTokens() {
        tokenMap.clear()
        saveTokens()
    }

    fun setActiveAccount(username: String) {
        preferences.edit().putString(ACTIVE_ACCOUNT_KEY, username).apply()
    }

    fun getActiveAccount(): String? {
        return preferences.getString(ACTIVE_ACCOUNT_KEY, null)
    }

    // Helper function to create encrypted shared preferences
    private fun createEncryptedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
}
