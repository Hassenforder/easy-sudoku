package fr.uha.hassenforder.easy.sudoku.processing.image

import android.graphics.Rect
import android.util.Log
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class BoxBuilder(
    private val width: Double,
    private val height: Double,
    private val expectedCellCount: Int
) {
    private fun lowestHorizontal(lines: Lines): Line {
        val best = lines.registry.entries.filter { it.key.startsWith("H") }.minWithOrNull {
                a, b ->
            val aMin = minOf(a.value[0].startY, a.value[0].endY)
            val bMin = minOf(b.value[0].startY, b.value[0].endY)
            aMin.compareTo(bMin)
        }
        return best!!.value[0]
    }

    private fun highestHorizontal(lines: Lines): Line {
        val best = lines.registry.entries.filter { it.key.startsWith("H") }.maxWithOrNull {
                a, b ->
            val aMax = maxOf(a.value[0].startY, a.value[0].endY)
            val bMax = maxOf(b.value[0].startY, b.value[0].endY)
            aMax.compareTo(bMax)
        }
        return best!!.value[0]
    }

    private fun lowestVertical(lines: Lines): Line {
        val best = lines.registry.entries.filter { it.key.startsWith("V") }.minWithOrNull {
                a, b ->
            val aMin = minOf(a.value[0].startX, a.value[0].endX)
            val bMin = minOf(b.value[0].startX, b.value[0].endX)
            aMin.compareTo(bMin)
        }
        return best!!.value[0]
    }

    private fun highestVertical(lines: Lines): Line {
        val best = lines.registry.entries.filter { it.key.startsWith("V") }.maxWithOrNull {
                a, b ->
            val aMax = maxOf(a.value[0].startX, a.value[0].endX)
            val bMax = maxOf(b.value[0].startX, b.value[0].endX)
            aMax.compareTo(bMax)
        }
        return best!!.value[0]
    }

    fun process(houghLines: Lines): List<Rect> {
        val countH = houghLines.horizontalCount
        if (countH != expectedCellCount + 1) {
            Log.w(TAG, "Wrong number of horizontal lines: $countH")
            return mutableListOf()
        }
        val countV = houghLines.verticalCount
        if (countV != expectedCellCount + 1) {
            Log.w(TAG, "Wrong number of vertical lines: $countV")
            return mutableListOf()
        }
        val lowestHorizontal = lowestHorizontal(houghLines)
        val highestHorizontal = highestHorizontal(houghLines)
        val lowestVertical = lowestHorizontal(houghLines)
        val highestVertical = highestVertical(houghLines)
        val startX = min(lowestHorizontal.startX, lowestVertical.startX).toInt()
        val endX = max(highestHorizontal.endX, highestVertical.endX).toInt()
        val startY = min(lowestHorizontal.startY, lowestVertical.startY).toInt()
        val endY = max(highestHorizontal.endY, highestVertical.endY).toInt()
        val deltaX: Int = getCellSize(endX, startX, expectedCellCount.toDouble())
        val deltaY: Int = getCellSize(endY, startY, expectedCellCount.toDouble())
        val boxes: MutableList<Rect> = mutableListOf()
        val internalMargin = 5
        var sy = startY
        (0..<expectedCellCount).forEach { _ ->
            var sx = startX
            (0..<expectedCellCount).forEach { _ ->
                val rect = Rect(
                    sx + internalMargin,
                    sy + internalMargin,
                    sx + deltaX - internalMargin,
                    sy + deltaX - internalMargin
                )
                boxes.add(rect)
                sx += deltaX
            }
            sy += deltaY
        }
        return boxes
    }

    companion object {
        private fun getCellSize(minGrid: Int, maxGrid: Int, cellCount: Double): Int {
            val gridSize = abs(maxGrid - minGrid).toDouble()
            val cellSize = gridSize / cellCount
            return cellSize.toInt()
        }

        private const val TAG = "BoxBuilder"
    }
}