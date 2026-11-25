package fr.uha.hassenforder.easy.sudoku.processing.image

import android.graphics.Rect
import org.opencv.core.Mat
import kotlin.math.min

class LineProcessor(
    private val width: Double,
    private val height: Double,
    private val expectedCellCount: Int
) {
    init {
        val size = min(width, height)
        Line.Companion.setStraightThreshold((size * 2f / 100f).toInt())
        Line.Companion.setQuantizerLevel((size * 2f / 100f).toInt())
    }

    fun convert(linesMat: Mat): Lines {
        val converter = LineConverter(this.width, this.height)
        val converted = converter.process(linesMat)
        val result = converted
        return result
    }

    fun reduce(initial: Lines): Lines {
        val merger = LineMerger()
        val straightener = LineStraightener()
        val size = min(width, height)
        val remover = LineRemover(size * 2f / 100f)
        val injecter = LineInjecter(this.width, this.height, this.expectedCellCount + 1)
        val merged = merger.process(initial)
        val straightened = straightener.process(merged)
        val removed = remover.process(straightened)
        val injected = injecter.process(removed)
        val result = injected
        return result
    }

    fun buildBoxes(lines: Lines): List<Rect> {
        val builder = BoxBuilder(this.width, this.height, this.expectedCellCount)
        val boxes = builder.process(lines)
        val result = boxes
        return result
    }

}