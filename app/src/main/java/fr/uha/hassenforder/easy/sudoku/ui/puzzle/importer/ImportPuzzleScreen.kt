package fr.uha.hassenforder.easy.sudoku.ui.puzzle.importer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.HourglassBottom
import androidx.compose.material.icons.outlined.Error
import androidx.compose.material.icons.outlined.Gamepad
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import fr.uha.hassenforder.easy.android.ui.DecoratorProperties
import fr.uha.hassenforder.easy.android.ui.app.AppTopBar
import fr.uha.hassenforder.easy.android.ui.field.group.Orientation
import fr.uha.hassenforder.easy.android.ui.field.group.OutlinedEnumRadioGroup
import fr.uha.hassenforder.easy.android.ui.field.group.RadioProperties
import fr.uha.hassenforder.easy.android.ui.field.slider.OutlinedSliderField
import fr.uha.hassenforder.easy.android.ui.field.slider.SliderProperties
import fr.uha.hassenforder.easy.android.ui.screen.StateScreen
import fr.uha.hassenforder.easy.sudoku.R
import fr.uha.hassenforder.easy.sudoku.model.CellState
import fr.uha.hassenforder.easy.sudoku.processing.image.ExportedBitmap
import fr.uha.hassenforder.easy.sudoku.processing.image.PreProcessor
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.computeGridDrawScope
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.drawDynamicLineCollections
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.drawInnerRect
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.drawOuterRect
import fr.uha.hassenforder.easy.sudoku.ui.camera.decorator.drawViewRect
import fr.uha.hassenforder.easy.sudoku.ui.field.GridAction

@Composable
fun ImportPuzzleScreen (
    vm: ImporterViewModel = hiltViewModel(),
) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val defaultDocumentName = stringResource(id = R.string.title_puzzle)

    LaunchedEffect(Unit) {
        vm.uiTitleBuilder.setScreenNameId(sid = R.string.title_import)
        vm.uiTitleBuilder.setDefaultDocumentName(defaultDocumentName)
    }

    StateScreen(state = uiState) {
        Scaffold(
            topBar = { AppTopBar(it.uiTitle, { vm.send(ImporterViewModel.UIEvent.OnBack) }) },
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
            ) {
                InternalImportPuzzleScreen(it, vm::send)
            }
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
private fun InternalImportPuzzleScreen(
    uiState: ImporterViewModel.UIState,
    send : (ImporterViewModel.UIEvent) -> Unit,
) {
    val showPopup = remember { mutableStateOf(true) }
    if (showPopup.value) {
        CameraDialog(
            onSelected = {
                showPopup.value = false
                it?.let { send(ImporterViewModel.UIEvent.OnBitmap(it)) }
            }
        )
    }
    val cellSize = 150
    LazyVerticalGrid(
        modifier = Modifier
            .height((9*cellSize).dp)
            .width((9*cellSize).dp)
        ,
        columns = GridCells.Fixed(9),
        verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
        horizontalArrangement = Arrangement.spacedBy(0.dp, Alignment.CenterHorizontally)
    ) {
        items(
            items = uiState.patches.values.toList(),
            key = { it.key() }
        ) { item ->
            AsyncImage(
                model = item.bitmap,
                contentDescription = "Scanned bitmap",
                modifier = Modifier.fillMaxWidth(),
                placeholder = rememberVectorPainter(Icons.Outlined.HourglassBottom),
                error = rememberVectorPainter(Icons.Outlined.Gamepad),
                alignment = Alignment.TopCenter,
                contentScale = ContentScale.Fit
            )
        }
    }
    /*
    OutlinedEnumRadioGroup(
        value = uiState.selectedPreprocessor,
        onValueChange = { send(ImporterViewModel.UIEvent.OnSelectPreprocessor(PreProcessor.valueOf(it))) },
        properties = RadioProperties(
            itemValues = arrayOf(PreProcessor.bilateral, PreProcessor.erode_dilate),
            orientation = Orientation.HorizontalList,
        ),
        decorator = DecoratorProperties(
            labelId = R.string.chose_preprocessor
        ),
        modifier = Modifier.fillMaxWidth()
    )
    if (uiState.selectedPreprocessor == PreProcessor.bilateral) {
        OutlinedSliderField(
            value = uiState.threshold.toFloat(),
            valueAsText = null,
            onValueChange = { send(ImporterViewModel.UIEvent.OnSelectThreshold(it.toDouble())) },
            modifier = Modifier.fillMaxWidth(),
            decorator = DecoratorProperties(
                labelId = R.string.chose_threshold
            ),
            properties = SliderProperties(
                0f, 255f, 32
            )
        )
    }
    val itemValues = when (uiState.selectedPreprocessor) {
        PreProcessor.bilateral -> arrayOf(ExportedBitmap.original, ExportedBitmap.gray, ExportedBitmap.denoised, ExportedBitmap.binary, )
        PreProcessor.erode_dilate -> arrayOf(ExportedBitmap.original, ExportedBitmap.gray, ExportedBitmap.erode, ExportedBitmap.dilate, ExportedBitmap.binary)
    }
    OutlinedEnumRadioGroup(
        value = uiState.selectedBitmap,
        onValueChange = { send(ImporterViewModel.UIEvent.OnSelectBitmap(ExportedBitmap.valueOf(it))) },
        properties = RadioProperties(
            itemValues = itemValues,
            orientation = Orientation.HorizontalGrid(3),
        ),
        decorator = DecoratorProperties(
            labelId = R.string.chose_export_bitmap
        ),
        modifier = Modifier.fillMaxWidth()
    )
    AsyncImage(
        model = uiState.exportedBitmap,
        contentDescription = "Scanned bitmap",
        modifier = Modifier
            .fillMaxWidth()
//            .fillMaxHeight()
            .drawWithContent {
                drawContent()
                val decoratorDrawScope = computeGridDrawScope(alignment = Alignment.TopCenter, contentScale = ContentScale.Fit, 9)
//                decoratorDrawScope.drawViewRect(colorLine = Color.Red)
//                decoratorDrawScope.drawInnerRect(colorLine = Color.Green)
//                decoratorDrawScope.drawOuterRect(colorLine = Color.Cyan)
                decoratorDrawScope.drawDynamicLineCollections(uiState.lines.registry.values, colorLine = Color.Blue)
            },
        placeholder = rememberVectorPainter(Icons.Outlined.HourglassBottom),
        error = rememberVectorPainter(Icons.Outlined.Error),
        alignment = Alignment.TopCenter,
        contentScale = ContentScale.Fit
    )
    */
    Spacer(modifier = Modifier.height(16.dp))
    Row (
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Button({ send(ImporterViewModel.UIEvent.OnBack) } ) {
            Text(stringResource(android.R.string.cancel))
        }
        Button({ showPopup.value = true }) {
            Text(stringResource(R.string.action_rescan))
        }
        Button({ send(ImporterViewModel.UIEvent.OnSave) }) {
            Text(stringResource(android.R.string.ok))
        }
    }
}
