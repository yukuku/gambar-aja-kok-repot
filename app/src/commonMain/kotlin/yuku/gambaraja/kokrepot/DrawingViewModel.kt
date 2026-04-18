package yuku.gambaraja.kokrepot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import yuku.gambaraja.kokrepot.model.DrawingAction
import yuku.gambaraja.kokrepot.model.Tool

/**
 * Holds the canvas state and dispatches mutations. Not an Android ViewModel —
 * multiplatform. Callers create an instance once (e.g., in a `remember` block on
 * web, or in an Activity scope on Android) and dispose it when done.
 */
class DrawingViewModel(private val storage: DrawingStorage) {

    companion object {
        const val STAMP_FIXED_SIZE = 50f
    }

    private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    var selectedColor by mutableStateOf(Color.Black)
        private set

    var selectedThickness by mutableStateOf(8f)
        private set

    var selectedTool by mutableStateOf(Tool.BRUSH)
        private set

    var panOffset by mutableStateOf(Offset.Zero)
        private set

    var canUndo by mutableStateOf(false)
        private set

    var canRedo by mutableStateOf(false)
        private set

    var currentStrokePoints by mutableStateOf<List<Offset>>(emptyList())
        private set

    private val _actions = mutableStateListOf<DrawingAction>()
    val actions: List<DrawingAction> get() = _actions

    private val _redoStack = mutableListOf<DrawingAction>()

    private var saveJob: Job? = null

    init {
        val snapshot = storage.load()
        if (snapshot != null) {
            _actions.addAll(snapshot.actions)
            panOffset = snapshot.panOffset
            updateUndoRedo()
        }
    }

    fun selectColor(color: Color) {
        selectedColor = color
        if (selectedTool == Tool.ERASER || selectedTool.isStamp) {
            selectedTool = Tool.BRUSH
        }
    }

    fun selectThickness(thickness: Float) {
        selectedThickness = thickness
        if (selectedTool.isStamp) {
            selectedTool = Tool.BRUSH
        }
    }

    fun selectTool(tool: Tool) {
        if (tool == selectedTool && tool.isStamp) {
            selectedTool = Tool.BRUSH
        } else {
            selectedTool = tool
        }
    }

    fun onDrawStart(worldPoint: Offset) {
        currentStrokePoints = listOf(worldPoint)
    }

    fun onDrawMove(worldPoint: Offset) {
        currentStrokePoints = currentStrokePoints + worldPoint
    }

    fun onDrawEnd() {
        val points = currentStrokePoints
        if (points.isEmpty()) return

        val isEraser = selectedTool == Tool.ERASER
        val color = if (isEraser) Color.White.toArgb() else selectedColor.toArgb()

        val stroke = DrawingAction.Stroke(
            points = points,
            color = color,
            thickness = selectedThickness,
            isEraser = isEraser
        )
        _actions.add(stroke)
        _redoStack.clear()
        currentStrokePoints = emptyList()
        updateUndoRedo()
        scheduleAutoSave()
    }

    fun onDrawCancel() {
        currentStrokePoints = emptyList()
    }

    fun onTap(worldPoint: Offset) {
        val tool = selectedTool
        if (tool.isStamp) {
            val stampType = tool.stampType ?: return
            val stamp = DrawingAction.Stamp(
                center = worldPoint,
                stampType = stampType,
                color = selectedColor.toArgb(),
                size = STAMP_FIXED_SIZE
            )
            _actions.add(stamp)
            _redoStack.clear()
            updateUndoRedo()
        } else {
            val isEraser = selectedTool == Tool.ERASER
            val color = if (isEraser) Color.White.toArgb() else selectedColor.toArgb()
            val dot = DrawingAction.Stroke(
                points = listOf(worldPoint),
                color = color,
                thickness = selectedThickness,
                isEraser = isEraser
            )
            _actions.add(dot)
            _redoStack.clear()
            updateUndoRedo()
        }
        scheduleAutoSave()
    }

    fun onPanDelta(delta: Offset) {
        panOffset = Offset(panOffset.x + delta.x, panOffset.y + delta.y)
    }

    fun undo() {
        if (_actions.isEmpty()) return
        val last = _actions.removeLast()
        _redoStack.add(last)
        updateUndoRedo()
        scheduleAutoSave()
    }

    fun redo() {
        if (_redoStack.isEmpty()) return
        val action = _redoStack.removeLast()
        _actions.add(action)
        updateUndoRedo()
        scheduleAutoSave()
    }

    fun saveNow() {
        saveJob?.cancel()
        val snapshot = DrawingSnapshot(_actions.toList(), panOffset)
        saveJob = scope.launch {
            storage.save(snapshot)
        }
    }

    private fun scheduleAutoSave() {
        saveJob?.cancel()
        val snapshot = DrawingSnapshot(_actions.toList(), panOffset)
        saveJob = scope.launch {
            storage.save(snapshot)
        }
    }

    private fun updateUndoRedo() {
        canUndo = _actions.isNotEmpty()
        canRedo = _redoStack.isNotEmpty()
    }
}
