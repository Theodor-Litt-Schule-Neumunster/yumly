package com.ita24.yumly

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import android.util.Log

class Imageloader(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun preloadImgs(context: Context) {
        val collection = "rezepte"
        val urlField = "bildurl"

        try {

            val snap = db.collection(collection).get().await()

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


}
