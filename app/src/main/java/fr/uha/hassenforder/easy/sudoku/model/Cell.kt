package fr.uha.hassenforder.easy.sudoku.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

/*
 * a cell value at coordinate [ x, y ] in a puzzle or in a game
 * in a puzzle it is supposed to be the initial value
 * in a game could be :
 *  + a guess
 *  + an expectation
 *  + a negative expectation
 *  + a cheat
 *
 *  the key is provided to have a simple solution to sort all cells according to the position
 *
 */
@Entity(tableName = "cells")
@Immutable
data class Cell (
    @PrimaryKey(autoGenerate = true)
    val cid : Long = 0,
    val pid : Long,
    val x : Int,
    val y : Int,
    val value : Int,
    val state : CellState,
) {
    @Ignore
    val key = buildKey(x, y)

    companion object {
        fun buildKey (x :Int, y:Int) : Int {
            return 1000 * y + x
        }
    }

}
