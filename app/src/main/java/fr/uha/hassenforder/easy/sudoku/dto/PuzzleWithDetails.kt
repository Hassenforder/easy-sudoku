package fr.uha.hassenforder.easy.sudoku.dto

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import fr.uha.hassenforder.easy.sudoku.model.Puzzle

@Immutable
data class PuzzleWithDetails (

    @Embedded
    val puzzle : Puzzle,

    val startedCount : Int,
    val lostCount : Int,
    val wonCount : Int,

) {
    fun gameInProgress () : Boolean {
        if (startedCount > 0) return true
        if (lostCount > 0) return true
        if (wonCount > 0) return true
        return false
    }
}