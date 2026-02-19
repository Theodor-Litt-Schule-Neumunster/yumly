package com.ita24.yumly

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson

data class localSavedRecipe(
    val name: String,
    val zeit: Int,
    val imgurl: String,
    val ingredients: List<String>,
    val allergies: List<String>,
    val attributlist: List<String>,
    var elo: Int = 1100
)

object userdataprefrecipes {

    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    private const val RECIPE_KEY_PREFIX = "recipe_data_"

    fun init(context: Context) {
        prefs = context.getSharedPreferences(
            "meine_rezepte",
            Context.MODE_PRIVATE
        )
    }

    fun saveRecipe(recipe: localSavedRecipe) {
        val json = gson.toJson(recipe)
        prefs.edit().putString("$RECIPE_KEY_PREFIX${recipe.name}", json).apply()
    }

    fun getAllRecipes(): MutableList<localSavedRecipe> {
        val recipes = mutableListOf<localSavedRecipe>()
        val allEntries = prefs.all

        for ((key, value) in allEntries) {
            if (key.startsWith(RECIPE_KEY_PREFIX)) {
                val json = value as? String ?: continue
                val recipe = gson.fromJson(json, localSavedRecipe::class.java)
                recipes.add(recipe)
            }
        }

        return recipes
    }
}
