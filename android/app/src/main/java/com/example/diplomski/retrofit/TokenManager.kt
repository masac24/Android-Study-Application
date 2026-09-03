package com.example.diplomski.retrofit

import android.content.Context
import android.content.SharedPreferences

object TokenManager {
    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    }

    fun saveToken(token: String) = prefs.edit().putString("token", token).apply()
    fun getToken(): String? = prefs.getString("token", null)
    fun clearToken() = prefs.edit().remove("token").apply()

    fun saveUserId(id: Long) = prefs.edit().putLong("userId", id).apply()
    fun getUserId(): Long = prefs.getLong("userId", -1)
}