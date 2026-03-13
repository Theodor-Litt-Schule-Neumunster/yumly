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

    var liste = mutableListOf<MutableList<Any?>>()

    suspend fun loadList(){
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val collection = "rezepte"
        
        try {
            val snap = db.collection(collection).get().await()
            liste.clear()
            var count = 0
            for (doc in snap.documents) {

                val id = doc.getString("id")?.toIntOrNull() ?: continue
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
                        id,
                        null 
                    )
                )
                count++
            }
            Log.e("testloadlist", "$liste")
            addLokalToList(count)

        }catch (e: Exception){
            Log.e("testloader", "loadList failed: ${e}")
        }
    }

    suspend fun loadallIds(): List<Int>{
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val collection = "rezepte"
        try {
            val snap = db.collection(collection).get().await()
            var listOfIds = mutableListOf<Int>()
            for (doc in snap.documents) {
                val id = doc.getString("id")?.toIntOrNull() ?: continue
                listOfIds.add(id)
            }
            return listOfIds
        }catch (e: Exception){
            Log.e("testloader", "loadallIds failed: ${e}")
            return emptyList()
        }
    }

    fun addLokalToList(count: Int){
        var currentCount = count
        val locals = userdataprefrecipes.getAllRecipes()
        for (recipe in locals){
            val id = currentCount
            val name = recipe.name
            val bildurl = recipe.imgurl
            val zeit = recipe.zeit
            val zutaten = recipe.ingredients
            val allergien = recipe.allergies
            val attribute = recipe.attributlist
            val elorank = recipe.elo
            val source = recipe.recipeSource

            liste.add(
                mutableListOf(
                    name,
                    bildurl,
                    zeit,
                    zutaten,
                    allergien,
                    attribute,
                    elorank,
                    id,
                    source
                )
            )
            currentCount++
        }

    }

    suspend fun loadallIngredients(): List<String> = withContext(Dispatchers.IO) {
        val db: FirebaseFirestore = FirebaseFirestore.getInstance()
        val collection = "rezepte"
        val listOfIngredients = mutableSetOf<String>()

        try {
            val snap = db.collection(collection).get().await()
            for (doc in snap.documents) {
                val ingredients = doc.get("zutaten") as? List<String> ?: continue
                for (ingredient in ingredients) {
                    listOfIngredients.add(ingredient)
                }
            }
            // Auch lokale Zutaten hinzufügen
            userdataprefrecipes.getAllRecipes().forEach { recipe ->
                recipe.ingredients.forEach { listOfIngredients.add(it) }
            }
            return@withContext listOfIngredients.sorted()
        } catch (e: Exception) {
            Log.e("testloader", "loadallIngredients failed: ${e}")
            return@withContext emptyList<String>()
        }
    }

    suspend fun preloadImgs(context: Context) {
        try {
            coroutineScope {
                liste.forEach { eintrag ->
                    val url = eintrag[1] as? String
                    if (url != null && (url.startsWith("http:") || url.startsWith("https:")))
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
    suspend fun loadnewImg(imageView: ImageView): MutableList<Any?>? {

        var rezept: List<Any?> = emptyList()
        var allreadyFiltered: MutableList<Int> = mutableListOf()

        while (true){
            rezept = EloManager.pickNextRecipe(liste, exclude, allreadyFiltered)
            
            if (rezept.isEmpty()) {
                Log.e("Imageloader", "No recipe found matching filters")
                return null
            }
            
            if (Filter.checkIfValid(rezept)) break
            else{
                val id = (rezept[idIndex] as? Number)?.toInt()
                if (id != null) allreadyFiltered.add(id) else break 
                continue
            }
        }

        if (rezept.size > idIndex && rezept[idIndex] is Int) {
            exclude.add(rezept[idIndex] as Int)
        }

        try {
            val url = rezept[1] as String

            withContext(Dispatchers.Main) {
                Glide.with(imageView)
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .dontAnimate()
                    .into(imageView)
            }
            return rezept.toMutableList()

        } catch (e: Exception) {
            Log.e("DEBUGtest", "loadnewImg crashed", e)
            return null
        }
    }
}
