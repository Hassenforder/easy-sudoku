package fr.uha.hassenforder.easy.sudoku.ui.camera.decorator

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import fr.uha.hassenforder.easy.sudoku.processing.image.Line

fun GridDrawScope.drawDynamicLines (
    lines : Collection<Line>,
    colorLine : Color = Color.Red,
    strokeWidth : Dp = 3.dp,
) {
    lines.forEach {
        drawLine(
            color = colorLine,
            start = Offset(outerRect.left + it.startX.toFloat(), outerRect.top + it.startY.toFloat()),
            end = Offset(outerRect.right + it.endX.toFloat(), outerRect.top + it.endY.toFloat()),
            strokeWidth = strokeWidth.toPx()
        )
    }
}

/*
 * this function is to avoid a flatten() in the Lines registry to call the drawDynamicLines
 */
fun GridDrawScope.drawDynamicLineCollections (
    lines : Collection<Collection<Line>>,
    colorLine : Color = Color.Red,
    strokeWidth : Dp = 3.dp,
) {
    Log.d("drawDynamicLineCollections", "linesCount = ${lines.size}")
    lines.forEach {
        it.forEach {
            Log.d("drawDynamicLineCollections", "Line : $it")
            drawLine(
                color = colorLine,
                start = Offset(outerRect.left + it.startX.toFloat(), outerRect.top + it.startY.toFloat()),
                end = Offset(outerRect.left + it.endX.toFloat(), outerRect.top + it.endY.toFloat()),
                strokeWidth = strokeWidth.toPx()
            )
        }
    }
}
