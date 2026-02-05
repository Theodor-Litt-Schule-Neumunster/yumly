package com.ita24.yumly
import android.content.Context

class UserDataLocal(context: Context) {

    private val prefs = context.getSharedPreferences("EloScores", Context.MODE_PRIVATE)

    fun getelo(username: String, recipe: String): Int?{
            val userKey = username
            val recipeKey = recipe

        return prefs.getInt(rezeptName, 1000)
        }

    }


