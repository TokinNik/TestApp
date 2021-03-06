package com.example.testapp.util

import javax.inject.Inject

class RollUtil@Inject constructor(){

    private val diceRange = 6

    fun roll3D6() = (1..diceRange).random() + (1..diceRange).random() + (1..diceRange).random()

    fun roll3D6(modification: Int) = roll3D6() + modification

    fun roll3D6(negative: Int, positive: Int) = roll3D6() - negative + positive

}

class Dice(val d: Int = 1, val value: Int = 0) {
    override fun toString(): String {
        return "${d}d${if (value > 0) "+" else ""}${if (value != 0) value else ""}"
    }
}