package fr.uha.hassenforder.easy.sudoku.ui.camera.decorator

import android.graphics.Rect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import kotlin.math.min

fun GridDrawScope.drawGridBorder (
    colorLine : Color = Color.Blue,
    strokeWidth : Dp = 3.dp,
) {
    val strokeWidthPx = strokeWidth.toPx()
    drawRect(
        color = colorLine,
        topLeft = innerRect.topLeft,
        size = Size (innerRect.right - innerRect.left, innerRect.bottom - innerRect.top ),
        style = Stroke(width = strokeWidthPx)
    )
}

fun GridDrawScope.drawStaticGrid (
    colorLine : Color = Color.Blue,
    strokeWidth : Dp = 3.dp,
) {
    (0..cellCount).forEach {
        drawLine(
            color = colorLine,
            start = Offset(innerRect.left + 0f, innerRect.top + it * cellSize),
            end = Offset(innerRect.right, innerRect.top + it * cellSize),
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            color = colorLine,
            start = Offset(innerRect.left + it * cellSize, innerRect.top + 0f),
            end = Offset(innerRect.left + it * cellSize, innerRect.bottom),
            strokeWidth = strokeWidth.toPx()
        )
    }
}

private fun GridDrawScope.buildRects (width : Int, height : Int) : List<Rect> {
    val result: MutableList<Rect> = mutableListOf()
    val w = width
    val h = height
    val cellSize = min (w, h) / 10
    val boardMargin = cellSize * 50 / 100
    val cellMargin = cellSize * 25 / 100
    val boardStart = boardMargin
    val boardSize = cellSize * 9
    (0 ..< 9).forEach { x ->
        (0 ..< 9).forEach { y ->
            if ((x + y * 9) % 2 == 0) {
                val cellLeft = innerRect.left.toInt() + boardStart + x * cellSize
                val cellTop = innerRect.top.toInt()  + boardStart + y * cellSize
                val rect = Rect(
                    max(0, cellLeft - cellMargin),
                    max(0, cellTop - cellMargin),
                    min(w, cellLeft + cellSize + cellMargin),
                    min(h, cellTop + cellSize + cellMargin),
                )
                result.add(rect)
            }
        }
    }
    return result
}

fun GridDrawScope.drawOCRCheckerBoard (
    rects : List<Rect>?,
    colorLine : Color = Color.Blue,
    strokeWidth : Dp = 1.dp,
) {
    val toDraw = rects?: buildRects(innerRect.width.toInt(), innerRect.height.toInt())
    val strokeWidthPx = strokeWidth.toPx()
    toDraw.forEach {
        drawRect(
            color = colorLine,
            topLeft = Offset(it.left.toFloat(), it.top.toFloat()),
            size = Size (it.width().toFloat(), it.height().toFloat()),
            style = Stroke(width = strokeWidthPx)
        )
    }
}

fun GridDrawScope.drawSquareVisor (
    colorLine : Color = Color.Blue,
    strokeWidth : Dp = 3.dp,
    wireLength : Float = 2.0f
) {
    val length = cellSize * wireLength
    val strokeWidthPx = strokeWidth.toPx()
    drawLine(
        color = colorLine,
        start = Offset(innerRect.left + 0f, innerRect.top),
        end = Offset(innerRect.left + length, innerRect.top),
        strokeWidth = strokeWidthPx
    )
    drawLine(
        color = colorLine,
        start = Offset(innerRect.right - length, innerRect.top),
        end = Offset(innerRect.right, innerRect.top),
        strokeWidth = strokeWidthPx
    )
    drawLine(
        color = colorLine,
        start = Offset(innerRect.left + 0f, innerRect.bottom),
        end = Offset(innerRect.left + length, innerRect.bottom),
        strokeWidth = strokeWidthPx
    )
    drawLine(
        color = colorLine,
        start = Offset(innerRect.right - length, innerRect.bottom),
        end = Offset(innerRect.right, innerRect.bottom),
        strokeWidth = strokeWidthPx
    )
    drawLine(
        color = colorLine,
        start = Offset(innerRect.left, innerRect.top + 0f),
        end = Offset(innerRect.left, innerRect.top + length),
        strokeWidth = strokeWidthPx
    )
    drawLine(
        color = colorLine,
        start = Offset(innerRect.left, innerRect.bottom - length),
        end = Offset(innerRect.left, innerRect.bottom),
        strokeWidth = strokeWidthPx
    )
    drawLine(
        color = colorLine,
        start = Offset(innerRect.right, innerRect.top + 0f),
        end = Offset(innerRect.right, innerRect.top + length),
        strokeWidth = strokeWidthPx
    )
    drawLine(
        color = colorLine,
        start = Offset(innerRect.right, innerRect.bottom - length),
        end = Offset(innerRect.right, innerRect.bottom),
        strokeWidth = strokeWidthPx
    )
}
