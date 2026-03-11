package com.ita24.yumly

import android.content.Context
import android.content.SharedPreferences

object Filter {

    private var prefs: SharedPreferences? = null

    // Die 6 Filter-Listen
    var whitelistAttributes = mutableSetOf<String>()
    var blacklistAttributes = mutableSetOf<String>()

    var whitelistIngredients = mutableSetOf<String>()
    var blacklistIngredients = mutableSetOf<String>()

    var whitelistAllergies = mutableSetOf<String>()
    var blacklistAllergies = mutableSetOf<String>()


    fun initFilters(context: Context) {
        prefs = context.applicationContext.getSharedPreferences("YumlyFilters", Context.MODE_PRIVATE)
        loadFilters()
    }

    private fun loadFilters() {
        prefs?.let { p ->
            whitelistAttributes = p.getStringSet("whiteAttr", emptySet())?.toMutableSet() ?: mutableSetOf()
            blacklistAttributes = p.getStringSet("blackAttr", emptySet())?.toMutableSet() ?: mutableSetOf()
            whitelistIngredients = p.getStringSet("whiteIngred", emptySet())?.toMutableSet() ?: mutableSetOf()
            blacklistIngredients = p.getStringSet("blackIngred", emptySet())?.toMutableSet() ?: mutableSetOf()
            whitelistAllergies = p.getStringSet("whiteAllergy", emptySet())?.toMutableSet() ?: mutableSetOf()
            blacklistAllergies = p.getStringSet("blackAllergy", emptySet())?.toMutableSet() ?: mutableSetOf()
        }
    }


    fun checkIfValid(recipe: List<Any?>): Boolean {
        val recipeIngredients = recipe[3] as? List<String> ?: emptyList()
        val recipeAllergies = recipe[4] as? List<String> ?: emptyList()
        val recipeAttributes = recipe[5] as? List<String> ?: emptyList()

        if (recipeAttributes.any { it in blacklistAttributes }) return false
        if (recipeIngredients.any { it in blacklistIngredients }) return false
        if (recipeAllergies.any { it in blacklistAllergies }) return false

        if (!recipeAttributes.containsAll(whitelistAttributes)) return false
        if (!recipeAllergies.containsAll(whitelistAllergies)) return false

        if (whitelistIngredients.isNotEmpty() && !recipeIngredients.any { it in whitelistIngredients }) return false

        return true
    }

    private fun save(key: String, set: Set<String>) {
        prefs?.edit()?.putStringSet(key, set)?.apply()
    }

    fun saveWhiteAttribute(item: String) {
        if (whitelistAttributes.contains(item)) whitelistAttributes.remove(item) else whitelistAttributes.add(item)
        save("whiteAttr", whitelistAttributes)
    }
    fun saveBlackAttribute(item: String) {
        if (blacklistAttributes.contains(item)) blacklistAttributes.remove(item) else blacklistAttributes.add(item)
        save("blackAttr", blacklistAttributes)
    }

    fun saveWhiteIngredient(item: String) {
        if (whitelistIngredients.contains(item)) whitelistIngredients.remove(item) else whitelistIngredients.add(item)
        save("whiteIngred", whitelistIngredients)
    }
    fun saveBlackIngredient(item: String) {
        if (blacklistIngredients.contains(item)) blacklistIngredients.remove(item) else blacklistIngredients.add(item)
        save("blackIngred", blacklistIngredients)
    }

    fun saveWhiteAllergy(item: String) {
        if (whitelistAllergies.contains(item)) whitelistAllergies.remove(item) else whitelistAllergies.add(item)
        save("whiteAllergy", whitelistAllergies)
    }
    fun saveBlackAllergy(item: String) {
        if (blacklistAllergies.contains(item)) blacklistAllergies.remove(item) else blacklistAllergies.add(item)
        save("blackAllergy", blacklistAllergies)
    }
}
