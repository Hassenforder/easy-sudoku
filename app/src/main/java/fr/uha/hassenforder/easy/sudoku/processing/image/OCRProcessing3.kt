package fr.uha.hassenforder.easy.sudoku.processing.image

import android.graphics.Bitmap
import android.graphics.Rect
import android.util.Log
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.googlecode.tesseract.android.TessBaseAPI
import kotlin.collections.forEach
import kotlin.math.min
import kotlin.math.max

class OCRProcessing3 (
) {

    var intermediatePatches = mutableMapOf<Int, Patch>()

    data class Patch (
        val x : Int,
        val y : Int,
        val rect : Rect,
        val bitmap : Bitmap? = null,
        val text : String = ""
    ) {
        fun key () : Int {
            return y*1000+x
        }

        override fun toString () : String {
            val tmp = StringBuilder()
            tmp.append(y)
            tmp.append(".")
            tmp.append(x)
            tmp.append("=")
            tmp.append(text)
            return tmp.toString()
        }
    }

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private fun preProcess (bitmap : Bitmap) : List<Patch> {
        val result: MutableList<Patch> = mutableListOf()
        val w = bitmap.width
        val h = bitmap.height
        val cellSize = min (w, h) / 9
        (0 ..< 9).forEach { y ->
            (0 ..< 9).forEach { x ->
                val cellLeft = x * cellSize
                val cellTop = y * cellSize
                val patch = Patch(
                    x,
                    y,
                    Rect(
                    max(0, cellLeft),
                    max(0, cellTop),
                    min(w, cellLeft + cellSize),
                    min(h, cellTop +  cellSize),
                    )
                )
                result.add(patch)
            }
        }
        return result
    }

    private fun applyOCROnBoxes (initialBitmap: Bitmap, patches : List<Patch>) : List<BoxedText> {
        val result: MutableList<BoxedText> = mutableListOf()
        patches.forEach { patch ->
            val cellBitmap = Bitmap.createBitmap(
                initialBitmap,
                patch.rect.left,
                patch.rect.top,
                patch.rect.width(),
                patch.rect.height()
            )
            intermediatePatches[patch.key()] = patch.copy(bitmap = cellBitmap)
            textRecognizer.process(cellBitmap, 0).addOnSuccessListener {
                Log.d("OCRProcessing3", "${patch.y}.${patch.x} : ${it.text}")
                result.add(BoxedText(patch.rect, it.text, 0.0))
            }
        }
        return result
    }

    private fun postProcess (rawBoxedText: List<BoxedText>) : List<BoxedText> {
        return rawBoxedText
    }

    fun process(bitmap: Bitmap): List<BoxedText> {
        val boxes = preProcess(bitmap)
        val rawBoxedText = applyOCROnBoxes(bitmap, boxes)
        val bestBoxedText = postProcess(rawBoxedText)
        return bestBoxedText
    }

}