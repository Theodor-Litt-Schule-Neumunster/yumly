package com.ita24.yumly

import android.util.Log
import com.google.firebase.firestore.auth.User
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val eloindex = 6
private const val idindex = 7

object EloManager {

    val userDataLocal = UserDataLocal()

    var starttime: Long = 0

    fun starttimer(){
        starttime = System.currentTimeMillis()
    }
    fun wasFastEnough(): Boolean{
        val time = System.currentTimeMillis() - starttime
        return time < 2500
    }


    suspend fun updateElo(winner: MutableList<Any>, loser: MutableList<Any>, fast: Boolean) {
        val winnerElo = winner[eloindex] as Int
        val loserElo  = loser[eloindex] as Int

        val k = if (fast) 30.0 else 20.0

        val expectedWinner =
            1.0 / (1.0 + 10.0.pow((loserElo - winnerElo) / 400.0))
        val expectedLoser = 1.0 - expectedWinner

        val accWinner = winnerElo + k * (1.0 - expectedWinner)
        val accLoser  = loserElo  + k * (0.0 - expectedLoser)

        winner[eloindex] = accWinner.roundToInt()
        loser[eloindex]  = accLoser.roundToInt()

        val winnerIndex = winner[idindex] as Int
        val loserIndex  = loser[idindex] as Int

        withContext(Dispatchers.IO) {

            userDataLocal.saveElo(winnerIndex, accWinner.roundToInt())
            UserDataOnline.setElo(winnerIndex, accWinner.roundToInt())
            Log.e("testelorating", "$winnerIndex $accWinner")
            userDataLocal.saveElo(loserIndex, accLoser.roundToInt())
            UserDataOnline.setElo(loserIndex, accLoser.roundToInt())
            Log.e("testelorating", "$loserIndex $accLoser")

        }



    }

    fun pickNextRecipe(recipelist: List<List<Any>>, exclude: List<Int>): List<Any> {

        Log.e("testelorating", "$exclude")

        val Auswahl = recipelist.filter { r ->
            val id = r[idindex] as Int
            id !in exclude
        }
        if (Auswahl.isEmpty()) return emptyList()


        val minWeight = 5
        val scale = 0.5
        val minElo = Auswahl.minOf { (it[eloindex] as Int) }
        val weights = Auswahl.map { r ->
            val elo = r[eloindex] as Int
            minWeight + (elo - minElo + 1) * scale
        }

        val total = weights.sum()
        var roll = Random.nextDouble() * total

        for (i in Auswahl.indices) {
            roll -= weights[i]
            if (roll <= 0) {
                return Auswahl[i]
            }
        }
        //Zur Sicherheit falls nichts rausgekommen ist
        return Auswahl.last()
    }
}
