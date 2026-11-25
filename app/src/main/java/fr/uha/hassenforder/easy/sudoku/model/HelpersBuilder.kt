package fr.uha.hassenforder.easy.sudoku.model

import fr.uha.hassenforder.easy.sudoku.ui.field.GridAction

class HelpersBuilder (
    val cells : List<Cell>
) {

    private fun indexes () : List<Int> {
        return (0..8).toList()
    }

    private fun allNumbers () : List<Int> {
        return (1..9).toList()
    }

    private fun getInLine (cells : List<Cell>, x : Int) : List<Int> {
        return cells.filter { it.x == x }.map { it.value }
    }

    private fun getInColumn (cells : List<Cell>, y : Int) : List<Int> {
        return cells.filter { it.y == y }.map { it.value }
    }

    private fun getInSquare (cells : List<Cell>, x : Int, y : Int) : List<Int> {
        val xSquare = x / 3
        val ySquare = y / 3
        return cells.filter { it.x / 3 == xSquare && it.y / 3 == ySquare}.map { it.value }
    }

    private fun getNeighbours (cells : List<Cell>, x : Int, y : Int) : Set<Int> {
        val neighbours = getInLine(cells, x) + getInColumn(cells, y) + getInSquare(cells, x, y)
        return neighbours.toSet()
    }

    private fun rowIndex (x : Int) : Int {
        return 100 + x
    }
    private fun lineIndex (y : Int) : Int {
        return 200 + y
    }
    private fun squareIndex (x : Int, y : Int) : Int {
        return 300 + (x / 3) + (y / 3) * 3
    }

    /*
     * Giant hack with replaceCell
     * Basically we should check the list but
     * the flow is ui_capture -> replace_event -> replace_database -> ui_compose
     * In this flow if we check in ui_compose it is late and :
     *    + in ui_compose the list is immutable, we should create a filtered one and add the item
     *    + if we update the state of the game we trigger another database update
     * If we intercept the even before database update :
     *    + the list is still immutable, and we must pay at least 2 list copies
     *
     *    + here we inject the new value which replace the same in the list
     *    + it is done in the cache building so just an additional test should be light way
     */
    private fun buildCache (replaceCell : Cell? = null) : Map<Int, List<Int>> {
        val gridHelpers = mutableMapOf<Int, MutableList<Int>>()
        cells.forEach { cell ->
            val cellToUse = if (cell.key == replaceCell?.key) replaceCell else cell
            if (cellToUse.value == 0) return@forEach
            // All 9 rows
            val rowIndex = rowIndex(cellToUse.x)
            if (!gridHelpers.contains(rowIndex))
                gridHelpers[rowIndex] = mutableListOf()
            gridHelpers[rowIndex]!!.add(cellToUse.value)
            // All 9 lines
            val lineIndex = lineIndex(cellToUse.y)
            if (!gridHelpers.contains(lineIndex))
                gridHelpers[lineIndex] = mutableListOf()
            gridHelpers[lineIndex]!!.add(cellToUse.value)
            // All 9 squares
            val squareIndex = squareIndex(cellToUse.x, cellToUse.y)
            if (!gridHelpers.contains(squareIndex))
                gridHelpers[squareIndex] = mutableListOf()
            gridHelpers[squareIndex]!!.add(cellToUse.value)
        }
        return gridHelpers
    }

    private fun getNeighbours (cache :  Map<Int, List<Int>>, x : Int, y : Int) : Set<Int> {
        val rowList = cache[rowIndex(x)]?: emptyList()
        val lineList = cache[lineIndex(y)]?: emptyList()
        val squareList = cache[squareIndex(x, y)]?: emptyList()
        val neighbours = rowList + lineList + squareList
        return neighbours.toSet()
    }

    fun buildHelpers () : Map<Int, List<Int>> {
        val gridHelpers = buildCache()
        return cells
            .filter { it.value == 0 }
            .associate { cell ->
                cell.key to allNumbers().filterNot { getNeighbours(gridHelpers, cell.x, cell.y).contains(it) }
            }
    }

    fun checker (replaceCell : Cell? = null) : GameState {
        val gridHelpers = buildCache(replaceCell)
        var wonCount = 0
        var lost = false
        indexes().forEach { index ->
            val rowList = gridHelpers[rowIndex(index)]?: emptyList()
            val listCount = rowList.size
            val setCount = rowList.toSet().size
            if (setCount != listCount) {
                lost = true
            } else if (listCount == 9) {
                wonCount++
            }
        }
        indexes().forEach { index ->
            val lineList = gridHelpers[lineIndex(index)]?: emptyList()
            val listCount = lineList.size
            val setCount = lineList.toSet().size
            if (setCount != listCount) {
                lost = true
            } else if (listCount == 9) {
                wonCount++
            }
        }
        return when {
            lost -> GameState.LOST
            wonCount == 18 -> GameState.WON
            else -> GameState.STARTED
        }
    }

}