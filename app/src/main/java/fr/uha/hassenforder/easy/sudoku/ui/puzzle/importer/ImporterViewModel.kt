package fr.uha.hassenforder.easy.sudoku.ui.puzzle.importer

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.googlecode.tesseract.android.TessBaseAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import fr.uha.hassenforder.easy.android.flow.Result
import fr.uha.hassenforder.easy.android.flow.combine
import fr.uha.hassenforder.easy.android.navigation.Navigator
import fr.uha.hassenforder.easy.android.ui.app.UITitle
import fr.uha.hassenforder.easy.android.ui.app.UITitleBuilder
import fr.uha.hassenforder.easy.sudoku.dto.FullPuzzle
import fr.uha.hassenforder.easy.sudoku.model.Cell
import fr.uha.hassenforder.easy.sudoku.model.Puzzle
import fr.uha.hassenforder.easy.sudoku.model.PuzzleLevel
import fr.uha.hassenforder.easy.sudoku.navigation.PuzzlesDestination
import fr.uha.hassenforder.easy.sudoku.processing.image.BoxedText
import fr.uha.hassenforder.easy.sudoku.processing.image.ExportedBitmap
import fr.uha.hassenforder.easy.sudoku.processing.image.HoughProcessing
import fr.uha.hassenforder.easy.sudoku.processing.image.ImageProcessing
import fr.uha.hassenforder.easy.sudoku.processing.image.Lines
import fr.uha.hassenforder.easy.sudoku.processing.image.OCRProcessing2
import fr.uha.hassenforder.easy.sudoku.processing.image.OCRProcessing3
import fr.uha.hassenforder.easy.sudoku.processing.image.PreProcessor
import fr.uha.hassenforder.easy.sudoku.repository.PuzzleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.opencv.android.OpenCVLoader
import javax.inject.Inject
import kotlin.concurrent.Volatile

@HiltViewModel
class ImporterViewModel @Inject constructor (
    private val repository: PuzzleRepository,
    private val navigator: Navigator
): ViewModel() {

    private var openCVInit : Boolean
    private val tessApi: TessBaseAPI = TessBaseAPI()
    private var tessInit : Boolean

    private val recycleLock = Any()

    @Volatile
    private var tessProcessing = false

    @Volatile
    private var recycleAfterProcessing = false

    init {
        openCVInit = OpenCVLoader.initLocal()
        if (! openCVInit) {
            Log.e(TAG, "Cannot initialize OpenCV:")
        }
        try {
            val dataPath = repository.installTessDataFile(TESS_DATA_FOLDER, TESS_DATA_FILE)
            tessInit = tessApi.init(dataPath, TESS_LANG, TESS_ENGINE)
        } catch (e: IllegalArgumentException) {
            tessInit = false
            Log.e(TAG, "Cannot initialize Tesseract:", e)
        } catch (e: RuntimeException) {
            tessInit = false
            Log.e(TAG, "Cannot initialize Tesseract:", e)
        }
    }

    @Override
    override fun onCleared() {
        super.onCleared()
        synchronized(recycleLock) {
            if (tessProcessing) {
                recycleAfterProcessing = true
                tessApi.stop()
            } else {
                tessApi.recycle()
            }
        }
    }

    val uiTitleBuilder = UITitleBuilder()

    private val _uiTitle : StateFlow<UITitle> = uiTitleBuilder.uiTitle.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = UITitle()
    )

    private val _selectedPreprocessor: MutableStateFlow<PreProcessor> = MutableStateFlow(PreProcessor.erode_dilate)
    private val _threshold: MutableStateFlow<Double> = MutableStateFlow(96.0)
    private val _selectedBitmap: MutableStateFlow<ExportedBitmap> = MutableStateFlow(ExportedBitmap.original)
    private val _intermediateBitmaps: MutableStateFlow<Map<ExportedBitmap, Bitmap>> = MutableStateFlow(mapOf())

    private val _patches: MutableStateFlow<Map<Int, OCRProcessing3.Patch>> = MutableStateFlow(mapOf())

    private val _inputBitmap: MutableStateFlow<Bitmap?> = MutableStateFlow(null)

    private val _internalBitmap: StateFlow<Bitmap?> = _inputBitmap.map { b ->
        if (b == null) return@map null
        val imageProcessing = ImageProcessing(exportedBitmap = ExportedBitmap.entries)
        val result = imageProcessing.process(b)
        _intermediateBitmaps.value = imageProcessing.intermediateBitmaps
        result
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
/*
    private val _lines: StateFlow<Lines> = combine (_inputBitmap, _selectedPreprocessor, _threshold) { b, p, t ->
        if (b == null) return@combine Lines()
        val houghProcessing = HoughProcessing(
            preProcessor = p,
            threshold = t,
            exportedBitmap = ExportedBitmap.entries
        )
        val result = houghProcessing.process(b)
        _intermediateBitmaps.value = houghProcessing.intermediateBitmaps
        result
        Lines()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Lines())
*/

    private val _exportedBitmap: StateFlow<Bitmap?> = combine (_intermediateBitmaps, _selectedBitmap) { m, v ->
        m[v]
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _boxedText: StateFlow<List<BoxedText>> = _internalBitmap.map { b ->
        if (b == null) return@map listOf<BoxedText>()
//        OCRProcessing(tessApi).process(b, l)
//        listOf<BoxedText>()
        val ocr = OCRProcessing3()
        val result = ocr.process(b)
        _patches.value = ocr.intermediatePatches
/*        ocr.intermediatePatches.forEach {
            Log.d(TAG, "OCR result: $it")
        }
*/
/*
        result.forEach {
            Log.d(TAG, "OCR result: $it")
        }
*/        result
//        listOf<BoxedText>()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), listOf<BoxedText>())

    @Immutable
    data class UIState(
        val uiTitle : UITitle,
        val selectedPreprocessor : PreProcessor,
        val threshold : Double,
        val selectedBitmap : ExportedBitmap,
        val exportedBitmap : Bitmap?,
        val patches: Map<Int, OCRProcessing3.Patch>,
//        val lines : Lines,
//        val boxedText: List<BoxedText>,
    ) {
        companion object {
            fun create(
                uiTitle: UITitle,
                selectedPreprocessor : PreProcessor,
                threshold : Double,
                selectedBitmap : ExportedBitmap,
                exportedBitmap : Bitmap?,
                patches: Map<Int, OCRProcessing3.Patch>,
//                lines : Lines,
//                boxedText: List<BoxedText>,
            ): UIState {
                return UIState(uiTitle, selectedPreprocessor, threshold, selectedBitmap, exportedBitmap, patches)
            }
        }
    }

    val uiState : StateFlow<Result<UIState>> = combine (_uiTitle, _selectedPreprocessor, _threshold, _selectedBitmap, _exportedBitmap, _patches, _boxedText)
        { t, p, h, v, i, ps, bt->
            Result.Success(content = UIState.create(t, p,h, v,  i, ps))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), Result.Loading())

    sealed class UIEvent {
        data object OnBack: UIEvent()
        data class OnBitmap(val bitmap : Bitmap) : UIEvent()
        data class OnSelectPreprocessor(val preProcessor : PreProcessor) : UIEvent()
        data class OnSelectThreshold(val threshold : Double) : UIEvent()
        data class OnSelectBitmap(val exportedBitmap : ExportedBitmap) : UIEvent()
        data object OnSave: UIEvent()

    }

    private fun doSave () = viewModelScope.launch {
        val state = (uiState.value as Result.Success).content
        val puzzle = Puzzle(
            0,
            "Puzzle",
            9,
            PuzzleLevel.EASY,
        )
//        val cells = state.boxedText.map { Cell(0, 0, it.x, it.y, it.text.toInt(), CellState.INITIAL) }
        val cells = listOf<Cell>()
        val fullPuzzle = FullPuzzle(puzzle, cells)
        val pid : Long = repository.create(fullPuzzle)
        navigator.navigate(PuzzlesDestination.EditScreen(pid))
    }

    fun send (uiEvent : UIEvent) = viewModelScope.launch {
        if (uiState.value !is Result.Success) return@launch
        when (uiEvent) {
            is UIEvent.OnBack -> navigator.navigateUp()
            is UIEvent.OnBitmap -> _inputBitmap.value = uiEvent.bitmap
            is UIEvent.OnSelectPreprocessor -> _selectedPreprocessor.value = uiEvent.preProcessor
            is UIEvent.OnSelectThreshold -> _threshold.value = uiEvent.threshold
            is UIEvent.OnSelectBitmap -> _selectedBitmap.value = uiEvent.exportedBitmap
            is UIEvent.OnSave -> doSave ()
        }
    }

    companion object {
        const val TAG: String = "ImporterViewModel"

        const val TESS_DATA_FOLDER : String = "tessdata"
        const val TESS_DATA_FILE : String = "eng.traineddata"
        const val TESS_ENGINE: Int = TessBaseAPI.OEM_LSTM_ONLY
        const val TESS_LANG: String = "eng"
    }
}
