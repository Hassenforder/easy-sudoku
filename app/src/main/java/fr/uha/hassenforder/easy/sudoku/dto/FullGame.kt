package fr.uha.hassenforder.easy.sudoku.dto

import androidx.compose.runtime.Immutable
import androidx.room.Embedded
import androidx.room.Ignore
import androidx.room.Relation
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.Game
import fr.uha.hassenforder.easy.sudoku.model.Puzzle

@Immutable
data class FullGame (

    @Embedded
    val game: Game,

    @Relation(parentColumn = "gid", entityColumn = "pid")
    val gameCells: List<Cell>,

    @Relation(parentColumn = "pid", entityColumn = "pid")
    val puzzle : Puzzle,

    @Relation(parentColumn = "pid", entityColumn = "pid")
    val puzzleCells: List<Cell>,

) {
    val cells : List<Cell> by lazy {
        val sortedGameCells = gameCells.associateBy { it.key }
        val mergedCells = puzzleCells.map { cell ->
            if (cell.value == 0) sortedGameCells[cell.key] ?: cell else cell
        }
        mergedCells
    }

}
