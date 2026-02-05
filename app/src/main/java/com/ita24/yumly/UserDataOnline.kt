package com.ita24.yumly
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.tasks.await

class UserDataOnline {
    private val database = FirebaseDatabase.getInstance()
    private val ref = database.reference


    suspend fun getelo(username: String, recipe: String): Int?{
            val userKey = username
            val recipeKey = recipe

            return ref
                .child("users")
                .child(userKey)
                .child("gerichte")
                .child(recipeKey)
                .child("elo")
                .get()
                .await()
                .getValue(Int::class.java)
    }
    fun getuserKey(): String {
        return "testname"
    }
}

