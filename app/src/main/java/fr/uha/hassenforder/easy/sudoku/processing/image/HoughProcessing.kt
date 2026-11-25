package fr.uha.hassenforder.easy.sudoku.processing.image

import android.graphics.Bitmap
import android.util.Log
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.Mat
import org.opencv.core.Size
import org.opencv.imgproc.Imgproc

enum class PreProcessor {
    bilateral,
    erode_dilate
}

enum class ExportedBitmap {
    original,
    gray,
    denoised,
    binary,
    erode,
    dilate
}

class HoughProcessing (
    val preProcessor: PreProcessor = PreProcessor.erode_dilate,
    val threshold : Double = 96.0,
    val exportedBitmap: List<ExportedBitmap> = emptyList()
) {

    var intermediateBitmaps = mutableMapOf<ExportedBitmap, Bitmap>()

    private fun exportMat (mat : Mat) : Bitmap {
        val bmp = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(mat, bmp)
        return bmp
    }

    private fun preProcessAsBilateral (bitmap : Bitmap) : Mat {
        val originalMat = Mat()
        val bmp32: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        Utils.bitmapToMat(bmp32, originalMat)
        if (originalMat.empty()) {
            Log.d("Hough", "Error opening image!")
        }
        if (exportedBitmap.contains(ExportedBitmap.original))
            intermediateBitmaps[ExportedBitmap.original] = exportMat(originalMat)
        val grayMat = Mat()
        Imgproc.cvtColor(originalMat, grayMat,Imgproc.COLOR_RGBA2GRAY)
        if (exportedBitmap.contains(ExportedBitmap.gray))
            intermediateBitmaps[ExportedBitmap.gray] = exportMat(grayMat)
        val denoisedMat = Mat()
        Imgproc.bilateralFilter(grayMat, denoisedMat, 15, 75.0, 75.0)
        if (exportedBitmap.contains(ExportedBitmap.denoised))
            intermediateBitmaps[ExportedBitmap.denoised] = exportMat(denoisedMat)
        val binaryMat = Mat()
        Imgproc.threshold(denoisedMat, binaryMat, threshold, 255.0, Imgproc.THRESH_BINARY_INV)
        if (exportedBitmap.contains(ExportedBitmap.binary))
            intermediateBitmaps[ExportedBitmap.binary] = exportMat(binaryMat)
        return binaryMat
    }

    private fun preProcessAsErodeDilate (bitmap : Bitmap) : Mat {
        val originalMat = Mat()
        val bmp32: Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, false)
        Utils.bitmapToMat(bmp32, originalMat)
        if (originalMat.empty()) {
            Log.d("Hough", "Error opening image!")
        }
        if (exportedBitmap.contains(ExportedBitmap.original))
            intermediateBitmaps[ExportedBitmap.original] = exportMat(originalMat)
        val grayMat = Mat()
        Imgproc.cvtColor(originalMat, grayMat,Imgproc.COLOR_RGBA2GRAY)
        Imgproc.adaptiveThreshold(
            grayMat,
            grayMat,
            255.0,
            Imgproc.ADAPTIVE_THRESH_MEAN_C,
            Imgproc.THRESH_BINARY,
            7,
            5.0
        )
        if (exportedBitmap.contains(ExportedBitmap.gray))
            intermediateBitmaps[ExportedBitmap.gray] = exportMat(grayMat)
        val kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ERODE, Size(2.0, 2.0))
        Imgproc.erode(grayMat, grayMat, kernel)
        if (exportedBitmap.contains(ExportedBitmap.erode))
            intermediateBitmaps[ExportedBitmap.erode] = exportMat(grayMat)
        val kernelDil = Imgproc.getStructuringElement(Imgproc.MORPH_DILATE, Size(2.0, 2.0))
        Imgproc.dilate(grayMat, grayMat, kernelDil)
        if (exportedBitmap.contains(ExportedBitmap.dilate))
            intermediateBitmaps[ExportedBitmap.dilate] = exportMat(grayMat)
        Core.bitwise_not(grayMat, grayMat)
        if (exportedBitmap.contains(ExportedBitmap.binary))
            intermediateBitmaps[ExportedBitmap.binary] = exportMat(grayMat)
        return grayMat
    }

    private fun preProcess (bitmap : Bitmap) : Mat {
        return when (preProcessor) {
            PreProcessor.bilateral -> preProcessAsBilateral(bitmap)
            PreProcessor.erode_dilate -> preProcessAsErodeDilate(bitmap)
        }
    }

    private fun applyHoughTransform (binaryMat : Mat) : Mat {
        val oneDegree = Math.PI / 180
        val fiveDegrees = 5 * oneDegree
        val linesMat = Mat()
        Imgproc.HoughLinesP(binaryMat, linesMat, 1.0, oneDegree, 80, 30.0, 10.0)
        return linesMat
    }

    private fun postProcess (width : Int, height : Int, linesMat : Mat) : Lines {
        val processor = LineProcessor(width.toDouble(), height.toDouble(), 9)
        val initialHoughLines = processor.convert(linesMat)
        val reducedHoughLines = processor.reduce(initialHoughLines)
        return reducedHoughLines
    }

    fun process(bitmap : Bitmap): Lines {
        val binaryMat = preProcess(bitmap)
        val linesMat = applyHoughTransform(binaryMat)
        val finalLines = postProcess(binaryMat.cols(), binaryMat.rows(), linesMat)
        return finalLines
    }

}