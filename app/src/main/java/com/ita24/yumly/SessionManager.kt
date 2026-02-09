package com.ita24.yumly

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREFS_NAME = "YumlySessionPrefs"
    private const val USERNAME_KEY = "LOGGED_IN_USERNAME"


    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, username: String) {
        val editor = getPrefs(context).edit()
        editor.putString(USERNAME_KEY, username)
        editor.apply()
    }

    fun getSession(context: Context): String? {
        return getPrefs(context).getString(USERNAME_KEY, null)
    }

    fun clearSession(context: Context) {
        val editor = getPrefs(context).edit()
        editor.remove(USERNAME_KEY)
        editor.apply()
    }
}
