package yuku.gambaraja.kokrepot

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.lifecycle.ViewModel
import yuku.gambaraja.kokrepot.model.DrawingAction
import yuku.gambaraja.kokrepot.model.Tool

class DrawingViewModel : ViewModel() {

    companion object {
        const val STAMP_FIXED_SIZE = 50f
    }

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
        // If tapping the same stamp tool, toggle back to brush
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
            // Place a dot
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
    }

    fun onPanDelta(delta: Offset) {
        panOffset = Offset(panOffset.x + delta.x, panOffset.y + delta.y)
    }

    fun undo() {
        if (_actions.isEmpty()) return
        val last = _actions.removeLast()
        _redoStack.add(last)
        updateUndoRedo()
    }

    fun redo() {
        if (_redoStack.isEmpty()) return
        val action = _redoStack.removeLast()
        _actions.add(action)
        updateUndoRedo()
    }

    private fun updateUndoRedo() {
        canUndo = _actions.isNotEmpty()
        canRedo = _redoStack.isNotEmpty()
    }
}
