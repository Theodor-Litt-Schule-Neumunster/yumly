package com.ita24.yumly

import android.content.Context
import android.content.SharedPreferences

object Filter {

    private var prefs: SharedPreferences? = null

    // Filter-Listen
    var whitelistAttributes = mutableSetOf<String>()
    var blacklistAttributes = mutableSetOf<String>()
    var whitelistIngredients = mutableSetOf<String>()
    var blacklistIngredients = mutableSetOf<String>()
    var whitelistAllergies = mutableSetOf<String>()
    var blacklistAllergies = mutableSetOf<String>()

    // Modus für Zutaten-Whitelist: true = Kühlschrank-Modus (Subset), false = Voraussetzungs-Modus (ContainsAll)
    var ingredientFilterIsLimit = false


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
            ingredientFilterIsLimit = p.getBoolean("ingredIsLimit", false)
        }
    }

    fun normalizeAttribute(attr: String): String {
        return when (attr.lowercase().trim()) {
            "gekocht", "cooked_att" -> "cooked_att"
            "gebraten", "fried_att" -> "fried_att"
            "frittiert", "deep_fried_att" -> "deep_fried_att"
            "scharf", "spicy", "spicy_att" -> "spicy_att"
            "warme gerichte", "warm", "hot_att" -> "hot_att"
            "vegetarisch", "veggie_att" -> "veggie_att"
            "herzhaft", "hearty_att" -> "hearty_att"
            "glutenfrei", "gluten_free_att" -> "gluten_free_att"
            "vegan", "vegan_att" -> "vegan_att"
            "kalte gerichte", "cold_att" -> "cold_att"
            "laktosefrei", "lactose_free_att" -> "lactose_free_att"
            "gebacken", "baked_att" -> "baked_att"
            "fast food", "fast_food_att" -> "fast_food_att"
            "süß", "sweet_att" -> "sweet_att"
            "gegrillt", "grilled_att" -> "grilled_att"
            else -> attr.lowercase().trim()
        }
    }

    fun normalizeAllergen(all: String): String {
        return when (all.lowercase().trim()) {
            "gluten", "gluten_all" -> "gluten_all"
            "ei", "eggs_all" -> "eggs_all"
            "fisch", "fish_all" -> "fish_all"
            "erdnüsse", "nüsse", "schalenfrüchte", "nuts_all" -> "nuts_all"
            "soja", "soy_all" -> "soy_all"
            "laktose", "lactose_all" -> "lactose_all"
            "sellerie", "celery_all" -> "celery_all"
            "senf", "mustard_all" -> "mustard_all"
            "sesam", "sesame_all" -> "sesame_all"
            "schwefeldioxid", "sulfite", "schwefel", "sulphur_all", "sulphites_all" -> "sulphites_all"
            "weichtiere", "molluscs_all" -> "molluscs_all"
            "krebstiere", "crustacea_all" -> "crustacea_all"
            else -> all.lowercase().trim()
        }
    }

    fun checkIfValid(recipe: List<Any?>): Boolean {
        val recipeIngredients = recipe[3] as? List<String> ?: emptyList()
        val rawAllergies = recipe[4] as? List<String> ?: emptyList()
        val rawAttributes = recipe[5] as? List<String> ?: emptyList()

        val recipeAllergies = rawAllergies.map { normalizeAllergen(it) }
        val recipeAttributes = rawAttributes.map { normalizeAttribute(it) }

        // Blacklist Check
        if (recipeAttributes.any { it in blacklistAttributes }) return false
        if (recipeIngredients.any { it in blacklistIngredients }) return false
        if (recipeAllergies.any { it in blacklistAllergies }) return false

        // Whitelist Check Attributes & Allergies
        if (!recipeAttributes.containsAll(whitelistAttributes)) return false
        if (!recipeAllergies.containsAll(whitelistAllergies)) return false

        // Ingredient Whitelist
        if (whitelistIngredients.isNotEmpty()) {
            if (ingredientFilterIsLimit) {
                // Kühlschrank-Modus: Alle Rezeptzutaten müssen in der Whitelist sein
                if (!whitelistIngredients.containsAll(recipeIngredients)) return false
            } else {
                // Voraussetzungs-Modus: Alle Whitelist-Zutaten müssen im Rezept sein
                if (!recipeIngredients.containsAll(whitelistIngredients)) return false
            }
        }

        return true
    }

    fun save(key: String, set: Set<String>) {
        prefs?.edit()?.putStringSet(key, set)?.apply()
    }

    fun saveWhiteAttribute(item: String) {
        val norm = normalizeAttribute(item)
        if (whitelistAttributes.contains(norm)) whitelistAttributes.remove(norm) else whitelistAttributes.add(norm)
        save("whiteAttr", whitelistAttributes)
    }
    fun saveBlackAttribute(item: String) {
        val norm = normalizeAttribute(item)
        if (blacklistAttributes.contains(norm)) blacklistAttributes.remove(norm) else blacklistAttributes.add(norm)
        save("blackAttr", blacklistAttributes)
    }

    fun saveWhiteIngredients(items: Set<String>, isLimit: Boolean) {
        whitelistIngredients = items.toMutableSet()
        ingredientFilterIsLimit = isLimit
        save("whiteIngred", whitelistIngredients)
        prefs?.edit()?.putBoolean("ingredIsLimit", isLimit)?.apply()
    }

    fun saveBlackIngredients(items: Set<String>) {
        blacklistIngredients = items.toMutableSet()
        save("blackIngred", blacklistIngredients)
    }

    fun saveBlackIngredient(item: String) {
        if (blacklistIngredients.contains(item)) blacklistIngredients.remove(item) else blacklistIngredients.add(item)
        save("blackIngred", blacklistIngredients)
    }

    fun saveWhiteAllergy(item: String) {
        val norm = normalizeAllergen(item)
        if (whitelistAllergies.contains(norm)) whitelistAllergies.remove(norm) else whitelistAllergies.add(norm)
        save("whiteAllergy", whitelistAllergies)
    }
    fun saveBlackAllergy(item: String) {
        val norm = normalizeAllergen(item)
        if (blacklistAllergies.contains(norm)) blacklistAllergies.remove(norm) else blacklistAllergies.add(norm)
        save("blackAllergy", blacklistAllergies)
    }

    fun saveWhiteIngredient(item: String) {
        if (whitelistIngredients.contains(item)) whitelistIngredients.remove(item) else whitelistIngredients.add(item)
        save("whiteIngred", whitelistIngredients)
    }
}
