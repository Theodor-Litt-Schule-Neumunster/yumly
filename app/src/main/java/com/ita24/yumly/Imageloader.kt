package com.ita24.yumly

import android.content.Context
import android.content.SharedPreferences
import android.content.Context.MODE_PRIVATE
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class Imageloader(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val userDataLocal = UserDataLocal()

    var liste = mutableListOf<List<Any>>()

    suspend fun loadList(){
        val collection = "rezepte"
        val snap = db.collection(collection).get().await()

        liste.clear()

        try {

        for (doc in snap.documents) {
            val name = doc.getString("name") ?: continue
            val bildurl = doc.getString("bildurl") ?: continue
            val zeit = (doc.getLong("zubereitungszeitMinuten") ?: 0L).toInt()
            val zutaten = doc.get("zutaten") as? ArrayList<String> ?: arrayListOf()
            val allergien = doc.get("allergien") as? ArrayList<String> ?: arrayListOf()
            val attribute = doc.get("attribute") as? ArrayList<String> ?: arrayListOf()

            val elorank = userDataLocal.getElo(name)

            liste.add(
                listOf(
                    name,
                    bildurl,
                    zeit,
                    zutaten,
                    allergien,
                    attribute,
                    elorank
                )
            )
        }

        }catch (e: Exception){
            Log.e("", "${e}")
        }
    }
    suspend fun preloadImgs(context: Context) {
        try {

            withContext(Dispatchers.IO) {
                liste.forEach { eintrag ->
                    val url = eintrag[1] as String
                    Log.e("testloader", "${url}")
                    Glide.with(context.applicationContext)
                        .load(url)
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .preload()
                }
            }

        } catch (e: Exception) {
            Log.e("Imageloader", "preloadImgs failed", e)
        }
    }

    suspend fun loadnewImg(imageView: ImageView) {
        try {
            if (liste.isEmpty()) return

            val rezept = liste.random()
            val url = rezept[1] as String

            withContext(Dispatchers.Main) {
                Glide.with(imageView)
                    .load(url)
                    .into(imageView)
            }

            Log.d("DEBUGtest", "liste.size = ${liste.size}")

        } catch (e: Exception) {
            Log.e("DEBUGtest", "loadnewImg crashed", e)
        }
    }
}
