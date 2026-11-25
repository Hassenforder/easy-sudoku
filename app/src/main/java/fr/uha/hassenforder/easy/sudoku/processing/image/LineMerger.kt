package fr.uha.hassenforder.easy.sudoku.processing.image

import kotlin.math.max
import kotlin.math.min

class LineMerger {
    /*
        * merge all lines expected that they are all vertical
        * here vertical means either straight but also slightly slanted
        *
        * the created line will have
        *  + a mean startX and a mean endX over all lines
        *  + the lowest startY and highest endY of all lines
        */
    private fun mergeVertically(lines: MutableList<Line>): Line {
        val reference = lines.get(0)
        var extendedStartY = reference.startY
        var extendedEndY = reference.endY
        var cumulatedStartX = 0.0
        var cumulatedEndX = 0.0
        for (line in lines) {
            if (line.key != reference.key) throw Error("illegal key")
            extendedStartY = min(extendedStartY, line.startY)
            extendedEndY = max(extendedEndY, line.endY)
            cumulatedStartX += line.startX
            cumulatedEndX += line.endX
        }
        return Line(
            cumulatedStartX / lines.size,
            cumulatedEndX / lines.size,
            extendedStartY,
            extendedEndY
        )
    }

    /*
     * merge all lines expected that they are all horizontal
     * here horizontal means straight but also slightly slanted
     * the created line will have
     *  + a mean startY and a mean endY over all lines
     *  + the lowest startX and highest endX of all lines
     */
    private fun mergeHorizontally(lines: MutableList<Line>): Line {
        val reference = lines.get(0)
        var extendedStartX = reference.startX
        var extendedEndX = reference.endX
        var cumulatedStartY = 0.0
        var cumulatedEndY = 0.0
        for (line in lines) {
            if (line.key != reference.key) throw Error("illegal key")
            extendedStartX = min(extendedStartX, line.startX)
            extendedEndX = max(extendedEndX, line.endX)
            cumulatedStartY += line.startY
            cumulatedEndY += line.endY
        }
        return Line(
            extendedStartX,
            extendedEndX,
            cumulatedStartY / lines.size,
            cumulatedEndY / lines.size
        )
    }

    private fun merge(lines: MutableList<Line>): Line? {
        if (lines.isEmpty()) return null
        if (lines.size == 1) return lines[0]
        return when (lines[0].orientation) {
            Line.Orientation.HORIZONTAL -> mergeHorizontally(lines)
            Line.Orientation.VERTICAL -> mergeVertically(lines)
            Line.Orientation.OTHER -> null
        }
    }

    /*
     * merge all lines with same key
     *
     * Here the lines can be either slanted or straight the result is according to the orientation
     * in one direction and the longest we can build and in the other direction mean values
     */
    fun process(initial: Lines): Lines {
        val selected: MutableList<Line> = mutableListOf()
        for (entry in initial.registry.entries) {
            val line = merge(entry.value)
            if (line == null) continue
            selected.add(line)
        }
        return Lines().register(selected)
    }
}