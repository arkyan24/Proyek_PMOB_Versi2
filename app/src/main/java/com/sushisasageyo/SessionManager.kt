package com.sushisasageyo

import android.content.Context

class SessionManager(context: Context) {
    private val sp = context.getSharedPreferences("session", Context.MODE_PRIVATE)

    fun saveUserId(userId: String) {
        sp.edit().putString("userId", userId).apply()
    }

    fun getUserId(): String? = sp.getString("userId", null)

    fun clear() {
        sp.edit().clear().apply()
    }
}
