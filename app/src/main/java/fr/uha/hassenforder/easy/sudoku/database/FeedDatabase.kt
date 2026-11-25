package fr.uha.hassenforder.easy.sudoku.database

import fr.uha.hassenforder.easy.android.database.model.ManagementState
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.CellState
import fr.uha.hassenforder.easy.sudoku.model.Puzzle
import fr.uha.hassenforder.easy.sudoku.model.PuzzleLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedDatabase (
    private val db : SudokuDatabase
) {

    private val empty = arrayOf (
        intArrayOf(0, 0, 0,  0, 0, 0,  0, 0, 0),
        intArrayOf(0, 0, 0,  0, 0, 0,  0, 0, 0),
        intArrayOf(0, 0, 0,  0, 0, 0,  0, 0, 0),

        intArrayOf(0, 0, 0,  0, 0, 0,  0, 0, 0),
        intArrayOf(0, 0, 0,  0, 0, 0,  0, 0, 0),
        intArrayOf(0, 0, 0,  0, 0, 0,  0, 0, 0),

        intArrayOf(0, 0, 0,  0, 0, 0,  0, 0, 0),
        intArrayOf(0, 0, 0,  0, 0, 0,  0, 0, 0),
        intArrayOf(0, 0, 0,  0, 0, 0,  0, 0, 0),
    )

    private val easy1 = arrayOf (
        intArrayOf(4, 0, 0,  0, 7, 1,  0, 0, 0),
        intArrayOf(8, 0, 0,  9, 0, 4,  0, 0, 0),
        intArrayOf(6, 9, 1,  5, 0, 2,  0, 0, 0),

        intArrayOf(1, 2, 0,  7, 0, 3,  0, 0, 9),
        intArrayOf(9, 0, 7,  0, 2, 5,  0, 1, 4),
        intArrayOf(0, 0, 4,  1, 0, 6,  7, 5, 2),

        intArrayOf(2, 1, 0,  4, 0, 7,  9, 0, 5),
        intArrayOf(0, 3, 0,  0, 0, 0,  0, 0, 7),
        intArrayOf(7, 0, 0,  0, 5, 9,  1, 0, 8),
    )

    private val easy2 = arrayOf (
        intArrayOf(0, 0, 0,  0, 0, 5,  0, 0, 9),
        intArrayOf(4, 0, 7,  0, 0, 6,  0, 5, 0),
        intArrayOf(0, 5, 0,  8, 0, 0,  6, 0, 0),

        intArrayOf(5, 1, 0,  0, 0, 3,  0, 6, 0),
        intArrayOf(0, 0, 9,  0, 0, 4,  0, 3, 1),
        intArrayOf(8, 0, 2,  9, 0, 0,  0, 7, 0),

        intArrayOf(0, 0, 0,  0, 3, 0,  8, 0, 0),
        intArrayOf(0, 6, 0,  1, 4, 0,  3, 0, 0),
        intArrayOf(7, 0, 3,  2, 0, 0,  4, 0, 6),
    )

    private val medium1 = arrayOf (
        intArrayOf(6, 9, 0,  0, 0, 5,  0, 3, 2),
        intArrayOf(3, 0, 0,  0, 0, 1,  0, 9, 5),
        intArrayOf(1, 0, 5,  0, 9, 0,  4, 8, 0),

        intArrayOf(0, 8, 0,  0, 6, 7,  0, 0, 0),
        intArrayOf(0, 0, 1,  8, 0, 0,  0, 0, 9),
        intArrayOf(2, 6, 0,  0, 0, 0,  0, 5, 0),

        intArrayOf(0, 0, 0,  0, 0, 0,  8, 0, 0),
        intArrayOf(0, 0, 0,  0, 0, 6,  0, 2, 0),
        intArrayOf(0, 2, 0,  0, 0, 0,  9, 6, 4),
    )

    private val medium2 = arrayOf (
        intArrayOf(6, 0, 0,  0, 8, 0,  0, 0, 0),
        intArrayOf(0, 0, 0,  0, 0, 0,  0, 6, 0),
        intArrayOf(8, 9, 4,  0, 0, 0,  2, 0, 0),

        intArrayOf(0, 3, 0,  8, 9, 0,  0, 0, 0),
        intArrayOf(0, 1, 0,  0, 0, 7,  0, 0, 4),
        intArrayOf(9, 0, 0,  4, 6, 0,  0, 0, 0),

        intArrayOf(0, 6, 0,  7, 0, 0,  0, 2, 0),
        intArrayOf(2, 4, 9,  0, 3, 0,  5, 0, 7),
        intArrayOf(0, 7, 0,  2, 0, 0,  0, 1, 0),
    )

    private val hard1 = arrayOf (
        intArrayOf(5, 2, 0,  0, 1, 0,  0, 4, 0),
        intArrayOf(6, 1, 0,  0, 0, 0,  0, 0, 8),
        intArrayOf(0, 0, 0,  0, 5, 7,  0, 1, 0),

        intArrayOf(0, 0, 0,  0, 0, 5,  0, 9, 4),
        intArrayOf(0, 7, 0,  9, 0, 0,  0, 0, 0),
        intArrayOf(0, 0, 4,  2, 0, 0,  3, 0, 0),

        intArrayOf(0, 0, 3,  8, 0, 0,  9, 0, 0),
        intArrayOf(0, 0, 0,  7, 6, 1,  0, 0, 0),
        intArrayOf(8, 0, 0,  0, 0, 0,  0, 0, 0),
    )

    private val hard2 = arrayOf (
        intArrayOf(7, 0, 1,  0, 0, 2,  0, 0, 0),
        intArrayOf(0, 0, 0,  5, 8, 0,  0, 2, 9),
        intArrayOf(0, 5, 0,  0, 3, 0,  0, 0, 4),

        intArrayOf(0, 7, 3,  0, 0, 0,  9, 0, 0),
        intArrayOf(1, 0, 0,  0, 0, 8,  0, 0, 0),
        intArrayOf(0, 9, 0,  0, 0, 0,  6, 5, 0),

        intArrayOf(4, 6, 0,  1, 0, 0,  0, 0, 8),
        intArrayOf(0, 0, 0,  0, 0, 0,  7, 0, 0),
        intArrayOf(0, 0, 7,  0, 0, 0,  4, 0, 0),
    )

    private fun convert (array : Array<IntArray>, pid : Long) : List<Cell> {
        val cells = array.indices.flatMap { i ->
            array[i].indices.map { j ->
                Cell(0, pid, j, i, array[i][j], CellState.INITIAL)
            }
        }
        return cells
    }

    private suspend fun puzzle (level : PuzzleLevel, grid : Array<IntArray>, prefix : String = "initial", postfix : Int = 0) {
        val puzzle = Puzzle(
            pid=0,
            description = "$prefix-${level.toString().lowercase()}-${postfix.toString()}",
            size=9,
            level=level,
            managementState = ManagementState.VALID,
        )
        val pid = db.getPuzzleDao().create(puzzle)
        db.getPuzzleDao().createCells(convert(grid, pid))
    }

    suspend fun populate(mode: Int) = withContext(Dispatchers.IO) {
        puzzle(PuzzleLevel.EASY, easy1, prefix="alsace", 1)
        puzzle(PuzzleLevel.EASY, easy2, prefix="alsace", 2)
        puzzle(PuzzleLevel.MEDIUM, medium1, prefix="alsace", 1)
        puzzle(PuzzleLevel.MEDIUM, medium2, prefix="alsace", 2)
        puzzle(PuzzleLevel.HARD, hard1, prefix="alsace", 1)
        puzzle(PuzzleLevel.HARD, hard2, prefix="alsace", 2)
    }

    suspend fun clear() = withContext(Dispatchers.IO) {
        db.clearAllTables()
    }

}