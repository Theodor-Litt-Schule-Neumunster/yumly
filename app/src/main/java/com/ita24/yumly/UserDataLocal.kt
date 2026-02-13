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

    fun edit(id: Int, elo: Int) {
        prefs.edit().putInt("recipe_$id", elo).apply()
    }

    fun get(id: Int, default: Int = 1000): Int {
        return prefs.getInt("recipe_$id", default)
    }
}


class UserDataLocal{

    fun getElo(recipe: Int): Int{
        return userdatapref.get(recipe, 1000)
        }

    fun saveElo(recipe: Int, elo: Int) {
        userdatapref.edit(recipe,elo)
    }

    }


