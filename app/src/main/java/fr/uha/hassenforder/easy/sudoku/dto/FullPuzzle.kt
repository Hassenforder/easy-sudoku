package fr.uha.hassenforder.easy.sudoku.dto

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Relation
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.Puzzle

@Immutable
data class FullPuzzle (

    @Embedded
    val puzzle : Puzzle,

    @Relation(parentColumn = "pid", entityColumn = "pid")
    val cells: List<Cell>,

)