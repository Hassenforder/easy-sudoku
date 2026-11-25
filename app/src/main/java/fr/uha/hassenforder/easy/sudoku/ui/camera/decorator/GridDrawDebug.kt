package fr.uha.hassenforder.easy.sudoku.ui.camera.decorator

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

fun GridDrawScope.drawViewRect (
    colorLine : Color = Color.Red,
    strokeWidth : Dp = 4.dp,
) {
    val strokeWidthPx = strokeWidth.toPx()
    drawRect(
        color = colorLine,
        topLeft = Offset(0f, 0f),
        size = Size (size.width, size.height ),
        style = Stroke(width = strokeWidthPx)
    )
    drawCircle(
        color = colorLine,
        radius = 5f,
        center = center,
        style = Stroke(width = strokeWidthPx)
    )
}

fun GridDrawScope.drawInnerRect (
    colorLine : Color = Color.Red,
    strokeWidth : Dp = 4.dp,
) {
    val strokeWidthPx = strokeWidth.toPx()
    drawRect(
        color = colorLine,
        topLeft = Offset(innerRect.left, innerRect.top),
        size = Size (innerRect.width, innerRect.height ),
        style = Stroke(width = strokeWidthPx)
    )
}

fun GridDrawScope.drawOuterRect (
    colorLine : Color = Color.Red,
    strokeWidth : Dp = 4.dp,
) {
    val strokeWidthPx = strokeWidth.toPx()
    drawRect(
        color = colorLine,
        topLeft = Offset(outerRect.left, outerRect.top),
        size = Size (outerRect.width, outerRect.height ),
        style = Stroke(width = strokeWidthPx)
    )
}
