package com.ita24.yumly

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.util.Log
import android.widget.ImageView

class Imageloader(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    var liste : List<List<Any>> = emptyList();
    suspend fun preloadImgs(context: Context) {
        val collection = "rezepte"
        val urlField = "bildurl"

        try {

            val snap = db.collection(collection).get().await()

            liste = snap.documents.mapNotNull { doc ->
                val name = doc.getString("name") ?: return@mapNotNull null
                val bildurl  = doc.getString("bildurl") ?: return@mapNotNull null
                val zeit = (doc.getLong("zubereitungszeitMinuten") ?: 0L).toInt()
                val zutaten = doc.get("zutaten") as? ArrayList<String> ?: arrayListOf()
                val allergien  = doc.get("allergien") as? ArrayList<String> ?: arrayListOf()
                val attribute  = doc.get("attribute") as? ArrayList<String> ?: arrayListOf()

                listOf(name, bildurl, zeit, zutaten, allergien, attribute)
            }
            val urls = snap.documents.mapNotNull { it.getString(urlField) }

            withContext(Dispatchers.Main) {
                urls.forEach { url ->
                    Log.e("TESTURLS", "$url")
                    Glide.with(context.applicationContext)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload()
                }
            }
        } catch (e: Exception) {
        }
    }

    suspend fun loadnewImg(imageView: ImageView) {
        try {
            val rezept = liste.random()
            val url = rezept[1] as String
            Glide.with(imageView)
                .load(url)
                .into(imageView)
            Log.d("DEBUGtest", "liste.size = ${liste.size}")

        } catch (e: Exception) {
            Log.e("DEBUGtest", "loadnewImg crashed", e)

        }
    }


}
