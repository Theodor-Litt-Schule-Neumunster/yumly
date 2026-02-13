package com.ita24.yumly

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserDataOnline(private val username: String) {

    private val benutzer = username

    private val database = FirebaseDatabase.getInstance(
        "https://yumly-874a5-default-rtdb.europe-west1.firebasedatabase.app/"
    )
    private val ref = database.reference

    private val local = UserDataLocal()

    suspend fun getIds(): List<Int> {
        if (benutzer.isBlank()) return emptyList()

        val snapshot = ref
            .child("users")
            .child(benutzer)
            .child("gerichte")
            .get()
            .await()

        val ids = mutableListOf<Int>()
        for (child in snapshot.children) {
            val key = child.key ?: continue
            val id = key.toIntOrNull() ?: continue
            ids.add(id)
        }
        return ids
    }

    suspend fun getElo(recipeId: Int): Int? {
        if (benutzer.isBlank()) return null

        return ref
            .child("users")
            .child(benutzer)
            .child("gerichte")
            .child(recipeId.toString())
            .child("elo")
            .get()
            .await()
            .getValue(Int::class.java)
    }

    suspend fun setElo(recipeId: Int, elo: Int) {
        if (benutzer.isBlank()) return

        ref
            .child("users")
            .child(benutzer)
            .child("gerichte")
            .child(recipeId.toString())
            .child("elo")
            .setValue(elo)
            .await()
    }

    suspend fun overrideLocal(recipeId: Int) {
        if (benutzer.isBlank()) return

        val elo = getElo(recipeId) ?: return
        local.saveElo(recipeId, elo)
    }

    suspend fun overrideLocalAll(recipeIds: List<Int>) {
        for (id in recipeIds) {
            overrideLocal(id)
        }
    }

    suspend fun start() {
        val recipeIds = getIds()
        overrideLocalAll(recipeIds)
    }
}
