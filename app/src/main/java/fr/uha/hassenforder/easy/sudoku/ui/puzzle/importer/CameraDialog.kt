package fr.uha.hassenforder.easy.sudoku.ui.puzzle.importer

import android.Manifest
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import fr.uha.hassenforder.easy.android.ui.dialog.OutlinedDialog
import fr.uha.hassenforder.easy.sudoku.R
import fr.uha.hassenforder.easy.sudoku.ui.camera.DecoratedCameraView
import fr.uha.hassenforder.easy.sudoku.ui.camera.MLKitCameraView
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.drawOCRCheckerBoard
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.drawSquareVisor
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.drawViewRect

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraDialog(
    onSelected: (Bitmap?) -> Unit,
    titleId : Int = R.string.title_scanner,
) {
    val context = LocalContext.current
    val cameraPermissionState = rememberPermissionState(permission = Manifest.permission.CAMERA)
    if (cameraPermissionState.status.isGranted) {
        OutlinedDialog (onDismiss = { onSelected(null) }, titleId = titleId) {
            Text(
                text = stringResource(R.string.scanner_instruction),
                modifier = Modifier.padding(bottom = 8.dp)
            )
            DecoratedCameraView(onSelected,
                {
                    drawViewRect()
                    drawSquareVisor()
                }
            )
        }
    } else if (cameraPermissionState.status.shouldShowRationale) {
        Toast.makeText(context, stringResource(fr.uha.hassenforder.easy.android.R.string.camera_rationale), Toast.LENGTH_LONG).show()
    } else {
        LaunchedEffect(Unit) {
            cameraPermissionState.launchPermissionRequest()
        }
    }
}
