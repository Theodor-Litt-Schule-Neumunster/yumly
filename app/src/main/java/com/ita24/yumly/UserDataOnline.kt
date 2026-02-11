package com.ita24.yumly
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await
import android.util.Log

class UserDataOnline(private val username: String) {

    private val benutzer = username
    private val database = FirebaseDatabase.getInstance("https://yumly-874a5-default-rtdb.europe-west1.firebasedatabase.app/")
    private val ref = database.reference
    val local: UserDataLocal = UserDataLocal()


    suspend fun getnames(): List<String> {
        if (benutzer.isBlank()) return emptyList()

        val snapshot = ref
            .child("users")
            .child(benutzer)
            .child("gerichte")
            .get()
            .await()

        val names = mutableListOf<String>()
        for (child in snapshot.children) {
            child.key?.let { names.add(it) }
        }
        return names
    }

    suspend fun getelo(recipe: String): Int?{

            return ref
                .child("users")
                .child(benutzer)
                .child("gerichte")
                .child(recipe)
                .child("elo")
                .get()
                .await()
                .getValue(Int::class.java)
    }
    suspend fun setElo(recipe: String, elo: Int){
        ref
            .child("users")
            .child(benutzer)
            .child("gerichte")
            .child(recipe)
            .child("elo")
            .setValue(elo)
            .await()
    }

    suspend fun overrideLocal(recipe: String){
        if (benutzer == "") return

        val elo = this.getelo(recipe)
        if (elo != null) {
            local.saveElo(recipe, elo)
        }
    }

    suspend fun overrideLocalAll(recipes: List<String>) {
        for (r in recipes) {
            overrideLocal(r)
        }
    }
    suspend fun start(){
        Log.e("testusername", "$username")
        val recipes = getnames()
        overrideLocalAll(recipes)
    }

}

