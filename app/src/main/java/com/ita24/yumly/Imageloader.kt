package com.ita24.yumly

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.*

object Imageloader
{
    private val userDataLocal = UserDataLocal()

    var liste = mutableListOf<List<Any>>()

    suspend fun loadList(){
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val collection = "rezepte"
        val snap = db.collection(collection).get().await()

        liste.clear()

        try {
            var count = 0
        for (doc in snap.documents) {

            val id = count
            val name = doc.getString("name") ?: continue
            val bildurl = doc.getString("bildurl") ?: continue
            val zeit = (doc.getLong("zubereitungszeitMinuten") ?: 0L).toInt()
            val zutaten = doc.get("zutaten") as? ArrayList<String> ?: arrayListOf()
            val allergien = doc.get("allergien") as? ArrayList<String> ?: arrayListOf()
            val attribute = doc.get("attribute") as? ArrayList<String> ?: arrayListOf()

            val elorank = userDataLocal.getElo(id)

            liste.add(
                mutableListOf(
                    name,
                    bildurl,
                    zeit,
                    zutaten,
                    allergien,
                    attribute,
                    elorank,
                    id
                )
            )
            count++
        }
            addLokalToList(count)
            Log.e("testloadlist", "$liste")

        }catch (e: Exception){
            Log.e("testloader", "${e}")
        }
    }

    fun addLokalToList(count: Int){
        var count = count
        val locals = userdataprefrecipes.getAllRecipes()
        for (recipe in locals){
            val id = count
            val name = recipe.name
            val bildurl = recipe.imgurl
            val zeit = recipe.zeit
            val zutaten = recipe.ingredients
            val allergien = recipe.allergies
            val attribute = recipe.attributlist
            val elorank = recipe.elo

            liste.add(
                mutableListOf(
                    name,
                    bildurl,
                    zeit,
                    zutaten,
                    allergien,
                    attribute,
                    elorank,
                    id
                )
            )
            count++
        }

    }
    suspend fun preloadImgs(context: Context) {
        try {
            coroutineScope {
                liste.forEach { eintrag ->
                    val url = eintrag[1] as String
                    if (url.startsWith("http:") || url.startsWith("https:"))
                    {
                        launch(Dispatchers.IO) {
                            try {
                                Log.d("testloader", "Starte: $url")
                                Glide.with(context.applicationContext)
                                    .downloadOnly()
                                    .load(url)
                                    .submit()
                                    .get()
                                Log.d("testloader", "Fertig: $url")
                            } catch (e: Exception) {
                                Log.e("testloader", "Fehler bei $url", e)
                            }
                        }
                    }else{
                        Log.d("testloader", "localimg: $url")
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("Imageloader", "preloadImgs failed", e)
        }
    }

    val exclude = mutableListOf<Int>()

    fun resetExcludedList(){
        exclude.clear()
    }
    const val idIndex = 7
    suspend fun loadnewImg(imageView: ImageView): List<Any>? {

        val rezept = EloManager.pickNextRecipe(liste, exclude)

        exclude.add(rezept[idIndex] as Int)

        try {
            if (liste.isEmpty()) return null

            val url = rezept[1] as String

            withContext(Dispatchers.Main) {
                Glide.with(imageView)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(imageView)
            }

            Log.d("DEBUGtest", "liste.size = ${liste.size}")
            return rezept

        } catch (e: Exception) {
            Log.e("DEBUGtest", "loadnewImg crashed", e)
            return null
        }
    }
}
