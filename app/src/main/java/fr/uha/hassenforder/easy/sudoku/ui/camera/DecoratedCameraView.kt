package fr.uha.hassenforder.easy.sudoku.ui.camera

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.compose.CameraXViewfinder
import androidx.camera.core.CameraSelector
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
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.GridDrawScope
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.computeGridDrawScope
import kotlinx.coroutines.flow.MutableStateFlow

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

@Composable
fun DecoratedCameraView(
    onCaptured: (Bitmap) -> Unit,
    decorator : GridDrawScope.() -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
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
        provider.unbindAll()
        provider.bindToLifecycle(
            lifecycleOwner,
            CameraSelector.DEFAULT_BACK_CAMERA,
            preview,
            imageCapture
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
                    val decoratorDrawScope = computeGridDrawScope(alignment = Alignment.TopCenter, contentScale = ContentScale.Fit,9)
                    decoratorDrawScope.decorator()
                }
            ,
            alignment = Alignment.TopCenter,
            contentScale = ContentScale.Fit,
        )
    }
}
