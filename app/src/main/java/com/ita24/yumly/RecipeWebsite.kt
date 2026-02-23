package com.ita24.yumly

import android.content.Intent
import android.net.Uri
import android.content.Context

object RecipeWebsite{

    fun getName(id: Int): String{
        val url = "https://theodor-litt-schule-neumunster.github.io/yumly/recipes/" + id + ".html"
        return url
    }

    fun sendToWebsite(context: Context, id: Int){
        val url = getName(id)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)

    }

}