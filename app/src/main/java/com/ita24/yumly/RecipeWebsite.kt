package com.ita24.yumly

import android.content.Context
import android.content.Intent

object RecipeWebsite {

    fun getName(id: Int): String {
        return "https://theodor-litt-schule-neumunster.github.io/yumly/recipes/" + id + ".html"
    }

    fun sendToWebsite(context: Context, id: Int) {
        val url = getName(id)
        val intent = Intent(context, RecipeWebViewActivity::class.java).apply {
            putExtra("RECIPE_URL", url)
        }
        context.startActivity(intent)
    }

}