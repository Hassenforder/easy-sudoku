package fr.uha.hassenforder.easy.sudoku.processing.image

import kotlin.math.abs

class LineRemover(private val distanceThreshold: Double) {
    /*
        * compute projected distance on X between two adjacent lines
        * and keep only them that are far enough
        * constraint : lines are straight and sorted by ascending X positions
        */
    private fun distanceVertically(initial: MutableList<Line>): MutableList<Line> {
        val result: MutableList<Line> = mutableListOf()
        var previousX: Double? = null
        for (line in initial) {
            if (previousX != null) {
                val distance = abs(line.x - previousX)
                if (distance < distanceThreshold) continue
            }
            result.add(line)
            previousX = line.x
        }
        return result
    }

    /*
     * compute projected distance on Y between two adjacent lines
     * and keep only them that are far enough
     * constraint : lines are straight and sorted by ascending Y positions
     */
    private fun distanceHorizontally(initial: MutableList<Line>): MutableList<Line> {
        val result: MutableList<Line> = mutableListOf()
        var previousY: Double? = null
        for (line in initial) {
            if (previousY != null) {
                val distance = abs(line.y - previousY)
                if (distance < distanceThreshold) continue
            }
            result.add(line)
            previousY = line.y
        }
        return result
    }

    private fun select(lines: Lines, orientation: Line.Orientation?): MutableList<Line> {
        val selected: MutableList<Line> = mutableListOf()
        for (entry in lines.registry.entries) {
            if (entry.value[0].orientation == orientation) {
                selected.addAll(entry.value)
            }
        }
        return selected
    }

    private fun processVertical(initial: Lines): Lines {
        val horizontal = select(initial, Line.Orientation.VERTICAL)
        val selected = distanceVertically(horizontal)
        return Lines().register(selected)
    }

    private fun processHorizontal(initial: Lines): Lines {
        val horizontal = select(initial, Line.Orientation.HORIZONTAL)
        val selected = distanceHorizontally(horizontal)
        return Lines().register(selected)
    }

    /*
     * remove adjacent lines those are near but without the same key
     * it is not a merger (merger work in a key) this one scans across the keys to find near lines and merge them
     *
     * useful if you have thick lines detected two times but just over the deviation
     *  + one line is in the key before, the other in the key after
     *
     * constraint : only one line per key (in the List)
     *
     */
    fun process(initial: Lines): Lines {
        val horizontal = processHorizontal(initial)
        val vertical = processVertical(initial)
        return Lines.Companion.merge(horizontal, vertical)
    }
}