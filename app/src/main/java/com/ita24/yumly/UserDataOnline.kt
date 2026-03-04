package com.ita24.yumly

import android.util.Log
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await


object UserDataOnline{

    public var benutzer = "";


    private val database = FirebaseDatabase.getInstance(
        "https://yumly-874a5-default-rtdb.europe-west1.firebasedatabase.app/"
    )
    private val ref = database.reference

    private val local = UserDataLocal()

    suspend fun getIds(): List<Int> {
        if (benutzer.isBlank()) return emptyList()

        try {
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
        } catch (e: Exception) {
            Log.e("UserDataOnline", "Error getting ids: ${e.message}")
            return emptyList()
        }
    }

    suspend fun getElo(recipeId: Int): Int? {
        if (benutzer.isBlank()) return null

        try {
            return ref
                .child("users")
                .child(benutzer)
                .child("gerichte")
                .child(recipeId.toString())
                .child("elo")
                .get()
                .await()
                .getValue(Int::class.java)
        } catch (e: Exception) {
            Log.e("UserDataOnline", "Error getting elo: ${e.message}")
            return null
        }
    }

    suspend fun setElo(recipeId: Int, elo: Int) {
        if (benutzer.isBlank()) return

        try {
            ref
                .child("users")
                .child(benutzer)
                .child("gerichte")
                .child(recipeId.toString())
                .child("elo")
                .setValue(elo)
                .await()
        } catch (e: Exception) {
            Log.e("UserDataOnline", "Error setting elo: ${e.message}")
        }
    }

    suspend fun setAllElo(ids: List<Int>){
        if (benutzer.isBlank()) return

        for (id in ids){
            try {
                val elo = local.getElo(id)
                ref
                    .child("users")
                    .child(benutzer)
                    .child("gerichte")
                    .child(id.toString())
                    .child("elo")
                    .setValue(elo)
                    .await()
            } catch (e: Exception) {
                Log.e("UserDataOnline", "Error setting all elo for id $id: ${e.message}")
            }
        }
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