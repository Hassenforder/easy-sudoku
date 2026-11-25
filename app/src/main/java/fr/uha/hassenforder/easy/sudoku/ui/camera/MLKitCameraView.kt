package fr.uha.hassenforder.easy.sudoku.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.core.SurfaceRequest
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.lifecycle.awaitInstance
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.Text
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptionsInterface
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.GridDrawScope
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.computeGridDrawScope
import kotlinx.coroutines.flow.MutableStateFlow
import java.util.concurrent.Executors

private fun cameraCapture (
    context : Context,
    imageCapture : ImageCapture,
    onCaptured: (Bitmap?) -> Unit,
) {
    val executor = ContextCompat.getMainExecutor(context)
    imageCapture.takePicture(executor, object : ImageCapture.OnImageCapturedCallback() {
            override fun onCaptureSuccess(image: ImageProxy) {
                val matrix = Matrix().apply {
                    postRotate(image.imageInfo.rotationDegrees.toFloat())
                }
                val rotatedBitmap = Bitmap.createBitmap(
                    image.toBitmap(),
                    0, 0, image.width, image.height,
                    matrix, true
                )
                onCaptured(rotatedBitmap)
                image.close()
            }
            override fun onError(exception: ImageCaptureException) {
                onCaptured(null)
            }
        }
    )
}

private fun dump (text : Text) : String {
    val tmp : StringBuilder = StringBuilder()
    var b_idx=1
    for (block in text.textBlocks) {
        var l_idx=1
        for (line in block.lines) {
            var e_idx=1
            for (element in line.elements) {
                tmp.append(" b")
                tmp.append(b_idx)
                tmp.append(".")
                tmp.append(l_idx)
                tmp.append(".")
                tmp.append(e_idx)
                tmp.append("=")
                tmp.append(element.text)
                ++e_idx
            }
            ++l_idx
        }
        ++b_idx
    }
    return tmp.toString()
}

private class TextRecognitionAnalyzer(
    private val onTextDetected: (String) -> Unit
) : ImageAnalysis.Analyzer {

    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        imageProxy.image?.let {
            val image = InputImage.fromMediaImage(it, imageProxy.imageInfo.rotationDegrees)
            textRecognizer.process(image)
                .addOnSuccessListener { text ->
                    onTextDetected(dump(text))
                    imageProxy.close()
                }
        }
    }
}

@Composable
fun MLKitCameraView(
    onCaptured: (Bitmap) -> Unit,
    decorator : GridDrawScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    var imageAnalysis by remember { mutableStateOf<ImageAnalysis?>(null) }
    val surfaceRequests = remember { MutableStateFlow<SurfaceRequest?>(null) }

    // Reader: Compose state derived from the flow
    val surfaceRequest by surfaceRequests.collectAsState(initial = null)

    // Bind CameraX use cases once
    LaunchedEffect(Unit) {
        val provider = ProcessCameraProvider.awaitInstance(context)
        val preview = Preview.Builder().build().apply {
            setSurfaceProvider { request ->
                surfaceRequests.value = request
            }
        }
        imageCapture = ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
        imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .apply {
                setAnalyzer(cameraExecutor, TextRecognitionAnalyzer() { Log.d ("MLKIT", "Text detected: $it") } )
            }
        provider.unbindAll()
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture,
            imageAnalysis,
        )
    }

    surfaceRequest?.let { request ->
        CameraXViewfinder(
            surfaceRequest = request,
            modifier = Modifier
                .fillMaxSize()
                .clickable() {
                    cameraCapture(context, imageCapture!!) {
                        it?.let { onCaptured(it) }
                    }
                }
                .drawWithContent() {
                    drawContent()
                    val decoratorDrawScope = computeGridDrawScope(
                        alignment = Alignment.TopCenter,
                        contentScale = ContentScale.Fit,
                        9
                    )
                    decoratorDrawScope.decorator()
                },
            alignment = Alignment.TopCenter,
            contentScale = ContentScale.Fit,
        )
    }
}
