package com.ita24.yumly
import kotlin.math.pow
import kotlin.math.roundToInt


object EloManager {

    fun updateElo(winner: MutableList<Any>, loser: MutableList<Any>, fast: Boolean) {
        val winnerElo = winner[6] as Int
        val loserElo = loser[6] as Int

        val K = if (fast) 1.5 else 0

        val expectedWinner = 1.0 / (1.0 + 10.0.pow((loserElo - winnerElo) / 400.0))



    }


}