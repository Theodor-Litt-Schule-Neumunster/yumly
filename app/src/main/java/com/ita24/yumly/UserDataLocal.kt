package com.ita24.yumly
import android.content.SharedPreferences
import android.content.Context

object userdatapref {

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.getSharedPreferences(
            "meine_prefs",
            Context.MODE_PRIVATE
        )
    }

    fun edit(recipe: String, elo: Int) {
        prefs.edit().putInt(recipe, elo).apply()
    }

    fun get(recipe: String, default: Int = 0): Int {
        return prefs.getInt(recipe, default)
    }
}

class UserDataLocal{

    fun getElo(recipe: String): Int{
        return userdatapref.get(recipe, 1000)
        }

    fun saveElo(recipe: String, elo: Int) {
        userdatapref.edit(recipe,elo)
    }

    }


