package fr.uha.hassenforder.easy.sudoku.processing.image

import java.util.TreeMap
import kotlin.math.abs

class LineInjecter(
    private val width: Double,
    private val height: Double,
    private val expectedLineCount: Int
) {
    /*
        * compute projected distance on X between two adjacent lines
        * and register distance and lines
        * hack ... we register the first line as negative distance it should be the offset
        * constraint : lines are straight and sorted by ascending X positions
        */
    private fun distanceVertically(initial: MutableList<Line>): MutableMap<Double?, MutableList<Line?>?> {
        val result: MutableMap<Double?, MutableList<Line?>?> =
            TreeMap<Double?, MutableList<Line?>?>()
        var previousX: Double? = null
        for (line in initial) {
            if (previousX != null) {
                val distance: Double = Line.Companion.normalize(abs(line.x - previousX))
                if (!result.containsKey(distance)) {
                    result.put(distance, ArrayList<Line?>())
                }
                result.get(distance)!!.add(line)
            } else {
                val offset = -line.x
                result.put(offset, ArrayList<Line?>())
                result.get(offset)!!.add(line)
            }
            previousX = line.x
        }
        return result
    }

    /*
     * compute projected distance on Y between two adjacent lines
     * and register distance and lines
     * hack ... we register the first line as negative distance it should be the offset
     * constraint : lines are straight and sorted by ascending Y positions
     */
    private fun distanceHorizontally(initial: MutableList<Line>): MutableMap<Double?, MutableList<Line?>?> {
        val result: MutableMap<Double?, MutableList<Line?>?> =
            TreeMap<Double?, MutableList<Line?>?>()
        var previousY: Double? = null
        for (line in initial) {
            if (previousY != null) {
                val distance: Double = Line.Companion.normalize(abs(line.y - previousY))
                if (!result.containsKey(distance)) {
                    result.put(distance, ArrayList<Line?>())
                }
                result.get(distance)!!.add(line)
            } else {
                val offset = -line.y
                result.put(offset, ArrayList<Line?>())
                result.get(offset)!!.add(line)
            }
            previousY = line.y
        }
        return result
    }

    private fun select(lines: Lines, orientation: Line.Orientation?): MutableList<Line> {
        val selected: MutableList<Line> = ArrayList<Line>()
        for (entry in lines.registry.entries) {
            if (entry.value[0].orientation == orientation) {
                selected.addAll(entry.value)
            }
        }
        return selected
    }

    private fun getBestDistance(distances: MutableMap<Double?, MutableList<Line?>?>): Double {
        var bestDistance = Double.Companion.MAX_VALUE
        var count = 0
        for (entry in distances.entries) {
            if (entry.key!! < 0) continue
            if (count < entry.value!!.size) {
                count = entry.value!!.size
                bestDistance = entry.key!!
            }
        }
        return bestDistance
    }

    private fun getOffset(distances: MutableMap<Double?, MutableList<Line?>?>): Double {
        for (entry in distances.entries) {
            if (entry.key!! < 0) return -entry.key!!
        }
        return 0.0
    }

    private fun addFinalHorizontal(
        selected: MutableList<Line>,
        offset: Double,
        bestDistance: Double
    ): MutableList<Line> {
        val room = offset + (expectedLineCount - 1) * bestDistance
        // no place for an additional final line
        if (width - room < 0) return selected
        // there is room
        selected.add(Line(0.0, width, room, room))
        return selected
    }

    private fun addInitialHorizontal(
        selected: MutableList<Line>,
        offset: Double,
        bestDistance: Double
    ): MutableList<Line> {
        val room = offset - bestDistance
        // no place for an additional initial line
        if (room < 0) return selected
        // there is room
        selected.add(Line(0.0, width, room, room))
        return selected
    }

    private fun fillHolesHorizontal(
        selected: MutableList<Line>,
        offset: Double,
        bestDistance: Double
    ): MutableList<Line> {
        return selected
    }

    private fun processHorizontal(initial: Lines): Lines {
        var selected = select(initial, Line.Orientation.HORIZONTAL)
        if (selected.size == expectedLineCount) {
            return Lines().register(selected)
        }
        val distances = distanceHorizontally(selected)
        val bestDistance = getBestDistance(distances)
        val offset = getOffset(distances)
        if (distances.size != 2) {
            selected = fillHolesHorizontal(selected, offset, bestDistance)
        }
        if (selected.size == expectedLineCount) {
            return Lines().register(selected)
        }
        selected = addInitialHorizontal(selected, offset, bestDistance)
        if (selected.size == expectedLineCount) {
            return Lines().register(selected)
        }
        selected = addFinalHorizontal(selected, offset, bestDistance)
        if (selected.size == expectedLineCount) {
            return Lines().register(selected)
        }
        // returns but wrong count of lines
        return Lines().register(selected)
    }

    private fun addFinalVertical(
        selected: MutableList<Line>,
        offset: Double,
        bestDistance: Double
    ): MutableList<Line> {
        val room = offset + (expectedLineCount - 1) * bestDistance
        // no place for an additional final line
        if (width - room < 0) return selected
        // there is room
        selected.add(Line(room, room, 0.0, height))
        return selected
    }

    private fun addInitialVertical(
        selected: MutableList<Line>,
        offset: Double,
        bestDistance: Double
    ): MutableList<Line> {
        val room = offset - bestDistance
        // no place for an additional initial line
        if (room < 0) return selected
        // there is room
        selected.add(Line(room, room, 0.0, height))
        return selected
    }

    private fun fillHolesVertical(
        selected: MutableList<Line>,
        offset: Double,
        bestDistance: Double
    ): MutableList<Line> {
        return selected
    }

    private fun processVertical(initial: Lines): Lines {
        var selected = select(initial, Line.Orientation.VERTICAL)
        if (selected.size == expectedLineCount) {
            return Lines().register(selected)
        }
        val distances = distanceVertically(selected)
        val bestDistance = getBestDistance(distances)
        val offset = getOffset(distances)
        if (distances.size != 2) {
            selected = fillHolesVertical(selected, offset, bestDistance)
        }
        if (selected.size == expectedLineCount) {
            return Lines().register(selected)
        }
        selected = addInitialVertical(selected, offset, bestDistance)
        if (selected.size == expectedLineCount) {
            return Lines().register(selected)
        }
        selected = addFinalVertical(selected, offset, bestDistance)
        if (selected.size == expectedLineCount) {
            return Lines().register(selected)
        }
        // returns but wrong count of lines
        return Lines().register(selected)
    }

    /*
     * grid should have
     * + a unique distance between lines
     * + 10 lines per axis
     * if not we are in trouble so we will inject additional lines
     *
     * + the more frequent distance could be the good one
     * + try to fill the holes suggested by a distance much more bigger (should be 2x 3x ...)
     * + first or last line could not be detected but the distance to the edge is more than the common distance
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