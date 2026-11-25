package fr.uha.hassenforder.easy.sudoku.processing.image

import org.opencv.core.Mat

class LineConverter(private val width: Double, private val height: Double) {
    fun process(linesMat: Mat): Lines {
        val selected: MutableList<Line> = mutableListOf()
        val squaredHalfSize = width * height / 4f
        for (i in 0..<linesMat.rows()) {
            val houghLine = linesMat.get(i, 0)
            val line = Line(houghLine)
            if (line.orientation == Line.Orientation.OTHER) continue
            if (line.squaredLength() < squaredHalfSize) continue
            selected.add(line)
        }
        return Lines().register(selected)
    }
}